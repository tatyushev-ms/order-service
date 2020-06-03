package com.efa.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.time.LocalDateTime;

public class StringDateEqualedTo extends TypeSafeMatcher<String> {
    
    private final LocalDateTime value;
    
    public StringDateEqualedTo(LocalDateTime value) {
        this.value = value;
    }
    
    @Override
    protected boolean matchesSafely(String item) {
        return value.equals(LocalDateTime.parse(item));
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a string representation of ").appendValue(value);
    }
    
    public static Matcher<String> dateEqualedTo(LocalDateTime value) {
        return new StringDateEqualedTo(value);
    }
    
}
