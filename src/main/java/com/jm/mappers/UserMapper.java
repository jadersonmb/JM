package com.jm.mappers;

import com.jm.dto.UserDTO;
import com.jm.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private CountryMapper countryMapper = new CountryMapperImpl() {
    };

    public UserDTO toDTO(Users entity) {
        if (entity == null) {
            return null;
        }
        return UserDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .lastName(entity.getLastName())
                .city(entity.getCity())
                .documentNumber(entity.getDocumentNumber())
                .phoneNumber(entity.getPhoneNumber())
                .postalCode(entity.getPostalCode())
                .state(entity.getState())
                .street(entity.getStreet())
                .type(entity.getType())
                .avatarUrl(entity.getAvatarUrl())
                .stripeCustomerId(entity.getStripeCustomerId())
                .asaasCustomerId(entity.getAsaasCustomerId())
                .birthDate(entity.getBirthDate())
                .age(entity.getAge())
                .education(entity.getEducation())
                .occupation(entity.getOccupation())
                .consultationGoal(entity.getConsultationGoal())
                .countryDTO(countryMapper.toDTO(entity.getCountry()))
                .build();
    }

    public Users toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        return Users.builder()
                .id(userDTO.getId())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .lastName(userDTO.getLastName())
                .city(userDTO.getCity())
                .documentNumber(userDTO.getDocumentNumber())
                .phoneNumber(userDTO.getPhoneNumber())
                .postalCode(userDTO.getPostalCode())
                .state(userDTO.getState())
                .street(userDTO.getStreet())
                .password(userDTO.getPassword())
                .type(userDTO.getType())
                .avatarUrl(userDTO.getAvatarUrl())
                .stripeCustomerId(userDTO.getStripeCustomerId())
                .asaasCustomerId(userDTO.getAsaasCustomerId())
                .birthDate(userDTO.getBirthDate())
                .age(userDTO.getAge())
                .education(userDTO.getEducation())
                .occupation(userDTO.getOccupation())
                .consultationGoal(userDTO.getConsultationGoal())
                .build();
    }

    public Users toUpdate(Users entity) {
        entity.setName(entity.getName());
        entity.setHashCode(entity.getHashCode());
        entity.setLastName(entity.getLastName());
        entity.setCity(entity.getCity());
        entity.setCountry(entity.getCountry());
        entity.setDocumentNumber(entity.getDocumentNumber());
        entity.setPhoneNumber(entity.getPhoneNumber());
        entity.setPostalCode(entity.getPostalCode());
        entity.setState(entity.getState());
        entity.setStreet(entity.getStreet());
        entity.setAvatarUrl(entity.getAvatarUrl());
        return entity;
    }
}