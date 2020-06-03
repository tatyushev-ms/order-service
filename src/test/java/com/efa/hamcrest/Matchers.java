package com.efa.hamcrest;

import com.efa.hamcrest.core.DoubleBigDecimalWhichIs;
import org.hamcrest.Matcher;

import java.time.LocalDateTime;

public class Matchers {
    
    public static Matcher<Double> doubleWhichIs(Matcher<java.math.BigDecimal> matcher) {
        return DoubleBigDecimalWhichIs.doubleWhichIs(matcher);
    }
    
    public static <T extends Enum<T>> Matcher<String> enumValueEqualedTo(T value) {
        return com.efa.hamcrest.core.StringEnumValueEqualedTo.enumValueEqualedTo(value);
    }
    
    public static Matcher<String> dateEqualedTo(LocalDateTime value) {
        return com.efa.hamcrest.core.StringDateEqualedTo.dateEqualedTo(value);
    }
    
}
