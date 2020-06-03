package com.efa.invoice;

import lombok.AllArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.efa.invoice.SimpleInputPayloadMetadata.payload;
import static com.efa.invoice.SimplePropertyMetadata.property;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@AllArgsConstructor
public class InvoiceRepresentationModelProcessor implements RepresentationModelProcessor<EntityModel<Invoice>> {
    
    private final EntityLinks entityLinks;
    
    @SafeVarargs
    public static <T extends Enum<T>> String oneOf(T... enumValues) {
        return Stream.of(enumValues).map(Enum::name).collect(Collectors.joining("|", "^(", ")$"));
    }
    
    @SafeVarargs
    public static <T extends Enum<T>> List<String> enumValues(T... enumValues) {
        return Stream.of(enumValues).map(e -> e.getClass() + "." + e.name()).collect(Collectors.toList());
    }
    
    @NonNull
    @Override
    public EntityModel<Invoice> process(@NonNull EntityModel<Invoice> model) {
        final Invoice invoice = model.getContent();
        
        final Link linkToItemResource = entityLinks.linkToItemResource(Invoice.class, invoice.getId()).withSelfRel();
        final Link linkToCollectionResource = entityLinks.linkToCollectionResource(Invoice.class);
        
        final Affordances affordances;
        final InvoiceStatus invoiceStatus = invoice.getInvoiceStatus();
        if (invoiceStatus == InvoiceStatus.CREATED) {
            affordances = addPayAffordance(linkToItemResource);
        } else if (invoiceStatus == InvoiceStatus.SENT) {
            affordances = addSendAffordance(linkToItemResource);
        } else {
            affordances = Affordances.of(linkToItemResource);
        }
        
        model.add(affordances.toLink());
        model.add(linkToCollectionResource.withRel(IanaLinkRelations.COLLECTION).andAffordance(afford(methodOn(InvoiceController.class).getCollection(null))));
        return model;
    }
    
    private Affordances addPayAffordance(Link linkToItemResource) {
        return Affordances.of(linkToItemResource)
                .afford(HttpMethod.PATCH)
                .withInput(payload().properties(
                        property().name("invoiceStatus").required(true)
                                .pattern(oneOf(InvoiceStatus.PAID, InvoiceStatus.SENT))
                                .i18nCodes(enumValues(InvoiceStatus.PAID, InvoiceStatus.SENT))
                                .type(InvoiceStatus.class)
                                .build())
                        .build())
                .withName("pay")
                .build();
    }
    
    private Affordances addSendAffordance(Link linkToItemResource) {
        return Affordances.of(linkToItemResource)
                .afford(HttpMethod.PATCH)
                .withInput(payload().properties(
                        property().name("invoiceStatus").required(true)
                                .pattern(oneOf(InvoiceStatus.SENT))
                                .i18nCodes(enumValues(InvoiceStatus.PAID, InvoiceStatus.SENT))
                                .type(InvoiceStatus.class)
                                .build())
                        .build())
                .withName("send")
                .build();
    }
    
}
