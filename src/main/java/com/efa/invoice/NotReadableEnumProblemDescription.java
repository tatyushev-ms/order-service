package com.efa.invoice;

import com.efa.ProblemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotReadableEnumProblemDescription {
    
    private final ProblemType problemType;
    private final String parameterName;
    private final boolean optional;
    private final Object actualParameterValue;
    private final Object[] validParameterValues;
    
}
