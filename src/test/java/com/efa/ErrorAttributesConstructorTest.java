package com.efa;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.ServletException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("ErrorAttributesConstructor tests")
class ErrorAttributesConstructorTest {
    
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final WebRequest webRequest = new ServletWebRequest(request);
    private final MockErrorExtractor mockErrorExtractor = new MockErrorExtractor(webRequest);
    
    private Map<String, Object> result;
    
    @Test
    @DisplayName("Puts timestamp")
    void shouldPutTimestamp() {
        //when
        constructWithDefaults();
        
        //then
        assertThat(valueOf("timestamp"), is(instanceOf(Date.class)));
    }
    
    @Nested
    @DisplayName("Handles the \"status\" attribute")
    class StatusAttribute {
        
        @Test
        @DisplayName("Puts an error and status when an error status code is exist")
        void shouldPutCorrespondingErrorAndStatusWhenErrorStatusCodeIsExist() {
            //given
            requestHasAttribute("javax.servlet.error.status_code", 404);
            
            //when
            constructWithDefaults();
            
            //then
            assertThat(valueOf("error"), is(equalTo("Not Found")));
            assertThat(valueOf("status"), is(equalTo(404)));
        }
        
        @Test
        @DisplayName("Puts a generic error and status when an error status code is not exist")
        void shouldPutCorrespondingErrorAndStatusWhenErrorStatusCodeIsNotExist() {
            //when
            constructWithDefaults();
            
            //then
            assertThat(valueOf("error"), is(equalTo("None")));
            assertThat(valueOf("status"), is(equalTo(999)));
        }
        
        @Test
        @DisplayName("Puts a generic error and status when an error status code is unknown")
        void shouldPutCorrespondingErrorAndStatusWhenErrorStatusCodeIsUnknown() {
            //given
            requestHasAttribute("javax.servlet.error.status_code", 777);
            
            //when
            constructWithDefaults();
            
            //then
            assertThat(valueOf("error"), is(equalTo("None")));
            assertThat(valueOf("status"), is(equalTo(999)));
        }
        
    }
    
    @Test
    @DisplayName("Puts an exception message when asked and the exception is exist")
    void shouldPutExceptionMessageWhenAsked() {
        //given
        errorExtractorWillReturn(new RuntimeException("Test"));
        
        //when
        constructWith(Include.MESSAGE);
        
        //then
        assertThat(field("exception"), is(absent()));
        assertThat(valueOf("message"), is(equalTo("Test")));
    }
    
    @Test
    @DisplayName("Does not put an exception message when not asked")
    void shouldNotPutExceptionMessageWhenNotAsked() {
        //given
        errorExtractorWillReturn(new RuntimeException("Test"));
        
        //when
        constructWithDefaults();
        
        //then
        assertThat(field("exception"), is(absent()));
        assertThat((String) valueOf("message"), is(emptyString()));
    }
    
    @Test
    @DisplayName("Puts an error message when asked and the error message is exist")
    void shouldPutErrorMessageWhenAsked() {
        //given
        requestHasAttribute("javax.servlet.error.message", "Test");
        
        //when
        constructWith(Include.MESSAGE);
        
        //then
        assertThat(field("exception"), is(absent()));
        assertThat(valueOf("message"), is(equalTo("Test")));
    }
    
    @Test
    @DisplayName("Does not put an error message when not asked")
    void shouldNotPutErrorMessageWhenNotAsked() {
        //given
        requestHasAttribute("javax.servlet.error.message", "Test");
        
        //when
        constructWithDefaults();
        
        //then
        assertThat(field("exception"), is(absent()));
        assertThat((String) valueOf("message"), is(emptyString()));
    }
    
    @Nested
    @DisplayName("Puts a dummy message when asked to put a message but there is no message")
    class DummyMessages {
        
        @Test
        @DisplayName("because there is no exception nor error message")
        void shouldPutDummyMessageWhenAskedToPutMessageButThereIsNoExceptionNorErrorMessage() {
            //when
            constructWith(Include.MESSAGE);
            
            //then
            assertThat(field("exception"), is(absent()));
            assertThat(valueOf("message"), is(equalTo("No message available")));
        }
        
        @Test
        @DisplayName("because an exception has no message")
        void shouldPutDummyMessageWhenAskedToPutMessageButExceptionHasNoMessage() {
            //given
            errorExtractorWillReturn(new RuntimeException());
            
            //when
            constructWith(Include.MESSAGE);
            
            //then
            assertThat(field("exception"), is(absent()));
            assertThat(valueOf("message"), is(equalTo("No message available")));
        }
        
        @Test
        @DisplayName("because an error message is empty")
        void shouldPutDummyMessageWhenAskedToPutMessageButErrorMessageIsEmpty() {
            //given
            requestHasAttribute("javax.servlet.error.message", "");
            
            //when
            constructWith(Include.MESSAGE);
            
            //then
            assertThat(field("exception"), is(absent()));
            assertThat(valueOf("message"), is(equalTo("No message available")));
        }
        
        @Test
        @DisplayName("because an error message is null")
        void shouldPutDummyMessageWhenAskedToPutMessageButErrorMessageIsNull() {
            //given
            requestHasAttribute("javax.servlet.error.message", null);
            
            //when
            constructWith(Include.MESSAGE);
            
            //then
            assertThat(field("exception"), is(absent()));
            assertThat(valueOf("message"), is(equalTo("No message available")));
        }
        
    }
    
    @Test
    @DisplayName("Unwraps servlet exceptions")
    void shouldUnwrapServletException() {
        //given
        errorExtractorWillReturn(new ServletException(new ServletException(new RuntimeException("Test"))));
        
        //when
        constructWith(Include.MESSAGE);
        
        //then
        assertThat(field("exception"), is(absent()));
        assertThat(valueOf("message"), is(equalTo("Test")));
    }
    
    @Test
    @DisplayName("Puts an message when asked and there is an error")
    void shouldPutMessageWhenAskedAndErrorIsExist() {
        //given
        errorExtractorWillReturn(new OutOfMemoryError("Test error"));
        
        //when
        constructWith(Include.MESSAGE);
        
        //then
        assertThat(field("exception"), is(absent()));
        assertThat(valueOf("message"), is(equalTo("Test error")));
    }
    
    @Nested
    @DisplayName("Handles binding errors")
    class BindingErrors {
        
        private final BindingResult bindingResult;
        
        {
            bindingResult = new MapBindingResult(Collections.singletonMap("a", "b"), "objectName");
            bindingResult.addError(new ObjectError("c", "d"));
        }
        
        @Test
        @DisplayName("Puts a corresponding message and errors when asked and the exception is exist")
        void shouldPutCorrespondingMessageAndErrorsWhenAsked() {
            //given
            errorExtractorWillReturn(new BindException(bindingResult));
            
            //when
            constructWith(Include.MESSAGE, Include.BINDING_ERRORS);
            
            //then
            assertThat(valueOf("message"), is(equalTo("Validation failed for object='objectName'. Error count: 1")));
            assertThat(valueOf("errors"), is(equalTo(bindingResult.getAllErrors())));
        }
        
        @Test
        @DisplayName("Does not put a corresponding message and errors when not asked")
        void shouldNotPutCorrespondingMessageAndErrorsWhenNotAsked() {
            //given
            errorExtractorWillReturn(new BindException(bindingResult));
            
            //when
            constructWithDefaults();
            
            //then
            assertThat(valueOf("message"), is(equalTo("")));
            assertThat(field("errors"), is(absent()));
        }
        
        @Test
        @DisplayName("Handles a MethodArgumentNotValidException")
        void shouldHandleMethodArgumentNotValidException() {
            //given
            final Method method = ReflectionUtils.findMethod(String.class, "substring", int.class);
            final MethodParameter parameter = new MethodParameter(method, 0);
            errorExtractorWillReturn(new MethodArgumentNotValidException(parameter, bindingResult));
            
            //when
            constructWith(Include.MESSAGE, Include.BINDING_ERRORS);
            
            //then
            assertThat(valueOf("message"), is(equalTo("Validation failed for object='objectName'. Error count: 1")));
            assertThat(valueOf("errors"), is(equalTo(bindingResult.getAllErrors())));
        }
        
    }
    
    @Nested
    @DisplayName("Handles the \"exception\" attribute")
    class ExceptionAttribute {
        
        @Test
        @DisplayName("Puts an exception when asked and the exception is exist")
        void shouldPutStackTraceWhenAsked() {
            //given
            errorExtractorWillReturn(new RuntimeException("Test"));
            
            //when
            constructWith(Include.EXCEPTION, Include.MESSAGE);
            
            //then
            assertThat(valueOf("exception"), is(equalTo(RuntimeException.class.getName())));
            assertThat(valueOf("message"), is(equalTo("Test")));
        }
        
        @Test
        @DisplayName("Does not put an exception when not asked")
        void shouldNotPutStackTraceWhenNotAsked() {
            //given
            errorExtractorWillReturn(new RuntimeException("Test"));
            
            //when
            constructWithDefaults();
            
            //then
            assertThat(field("exception"), is(absent()));
        }
        
        @Test
        @DisplayName("Does not put an exception when asked but an exception is not exist")
        void shouldNotPutStackTraceWhenAskedButThereIsNoException() {
            //when
            constructWith(Include.EXCEPTION, Include.MESSAGE);
            
            //then
            assertThat(field("exception"), is(absent()));
        }
        
    }
    
    @Nested
    @DisplayName("Handles the \"trace\" attribute")
    class TraceAttribute {
        
        @Test
        @DisplayName("Puts a stack trace when asked and the exception is exist")
        void shouldPutStackTraceWhenAsked() {
            //given
            errorExtractorWillReturn(new RuntimeException("Test"));
            
            //when
            constructWith(Include.STACK_TRACE);
            
            //then
            assertThat((String) valueOf("trace"), startsWith("java.lang"));
        }
        
        @Test
        @DisplayName("Does not put a stack trace when not asked")
        void shouldNotPutStackTraceWhenNotAsked() {
            //given
            errorExtractorWillReturn(new RuntimeException("Test"));
            
            //when
            constructWithDefaults();
            
            //then
            assertThat(field("trace"), is(absent()));
        }
        
        @Test
        @DisplayName("Does not put a stack trace when asked but an exception is not exist")
        void shouldNotPutStackTraceWhenAskedButThereIsNoException() {
            //when
            constructWith(Include.STACK_TRACE);
            
            //then
            assertThat(field("trace"), is(absent()));
        }
        
    }
    
    @Nested
    @DisplayName("Handles the \"path\" attribute")
    class PathAttribute {
        
        @Test
        @DisplayName("Puts a path when an error request uri is exist")
        void shouldPutPathWhenErrorRequestUriIsExist() {
            //given
            requestHasAttribute("javax.servlet.error.request_uri", "path");
            
            //when
            constructWithDefaults();
            
            //then
            assertThat(valueOf("path"), is(equalTo("path")));
        }
        
        @Test
        @DisplayName("Does not put a path when an error request uri is not exist")
        void shouldNotPutPathWhenErrorRequestUriIsNotExist() {
            //when
            constructWithDefaults();
            
            //then
            assertThat(field("path"), is(absent()));
        }
        
    }
    
    private void requestHasAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }
    
    private void errorExtractorWillReturn(Throwable throwable) {
        mockErrorExtractor.setReturnValue(throwable);
    }
    
    private void constructWithDefaults() {
        final var constructor = new ErrorAttributesConstructor(webRequest, ErrorAttributeOptions.defaults(), mockErrorExtractor);
        result = constructor.construct();
    }
    
    private void constructWith(Include atLeastOne, Include... others) {
        final var constructor = new ErrorAttributesConstructor(webRequest, ErrorAttributeOptions.of(EnumSet.of(atLeastOne, others)), mockErrorExtractor);
        result = constructor.construct();
    }
    
    private Object valueOf(String key) {
        return result.get(key);
    }
    
    private boolean field(String key) {
        return result.containsKey(key);
    }
    
    private static Matcher<Boolean> absent() {
        return is(false);
    }
    
    private static class MockErrorExtractor implements Function<WebRequest, Throwable> {
        
        private final WebRequest webRequest;
        private Throwable result = null;
        
        private MockErrorExtractor(WebRequest webRequest) {
            this.webRequest = webRequest;
        }
        
        public void setReturnValue(Throwable result) {
            this.result = result;
        }
        
        @Override
        public Throwable apply(WebRequest webRequest) {
            assertThat(webRequest, is(this.webRequest));
            return result;
        }
        
    }
    
}
