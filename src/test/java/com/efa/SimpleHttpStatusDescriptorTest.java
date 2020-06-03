package com.efa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@DisplayName("SimpleHttpStatusDescriptor tests")
class SimpleHttpStatusDescriptorTest {
    
    /*private final SimpleHttpStatusDescriptor descriptor = new SimpleHttpStatusDescriptor();
    
    @Nested
    @DisplayName("Returns a reason phrase")
    class ReasonPhrase {
        
        @Test
        @DisplayName("Returns a corresponding reason phrase when an error status code is specified")
        void shouldReturnCorrespondingReasonPhraseWhenErrorStatusCodeIsSpecified() {
            assertThat(descriptor.getReasonPhrase(404), is(equalTo(HttpStatus.NOT_FOUND.getReasonPhrase())));
        }
        
        @Test
        @DisplayName("Returns a generic reason phrase when an error status code is missing")
        void shouldReturnGenericReasonPhraseWhenErrorStatusCodeIsMissing() {
            assertThat(descriptor.getReasonPhrase(null), is(equalTo("None")));
        }
        
        @Test
        @DisplayName("Returns a message when an error status code is illegal")
        void shouldReturnCorrespondingReasonPhraseWhenErrorStatusCodeIsIllegal() {
            assertThat(descriptor.getReasonPhrase(666), is(equalTo("Http Status 666")));
        }
        
    }
    
    @Nested
    @DisplayName("Returns a status code")
    class StatusCode {
        
        @Test
        @DisplayName("Returns a corresponding status code when an error status code is specified")
        void shouldReturnCorrespondingStatusCodeWhenErrorStatusCodeIsSpecified() {
            assertThat(descriptor.getStatusCode(404), is(equalTo(404)));
        }
        
        @Test
        @DisplayName("Returns a generic status code when an error status code is missing")
        void shouldReturnGenericStatusCodeWhenErrorStatusCodeIsMissing() {
            assertThat(descriptor.getStatusCode(null), is(equalTo(999)));
        }
        
        @Test
        @DisplayName("Returns a generic status code when an error status code is illegal")
        void shouldReturnGenericStatusCodeWhenErrorStatusCodeIsMissing() {
            assertThat(descriptor.getStatusCode(666), is(equalTo(999)));
        }
        
    }*/
    
}
