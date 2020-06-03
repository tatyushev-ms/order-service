package com.efa.invoice;

import lombok.ToString;
import org.springframework.hateoas.AffordanceModel;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleInputPayloadMetadata implements AffordanceModel.InputPayloadMetadata {
    
    private final SortedMap<String, AffordanceModel.PropertyMetadata> properties;
    
    private SimpleInputPayloadMetadata(AffordanceModel.PropertyMetadata... properties) {
        this.properties = new TreeMap<>(Stream.of(properties).collect(Collectors.toMap(AffordanceModel.PropertyMetadata::getName, Function.identity())));
    }
    
    @Override
    public Stream<AffordanceModel.PropertyMetadata> stream() {
        return properties.values().stream();
    }
    
    @Override
    public <T extends AffordanceModel.PropertyMetadataConfigured<T> & AffordanceModel.Named> T applyTo(T target) {
        final AffordanceModel.PropertyMetadata metadata = properties.get(target.getName());
        return metadata == null ? target : target.apply(metadata);
    }
    
    @Override
    public <T extends AffordanceModel.Named> T customize(T target, Function<AffordanceModel.PropertyMetadata, T> customizer) {
        final AffordanceModel.PropertyMetadata metadata = properties.get(target.getName());
        return metadata == null ? target : customizer.apply(metadata);
    }
    
    @Override
    public List<String> getI18nCodes() {
        return properties.values().stream()
                .map(propertyMetadata -> ((SimplePropertyMetadata) propertyMetadata))
                .map(SimplePropertyMetadata::getI18nCodes)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    public static SimpleInputPayloadMetadataBuilder payload() {
        return new SimpleInputPayloadMetadataBuilder();
    }
    
    @ToString
    public static class SimpleInputPayloadMetadataBuilder {
        
        private AffordanceModel.PropertyMetadata[] properties;
        
        private SimpleInputPayloadMetadataBuilder() {
        }
        
        public SimpleInputPayloadMetadata.SimpleInputPayloadMetadataBuilder properties(final AffordanceModel.PropertyMetadata... properties) {
            this.properties = properties;
            return this;
        }
        
        public SimpleInputPayloadMetadata build() {
            return new SimpleInputPayloadMetadata(this.properties);
        }
        
    }
    
    
}
