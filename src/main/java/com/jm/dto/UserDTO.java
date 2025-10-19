package com.jm.dto;

import com.jm.entity.Users;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "user")
public class UserDTO {

    private UUID id;
    private String name;
    private String email;
    private String lastName;
    private String documentNumber;
    private String phoneNumber;
    private String street;
    private String state;
    private String postalCode;
    private String locale;
    private UUID cityId;
    private String cityName;
    private UUID countryId;
    private CountryDTO countryDTO;
    private UUID educationLevelId;
    private String educationLevelName;
    private UUID professionId;
    private String professionName;
    private String password;
    private String avatarUrl;
    private Users.Type type;
    private String stripeCustomerId;
    private String asaasCustomerId;
    private LocalDate birthDate;
    private Integer age;
    private String consultationGoal;
    private Boolean firstAccess;
    private Set<RoleDTO> roles;
}
