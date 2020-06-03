package com.efa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("ErrorAttributesConstructorFactory tests")
class ErrorAttributesConstructorFactoryTest {
    
    @Test
    @DisplayName("Creates an ErrorAttributesConstructor")
    void shouldCreateErrorAttributesConstructor() {
        final ErrorAttributesConstructorFactory factory = new ErrorAttributesConstructorFactory();
        final ErrorAttributesConstructor errorAttributesConstructor = factory.get(null, null, null);
        assertThat(errorAttributesConstructor, is(notNullValue()));
    }
    
}
