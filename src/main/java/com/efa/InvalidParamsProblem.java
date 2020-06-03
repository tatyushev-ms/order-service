package com.efa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.hateoas.mediatype.problem.Problem;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Getter(onMethod = @__(@JsonProperty))
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvalidParamsProblem extends Problem {
    
    private final List<InvalidParameter> invalidParams;
    
    public InvalidParamsProblem(URI type, String title, int status, String detail, List<InvalidParameter> invalidParams) {
        this(type, title, status, detail, null, invalidParams);
    }
    
    public InvalidParamsProblem(URI type, String title, int status, String detail, URI instance, List<InvalidParameter> invalidParams) {
        super(type, title, status, detail, instance);
        this.invalidParams = invalidParams != null ? invalidParams : Collections.emptyList();
    }
    
}
