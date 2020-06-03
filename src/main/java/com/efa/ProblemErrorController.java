package com.efa;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Global error {@link Controller @Controller}, rendering {@link ErrorAttributes}.
 * More specific errors can be handled either using Spring MVC abstractions (e.g.
 * {@code @ExceptionHandler}) or by adding servlet.
 * {@link AbstractServletWebServerFactory#setErrorPages server error pages}.
 * Similar to {@link org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController}, but
 * returns {@link Problem Problem Details}.
 * Deliberately does not support pages.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7807">Problem Details for HTTP APIs</a>
 * @see ProblemDetailsBuilder
 * @see ErrorProperties
 */
@RequestMapping("${server.error.path:${error.path:/error}}")
@AllArgsConstructor
public class ProblemErrorController implements ErrorController {
    
    private final ProblemDetailsBuilder errorAttributes;
    private final ErrorProperties errorProperties;
    
    /**
     * @deprecated since 2.3.0 in favor of setting the property `server.error.path`
     */
    @Deprecated
    @Override
    public String getErrorPath() {
        throw new UnsupportedOperationException("deprecated since 2.3.0 in favor of setting the property `server.error.path`");
    }
    
    @RequestMapping
    public ResponseEntity<Problem> error(HttpServletRequest request) {
        final HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
        final Problem problemDetails = getProblemDetails(request, getErrorAttributeOptions(request));
        return new ResponseEntity<>(problemDetails, status);
    }
    
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Void> mediaTypeNotAcceptable(HttpServletRequest request) {
        final HttpStatus status = getStatus(request);
        return new ResponseEntity<>(status);
    }
    
    private HttpStatus getStatus(HttpServletRequest request) {
        final Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    private ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request) {
        final EnumSet<ErrorAttributeOptions.Include> includes = EnumSet.noneOf(ErrorAttributeOptions.Include.class);
        if (errorProperties.isIncludeException()) {
            includes.add(ErrorAttributeOptions.Include.EXCEPTION);
        }
        if (isIncludeStackTrace(request)) {
            includes.add(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        if (isIncludeMessage(request)) {
            includes.add(ErrorAttributeOptions.Include.MESSAGE);
        }
        if (isIncludeBindingErrors(request)) {
            includes.add(ErrorAttributeOptions.Include.BINDING_ERRORS);
        }
        return ErrorAttributeOptions.of(includes);
    }
    
    private Problem getProblemDetails(HttpServletRequest request, ErrorAttributeOptions options) {
        final WebRequest webRequest = new ServletWebRequest(request);
        return errorAttributes.getProblemDetails(webRequest, options);
    }
    
    private boolean isIncludeStackTrace(HttpServletRequest request) {
        switch (errorProperties.getIncludeStacktrace()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
            case ON_TRACE_PARAM:
                return getTraceParameter(request);
            default:
                return false;
        }
    }
    
    private boolean isIncludeMessage(HttpServletRequest request) {
        switch (errorProperties.getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return getMessageParameter(request);
            default:
                return false;
        }
    }
    
    private boolean isIncludeBindingErrors(HttpServletRequest request) {
        switch (errorProperties.getIncludeMessage()) {
            case ALWAYS:
                return true;
            case ON_PARAM:
                return getErrorsParameter(request);
            default:
                return false;
        }
    }
    
    private boolean getTraceParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "trace");
    }
    
    private boolean getMessageParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "message");
    }
    
    private boolean getErrorsParameter(HttpServletRequest request) {
        return getBooleanParameter(request, "errors");
    }
    
    /**
     * @return whether parameter is present and equals exactly to {@code true}.
     */
    private boolean getBooleanParameter(HttpServletRequest request, String parameterName) {
        return Optional.ofNullable(request.getParameter(parameterName))
                .map("true"::equals)
                .orElse(false);
    }
    
}
