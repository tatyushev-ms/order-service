package com.efa.invoice;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimplePropertyMetadata implements AffordanceModel.PropertyMetadata {
    
    private final String name;
    private final boolean required;
    private final boolean readOnly;
    private final String pattern;
    private final ResolvableType type;
    private final List<String> i18nCodes;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isRequired() {
        return required;
    }
    
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
    
    @Override
    public Optional<String> getPattern() {
        return Optional.ofNullable(pattern);
    }
    
    @Override
    public ResolvableType getType() {
        return type;
    }
    
    public List<String> getI18nCodes() {
        return i18nCodes;
    }
    
    public static SimplePropertyMetadataBuilder property() {
        return new SimplePropertyMetadataBuilder();
    }
    
    @ToString
    public static class SimplePropertyMetadataBuilder {
        
        private String name;
        private boolean required;
        private boolean readOnly;
        private String pattern;
        private ResolvableType type;
        private List<String> i18nCodes;
        
        private SimplePropertyMetadataBuilder() {
        }
        
        public SimplePropertyMetadataBuilder name(final String name) {
            this.name = name;
            return this;
        }
        
        public SimplePropertyMetadataBuilder required(final boolean required) {
            this.required = required;
            return this;
        }
        
        public SimplePropertyMetadataBuilder readOnly(final boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }
        
        public SimplePropertyMetadataBuilder pattern(final String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public SimplePropertyMetadataBuilder type(@Nullable final Class<?> clazz) {
            this.type = ResolvableType.forClass(clazz);
            return this;
        }
        
        public SimplePropertyMetadataBuilder i18nCodes(final List<String> i18nCodes) {
            this.i18nCodes = i18nCodes;
            return this;
        }
        
        public SimplePropertyMetadata build() {
            return new SimplePropertyMetadata(this.name, this.required, this.readOnly, this.pattern, this.type, this.i18nCodes);
        }
        
    }
    
}
