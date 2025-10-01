package com.jm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(
                            name = "uuid_gen_strategy_class",
                            value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String firstName;
    private String lastName;

    private String documentNumber; // e.g. passport, ID card
    private String phoneNumber;
    private String email;

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users user;
}
