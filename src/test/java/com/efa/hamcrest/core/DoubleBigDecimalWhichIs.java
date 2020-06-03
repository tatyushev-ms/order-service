package com.efa.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;

public class DoubleBigDecimalWhichIs extends TypeSafeMatcher<Double> {
    
    private final Matcher<BigDecimal> matcher;
    
    public DoubleBigDecimalWhichIs(Matcher<BigDecimal> matcher) {
        this.matcher = matcher;
    }
    
    @Override
    protected boolean matchesSafely(Double item) {
        return matcher.matches(new BigDecimal(item));
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a double value, which is ").appendDescriptionOf(matcher);
    }
    
    @Override
    public void describeMismatchSafely(Double item, Description mismatchDescription) {
        matcher.describeMismatch(new BigDecimal(item), mismatchDescription);
    }
    
    public static Matcher<Double> doubleWhichIs(Matcher<java.math.BigDecimal> matcher) {
        return new DoubleBigDecimalWhichIs(matcher);
    }
    
}
