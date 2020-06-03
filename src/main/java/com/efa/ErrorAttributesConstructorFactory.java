package com.efa;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.web.context.request.WebRequest;

import java.util.function.Function;

class ErrorAttributesConstructorFactory {
    
    public ErrorAttributesConstructor get(WebRequest webRequest,
                                          ErrorAttributeOptions options,
                                          Function<WebRequest, Throwable> errorExtractor) {
        return new ErrorAttributesConstructor(webRequest, options, errorExtractor);
    }
    
}
