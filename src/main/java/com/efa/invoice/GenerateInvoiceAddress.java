package com.efa.invoice;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class GenerateInvoiceAddress {
    
    @NotBlank
    @Length(min = 1, max = 255)
    private String street1;
    
    @Length(max = 255, message = "{javax.validation.constraints.Length.NotEmpty.message}")
    @Length(min = 1, message = "{javax.validation.constraints.NotEmpty.message}")
    private String street2;
    
    @NotBlank
    @Length(min = 1, max = 3)
    private String stateCode;//ISO 3166-2
    
    @NotBlank
    @Length(min = 1, max = 255)
    private String city;
    
    @NotBlank
    @Length(min = 2, max = 2, message = "{javax.validation.constraints.Length.equal.message}")
    private String countryCode;//ISO 3166-2
    
    @NotBlank
    @Length(min = 1, max = 20)
    private String postcode;
    
    @Getter(AccessLevel.NONE)
    private GenerateInvoiceAddressType addressType;
    
    
}
