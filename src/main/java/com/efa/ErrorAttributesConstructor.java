package com.efa;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Method Object of {@link org.springframework.boot.web.servlet.error.DefaultErrorAttributes#getErrorAttributes(WebRequest, ErrorAttributeOptions)
 * DefaultErrorAttributes#getErrorAttributes(WebRequest, ErrorAttributeOptions)}.
 */
@RequiredArgsConstructor
class ErrorAttributesConstructor {
    
    private final WebRequest webRequest;
    private final ErrorAttributeOptions options;
    private final Function<WebRequest, Throwable> errorExtractor;
    
    Map<String, Object> response;
    
    public Map<String, Object> construct() {
        response = new LinkedHashMap<>();
        
        addTimestamp();
        addStatus();
        addErrorDetails();
        addPath();
        
        return response;
    }
    
    private void addTimestamp() {
        response.put("timestamp", new Date());
    }
    
    private void addStatus() {
        final Integer status = (Integer) getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            final HttpStatus httpStatus = HttpStatus.resolve(status);
            if (httpStatus != null) {
                response.put("status", httpStatus.value());
                response.put("error", httpStatus.getReasonPhrase());
                return;
            }
        }
        response.put("status", 999);
        response.put("error", "None");
    }
    
    private void addErrorDetails() {
        final Throwable error = getError();
        if (error != null) {
            addErrorDetails(error);
            return;
        }
        response.put("message", options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) ? getExceptionErrorMessage() : "");
    }
    
    private Throwable getError() {
        return errorExtractor.apply(webRequest);
    }
    
    private void addPath() {
        final Object path = getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (path != null) {
            response.put("path", path);
        }
    }
    
    private Object getAttribute(String name) {
        return webRequest.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }
    
    private void addErrorDetails(Throwable error) {
        final Throwable rootCause = getRootCause(error);
        if (options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
            response.put("exception", rootCause.getClass().getName());
        }
        
        if (options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
            response.put("trace", getStackTrace(rootCause));
        }
        final BindingResult result = extractBindingResult(rootCause);
        if (result != null) {
            response.put("message", options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) ? getBindingResultErrorMessage(result) : "");
            if (options.isIncluded(ErrorAttributeOptions.Include.BINDING_ERRORS)) {
                response.put("errors", result.getAllErrors());
            }
            return;
        }
        response.put("message", options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) ? getExceptionErrorMessage(rootCause) : "");
    }
    
    private Object getExceptionErrorMessage() {
        final Object message = getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (StringUtils.isEmpty(message)) {
            return "No message available";
        }
        return message;
    }
    
    private Throwable getRootCause(Throwable error) {
        while (error instanceof ServletException && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }
    
    private String getStackTrace(Throwable error) {
        final StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        stackTrace.flush();
        return stackTrace.toString();
    }
    
    private BindingResult extractBindingResult(Throwable error) {
        if (error instanceof BindingResult) {
            return (BindingResult) error;
        }
        if (error instanceof MethodArgumentNotValidException) {
            return ((MethodArgumentNotValidException) error).getBindingResult();
        }
        return null;
    }
    
    private String getBindingResultErrorMessage(BindingResult result) {
        return "Validation failed for object='" + result.getObjectName() + "'. Error count: " + result.getErrorCount();
    }
    
    private Object getExceptionErrorMessage(Throwable error) {
        Object message = getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (StringUtils.isEmpty(message)) {
            message = error.getMessage();
        }
        if (StringUtils.isEmpty(message)) {
            message = "No message available";
        }
        return message;
    }
    
}
