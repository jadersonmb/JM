package com.jm.mappers;

import com.jm.dto.OllamaDTO;
import com.jm.entity.Ollama;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OllamaMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "model", source = "model")
    @Mapping(target = "prompt", source = "prompt")
    @Mapping(target = "response", source = "response")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "startedAt", source = "startedAt")
    @Mapping(target = "finishedAt", source = "finishedAt")
    @Mapping(target = "elapsedMs", source = "elapsedMs")
    @Mapping(target = "requestedBy", source = "requestedBy")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "metadata", source = "metadata")
    OllamaDTO toDTO(Ollama entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "model", source = "model")
    @Mapping(target = "prompt", source = "prompt")
    @Mapping(target = "response", source = "response")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "startedAt", source = "startedAt")
    @Mapping(target = "finishedAt", source = "finishedAt")
    @Mapping(target = "elapsedMs", source = "elapsedMs")
    @Mapping(target = "requestedBy", source = "requestedBy")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "metadata", source = "metadata")
    Ollama toEntity(OllamaDTO dto);
}
