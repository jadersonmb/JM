package com.jm.repository;

import com.jm.entity.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnamneseRepository extends JpaRepository<Anamnese, UUID> {
}
