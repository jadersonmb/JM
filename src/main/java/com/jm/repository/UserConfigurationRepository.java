package com.jm.repository;

import com.jm.entity.UserConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserConfigurationRepository extends JpaRepository<UserConfiguration, UUID> {
    Optional<UserConfiguration> findByUserId(UUID userId);
}
