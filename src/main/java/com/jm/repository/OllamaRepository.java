package com.jm.repository;

import com.jm.entity.Ollama;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OllamaRepository extends JpaRepository<Ollama, UUID>, JpaSpecificationExecutor<Ollama> {
}
