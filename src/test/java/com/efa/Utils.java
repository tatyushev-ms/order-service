package com.efa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Utils {
    
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    public static BigDecimal bd(String bigDecimalAsString) {
        return new BigDecimal(bigDecimalAsString);
    }
    
    public static final class Time {
        
        private static LocalDateTime time;
        
        public static LocalDateTime now() {
            time = LocalDateTime.now();
            return time;
        }
        
        public static LocalDateTime lastValue() {
            return time;
        }
        
        private Time() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
        
    }
    
}
