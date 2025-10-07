package com.jm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_entity")
public class Users {

        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
                        @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy") })
        @Column(name = "id", updatable = false, nullable = false)
        private UUID id;

        private String name;
        private String lastName;
        private String email;
        private int hashCode;

        private String password;
        @Enumerated(EnumType.STRING)
        private Type type;

        private String documentNumber;
        private String phoneNumber;

        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String avatarUrl;

        private String stripeCustomerId;
        private String asaasCustomerId;

        private LocalDate birthDate;
        private Integer age;
        private String education;
        private String occupation;
        @Column(length = 1000)
        private String consultationGoal;

        @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
        private List<Image> imagens = new ArrayList<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Anamnese> anamneses = new ArrayList<>();

        public enum Type {
                ADMIN, CLIENT;
        }
}
