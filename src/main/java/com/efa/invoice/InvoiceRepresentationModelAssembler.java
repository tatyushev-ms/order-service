package com.efa.invoice;

import lombok.AllArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InvoiceRepresentationModelAssembler implements RepresentationModelAssembler<Invoice, EntityModel<Invoice>> {// TODO: PagedResourcesAssembler
    
    @NonNull
    @Override
    public EntityModel<Invoice> toModel(@NonNull Invoice invoice) {
        return EntityModel.of(invoice);
    }
    
}
