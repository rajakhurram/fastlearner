package com.vinncorp.fast_learner.response.subscription;

import lombok.Data;

@Data
public class NameAndAddressType {
    private String firstName;
    private String lastName;
    private String company;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
}
