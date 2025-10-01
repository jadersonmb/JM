package com.jm.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PersonDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String phoneNumber;
    private String email;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private UUID userId;
}


