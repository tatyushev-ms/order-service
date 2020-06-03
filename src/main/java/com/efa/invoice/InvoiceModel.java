package com.efa.invoice;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Getter(onMethod_ = {@JsonUnwrapped, @NonNull})
@EqualsAndHashCode(callSuper = false)
public class InvoiceModel extends RepresentationModel<InvoiceModel> {
    
    private final Invoice content;
    
    @NonNull
    @Override
    public String toString() {
        return String.format("Resource { content: %s, %s }", getContent(), super.toString());
    }
    
}
