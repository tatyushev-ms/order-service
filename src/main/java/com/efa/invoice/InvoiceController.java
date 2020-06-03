package com.efa.invoice;

import com.efa.ProblemType;
import com.efa.address.Address;
import com.efa.address.AddressType;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping("/v1/invoices")
@ExposesResourceFor(Invoice.class)
@AllArgsConstructor
public class InvoiceController {
    // TODO: validation, hateoas, problem details
    
    private final InvoiceService invoiceService;
    
    private final InvalidRequestParametersAnalyzer invalidRequestParametersAnalyzer;
    private final InvoiceRepresentationModelAssembler assembler;
    
    @PostMapping
    public ResponseEntity<EntityModel<Invoice>> post(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal, @Valid @RequestBody GenerateInvoice dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestParametersException(ProblemType.INVOICE_GENERATION_INVALID_PARAMS, invalidRequestParametersAnalyzer.formInvalidParameters(bindingResult));
        }
        final String accountNumber = principal.getAttribute("account_number");
        final Set<String> orderIds = dto.getOrderIds();
        final Address billingAddress = formBillingAddress(dto.getBillingAddress());
        
        final Invoice generatedInvoice = invoiceService.generate(accountNumber, orderIds, billingAddress);
        
        return ResponseEntity
                .created(linkTo(getClass()).slash(generatedInvoice.getId()).toUri())
                .body(assembler.toModel(generatedInvoice));
    }
    
    @GetMapping
    public ResponseEntity<Collection<Invoice>> getCollection(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {// TODO: Page
        final String accountNumber = principal.getAttribute("account_number");
        //throw new UnsupportedOperationException("Method is not implemented yet");
        return ResponseEntity.ok(Collections.emptyList());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> get(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal, @PathVariable String id) {
        final String accountNumber = principal.getAttribute("account_number");
        return invoiceService.findById(accountNumber, id)
                //.map(invoiceRepresentationModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Invoice> patch(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal, @PathVariable String id, @RequestBody Invoice invoice) {
        final String accountNumber = principal.getAttribute("account_number");
        final InvoiceStatus invoiceStatus = invoice.getInvoiceStatus();
        invoiceService.updateInvoiceStatus(accountNumber, id, invoiceStatus);
        return invoiceService.findById(accountNumber, id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
    }
    
    private Address formBillingAddress(GenerateInvoiceAddress billingAddress) {
        return new Address(billingAddress.getStreet1(),
                billingAddress.getStreet2(),
                billingAddress.getStateCode(),
                billingAddress.getCity(),
                billingAddress.getCountryCode(),
                billingAddress.getPostcode(),
                AddressType.BILLING);
    }
    
}
