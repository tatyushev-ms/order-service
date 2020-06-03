package com.efa;

import com.efa.invoice.NotReadableEnumProblemDescription;

import java.util.List;

public interface InvalidParametersConstructor {
    
    List<InvalidParameter> construct(NotReadableEnumProblemDescription descriptor);
    
}
