package com.efa.invoice;

import com.efa.InvalidParameter;
import com.efa.ProblemType;

import java.util.List;

public class InvalidRequestParametersException extends RuntimeException {
    
    private final ProblemType invoiceGenerationInvalidParams;
    private final List<InvalidParameter> invalidParameters;
    
    public InvalidRequestParametersException(ProblemType problemType, List<InvalidParameter> invalidParameters) {
        super(problemType.name());
        this.invoiceGenerationInvalidParams = problemType;
        this.invalidParameters = invalidParameters;
    }
    
    public ProblemType getInvoiceGenerationInvalidParams() {
        return invoiceGenerationInvalidParams;
    }
    
    public List<InvalidParameter> getInvalidParameters() {
        return invalidParameters;
    }
    
}
