package com.jm.repository;

import com.jm.entity.Anamnesis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnamnesisRepository extends JpaRepository<Anamnesis, UUID> {
}
