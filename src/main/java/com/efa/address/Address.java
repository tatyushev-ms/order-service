package com.efa.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    private String street1;
    
    private String street2;
    
    private String stateCode;
    
    private String city;
    
    private String countryCode;
    
    private String postcode;
    
    private AddressType addressType;
    
    public Address(String street1, String street2, String stateCode, String city, String countryCode, String postcode) {
        this.street1 = street1;
        this.street2 = street2;
        this.stateCode = stateCode;
        this.city = city;
        this.countryCode = countryCode;
        this.postcode = postcode;
    }
    
}
