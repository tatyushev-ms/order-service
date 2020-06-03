package com.efa.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

@SuppressWarnings({"rawtypes", "unchecked"})
public class StringEnumValueEqualedTo extends TypeSafeMatcher<String> {
    
    private final Class<? extends Enum> valueClass;
    private final Enum<?> value;
    
    public <T extends Enum<T>> StringEnumValueEqualedTo(T value) {
        this.valueClass = value.getClass();
        this.value = value;
    }
    
    @Override
    protected boolean matchesSafely(String item) {
        final Enum<?> enumValue;
        try {
            enumValue = Enum.valueOf(valueClass, item);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return enumValue.equals(value);
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a string representation of ").appendValue(value);
    }
    
    public static <T extends Enum<T>> Matcher<String> enumValueEqualedTo(T value) {
        return new StringEnumValueEqualedTo(value);
    }
    
}
