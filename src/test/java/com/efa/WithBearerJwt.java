package com.efa;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * todo: add Javadoc
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithBearerJwtSecurityContextFactory.class)
public @interface WithBearerJwt {
    
    Attribute[] attributes() default {};
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Attribute {
        
        String name();
        
        String value();
        
    }
    
}
