package com.efa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SimpleProblemErrorAttributes tests")
class SimpleProblemErrorAttributesTest {
    
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final WebRequest webRequest = new ServletWebRequest(request);
    private final ErrorAttributeOptions options = ErrorAttributeOptions.of();
    private final Map<String, Object> expectedAttributes = Collections.emptyMap();
    
    @Mock
    private ErrorAttributesConstructor errorAttributesConstructor;
    
    @Mock
    private ErrorAttributesConstructorFactory constructorFactory;
    @InjectMocks
    private SimpleProblemErrorAttributes errorAttributes;
    
    @Test
    @DisplayName("Returns null as view")
    void shouldReturnNullAsView() {
        //given
        final RuntimeException expectedError = new RuntimeException("Test");
        
        //when
        final ModelAndView modelAndView = errorAttributes.resolveException(request, null, null, expectedError);
        
        //then
        assertThat(modelAndView, is(nullValue()));
    }
    
    @Test
    @DisplayName("Returns a servlet error")
    void shouldReturnServletError() {
        //given
        final RuntimeException expectedError = new RuntimeException("Test");
        request.setAttribute("javax.servlet.error.exception", expectedError);
        
        //when
        final Throwable error = errorAttributes.getError(webRequest);
        
        //then
        assertThat(error, is(expectedError));
    }
    
    @Test
    @DisplayName("Returns a mvc error")
    void shouldReturnMvcError() {
        //given
        final RuntimeException expectedError = new RuntimeException("Test");
        errorAttributes.resolveException(request, null, null, expectedError);
        
        //when
        final Throwable error = errorAttributes.getError(webRequest);
        
        //then
        assertThat(error, is(expectedError));
    }
    
    @Test
    @DisplayName("Prefers a mvc error to a servlet error")
    void shouldPreferMvcErrorToServletError() {
        //given
        final RuntimeException expectedError = new RuntimeException("Test");
        errorAttributes.resolveException(request, null, null, expectedError);
        request.setAttribute("javax.servlet.error.exception", new RuntimeException("Ignored"));
        
        //when
        final Throwable error = errorAttributes.getError(webRequest);
        
        //then
        assertThat(error, is(expectedError));
    }
    
    @Test
    @DisplayName("Returns error attributes")
    void shouldReturnErrorAttributes() {
        //given
        given(constructorFactory.get(eq(webRequest), eq(options), any())).willReturn(errorAttributesConstructor);
        given(errorAttributesConstructor.construct()).willReturn(expectedAttributes);
        
        //when
        final Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest, options);
        
        //then
        assertThat(attributes, is(expectedAttributes));
    }
    
}
