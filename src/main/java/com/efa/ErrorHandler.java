package com.efa;

import com.efa.invoice.GenerateInvoiceAddressType;
import com.efa.invoice.InvalidRequestParametersException;
import com.efa.invoice.InvoiceStatus;
import com.efa.invoice.NotReadableEnumProblemDescription;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import java.net.URI;
import java.util.List;

@ControllerAdvice
@AllArgsConstructor
public class ErrorHandler extends ResponseEntityExceptionHandler {
    
    private final InvalidParametersConstructor invalidParametersConstructor;
    
    @ExceptionHandler(InvalidRequestParametersException.class)
    public ResponseEntity<Object> handleInvalidRequestParametersException(InvalidRequestParametersException ex, WebRequest request) {
        final Problem problemDetails = new InvalidParamsProblem(URI.create("https://example.net/validation-error"),
                "Your request parameters didn't validate.", HttpStatus.UNPROCESSABLE_ENTITY.value(), null, ex.getInvalidParameters());
        return handleExceptionInternal(ex, problemDetails, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }
    
    @NonNull
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatus status, @NonNull WebRequest request) {
        if (notReadableEnum(ex)) {
            return handleNotReadableEnum(ex, headers, status, request);
        }
        return handleExceptionInternal(ex, null, headers, status, request);
    }
    
    @NonNull
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, Object body, @NonNull HttpHeaders headers,
                                                             @NonNull HttpStatus status, @NonNull WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        if (body == null) {
            body = Problem.statusOnly(status);
        } else if (!(body instanceof Problem)) {
            logger.warn("response body is not instance of Problem Details but " + body.getClass());
        }
        return new ResponseEntity<>(body, headers, status);
    }
    
    private boolean notReadableEnum(HttpMessageNotReadableException ex) {
        final Throwable cause = ex.getCause();
        if (!(cause instanceof InvalidFormatException)) {
            return false;
        }
        final InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
        final Class<?> targetType = invalidFormatException.getTargetType();
        return targetType.isEnum();
    }
    
    @NonNull
    private ResponseEntity<Object> handleNotReadableEnum(@NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers,
                                                         @NonNull HttpStatus status, @NonNull WebRequest request) {
        final var problemDescription = describeProblem((InvalidFormatException) ex.getCause());
        if (problemDescription == null) {
            return handleExceptionInternal(ex, null, headers, status, request);
        }
        return handleNotReadableEnum(status, problemDescription);
    }
    
    @NonNull
    private ResponseEntity<Object> handleNotReadableEnum(@NonNull HttpStatus status, @NonNull NotReadableEnumProblemDescription problemDescription) {
        final List<InvalidParameter> invalidParameters = invalidParametersConstructor.construct(problemDescription);
        final Problem problemDetails = new InvalidParamsProblem(URI.create("https://example.net/validation-error"),
                "Your request parameters didn't validate.", status.value(), null, invalidParameters);
        return new ResponseEntity<>(problemDetails, status);
    }
    
    private NotReadableEnumProblemDescription describeProblem(InvalidFormatException invalidFormatException) {
        final Class<?> targetType = invalidFormatException.getTargetType();
        if (targetType.isAssignableFrom(GenerateInvoiceAddressType.class)) {
            return new NotReadableEnumProblemDescription(ProblemType.INVOICE_GENERATION_INVALID_PARAMS, "billingAddress.addressType", true,
                    invalidFormatException.getValue(), targetType.getEnumConstants());
        }
        if (targetType.isAssignableFrom(InvoiceStatus.class)) {
            return new NotReadableEnumProblemDescription(ProblemType.INVOICE_STATUS_CHANGE_INVALID_PARAMS, "invoiceStatus", false,
                    invalidFormatException.getValue(), targetType.getEnumConstants());
        }
        return null;
    }
    
}
