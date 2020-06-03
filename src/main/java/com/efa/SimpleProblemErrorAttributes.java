package com.efa;

import lombok.AllArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Map;

@AllArgsConstructor
public class SimpleProblemErrorAttributes implements ProblemDetailsBuilder, HandlerExceptionResolver, ErrorAttributes {
    
    private static final String ERROR_ATTRIBUTE = SimpleProblemErrorAttributes.class.getName() + ".ERROR";
    
    private final ErrorAttributesConstructorFactory errorAttributesConstructorFactory;
    
    @Override
    public Problem getProblemDetails(WebRequest webRequest, ErrorAttributeOptions options) {
        final Integer status = (Integer) getAttribute(webRequest, RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            final HttpStatus httpStatus = HttpStatus.resolve(status);
            if (httpStatus != null) {
                return Problem.statusOnly(httpStatus);
            }
        }
        return new Problem(URI.create("about:blank"), "None", 999, null, null);
        
    }
    
    @Override
    @SuppressWarnings("NullableProblems")
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        storeErrorAttributes(request, ex);
        return null;
    }
    
    /**
     * @deprecated since 2.3.0 in favor of
     * {@link #getErrorAttributes(WebRequest, ErrorAttributeOptions)}
     */
    @Deprecated
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        return getErrorAttributes(webRequest, ErrorAttributeOptions.of((ErrorAttributeOptions.Include.STACK_TRACE)));
    }
    
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        final var errorAttributesConstructor = errorAttributesConstructorFactory.get(webRequest, options, this::getError);
        return errorAttributesConstructor.construct();
    }
    
    @Override
    public Throwable getError(WebRequest webRequest) {
        final Throwable exception = (Throwable) getAttribute(webRequest, ERROR_ATTRIBUTE);
        return (exception != null) ? exception : (Throwable) getAttribute(webRequest, RequestDispatcher.ERROR_EXCEPTION);
    }
    
    private void storeErrorAttributes(HttpServletRequest request, Exception ex) {
        request.setAttribute(ERROR_ATTRIBUTE, ex);
    }
    
    private Object getAttribute(RequestAttributes requestAttributes, String name) {
        return requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }
    
}
