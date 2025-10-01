package com.jm.mappers;

import com.jm.dto.PersonDTO;
import com.jm.entity.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    public PersonDTO toDTO(Person entity){
        return PersonDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .city(entity.getCity())
                .email(entity.getEmail())
                .state(entity.getState())
                .country(entity.getCountry())
                .documentNumber(entity.getDocumentNumber())
                .phoneNumber(entity.getPhoneNumber())
                .postalCode(entity.getPostalCode())
                .street(entity.getStreet())
                .userId(entity.getUser().getId())
                .build();
    }

    public Person toEntity(PersonDTO dto){
        return Person.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .city(dto.getCity())
                .email(dto.getEmail())
                .state(dto.getState())
                .country(dto.getCountry())
                .documentNumber(dto.getDocumentNumber())
                .phoneNumber(dto.getPhoneNumber())
                .postalCode(dto.getPostalCode())
                .street(dto.getStreet())
                .build();
    }
}
