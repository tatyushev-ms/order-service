package com.efa.invoice;

import com.efa.InvalidParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvalidRequestParametersAnalyzer {
    
    public List<InvalidParameter> formInvalidParameters(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(fieldError -> new InvalidParameter(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());
    }
    
}
