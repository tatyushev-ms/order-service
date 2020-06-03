package com.efa;

import com.efa.invoice.NotReadableEnumProblemDescription;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
public class SimpleInvalidParametersConstructor implements InvalidParametersConstructor {
    
    @Override
    public List<InvalidParameter> construct(NotReadableEnumProblemDescription descriptor) {
        final List<InvalidParameter> invalidParameters = new LinkedList<>();
        if (String.valueOf(descriptor.getActualParameterValue()).isEmpty()) {
            invalidParameters.add(new InvalidParameter(descriptor.getParameterName(), (descriptor.isOptional() ? "if present, " : "") + "must not be empty"));
        }
        final StringBuilder reason = new StringBuilder();
        if (descriptor.isOptional()) {
            reason.append("if present, ");
        }
        final Object[] validParameterValues = descriptor.getValidParameterValues();
        if (validParameterValues.length == 1) {
            reason.append("must be ").append(validParameterValues[0]);
        } else {
            reason.append("must be one of ").append(Arrays.toString(validParameterValues));
        }
        invalidParameters.add(new InvalidParameter(descriptor.getParameterName(), reason.toString()));
        return invalidParameters;
    }
    
}
