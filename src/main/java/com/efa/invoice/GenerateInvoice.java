package com.efa.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateInvoice {
    
    @Valid
    @NotNull
    private GenerateInvoiceAddress billingAddress;
    
    @Valid
    @NotEmpty
    @Size(max = 9, message = "{javax.validation.constraints.Size.NotEmpty.message}")
    private Set<String> orderIds = new HashSet<>();
    
}
