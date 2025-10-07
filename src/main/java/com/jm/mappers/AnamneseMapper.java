package com.jm.mappers;

import com.jm.dto.AnamneseDTO;
import com.jm.dto.ExameBioquimicoDTO;
import com.jm.dto.Refeicao24hDTO;
import com.jm.entity.Anamnese;
import com.jm.entity.ExameBioquimico;
import com.jm.entity.Refeicao24h;
import com.jm.entity.Users;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnamneseMapper {

    @Mapping(target = "examesBioquimicos", ignore = true)
    @Mapping(target = "refeicoes24h", ignore = true)
    @Mapping(target = "userId", ignore = true)
    AnamneseDTO toDTO(Anamnese entity);

    @Mapping(target = "examesBioquimicos", ignore = true)
    @Mapping(target = "refeicoes24h", ignore = true)
    @Mapping(target = "user", ignore = true)
    Anamnese toEntity(AnamneseDTO dto);

    @AfterMapping
    default void afterToDto(Anamnese entity, @MappingTarget AnamneseDTO dto) {
        if (Objects.nonNull(entity.getExamesBioquimicos())) {
            dto.setExamesBioquimicos(entity.getExamesBioquimicos()
                    .stream()
                    .map(this::mapExameBioquimicoToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setExamesBioquimicos(new ArrayList<>());
        }

        if (Objects.nonNull(entity.getRefeicoes24h())) {
            dto.setRefeicoes24h(entity.getRefeicoes24h()
                    .stream()
                    .map(this::mapRefeicaoToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setRefeicoes24h(new ArrayList<>());
        }

        if (entity.getUser() != null) {
            Users user = entity.getUser();
            dto.setUserId(user.getId());
            dto.setPaciente(buildFullName(user));
            dto.setEndereco(buildAddress(user));
            dto.setDataNascimento(user.getBirthDate());
            dto.setIdade(user.getAge());
            dto.setTelefone(user.getPhoneNumber());
            dto.setEscolaridade(user.getEducation());
            dto.setProfissao(user.getOccupation());
            dto.setObjetivoConsulta(user.getConsultationGoal());
        }
    }

    @AfterMapping
    default void afterToEntity(AnamneseDTO dto, @MappingTarget Anamnese entity) {
        if (Objects.nonNull(dto.getExamesBioquimicos())) {
            List<ExameBioquimico> exames = dto.getExamesBioquimicos()
                    .stream()
                    .map(this::mapExameBioquimicoToEntity)
                    .collect(Collectors.toList());
            exames.forEach(exame -> exame.setAnamnese(entity));
            entity.setExamesBioquimicos(exames);
        } else {
            entity.setExamesBioquimicos(new ArrayList<>());
        }

        if (Objects.nonNull(dto.getRefeicoes24h())) {
            List<Refeicao24h> refeicoes = dto.getRefeicoes24h()
                    .stream()
                    .map(this::mapRefeicaoToEntity)
                    .collect(Collectors.toList());
            refeicoes.forEach(refeicao -> refeicao.setAnamnese(entity));
            entity.setRefeicoes24h(refeicoes);
        } else {
            entity.setRefeicoes24h(new ArrayList<>());
        }
    }

    default ExameBioquimicoDTO mapExameBioquimicoToDto(ExameBioquimico entity) {
        if (entity == null) {
            return null;
        }
        return ExameBioquimicoDTO.builder()
                .id(entity.getId())
                .nomeExame(entity.getNomeExame())
                .valor(entity.getValor())
                .dataExame(entity.getDataExame())
                .build();
    }

    default ExameBioquimico mapExameBioquimicoToEntity(ExameBioquimicoDTO dto) {
        if (dto == null) {
            return null;
        }
        UUID id = dto.getId() != null ? dto.getId() : UUID.randomUUID();
        return ExameBioquimico.builder()
                .id(id)
                .nomeExame(dto.getNomeExame())
                .valor(dto.getValor())
                .dataExame(dto.getDataExame())
                .build();
    }

    default Refeicao24hDTO mapRefeicaoToDto(Refeicao24h entity) {
        if (entity == null) {
            return null;
        }
        return Refeicao24hDTO.builder()
                .id(entity.getId())
                .nomeRefeicao(entity.getNomeRefeicao())
                .alimentos(entity.getAlimentos())
                .quantidades(entity.getQuantidades())
                .build();
    }

    default Refeicao24h mapRefeicaoToEntity(Refeicao24hDTO dto) {
        if (dto == null) {
            return null;
        }
        UUID id = dto.getId() != null ? dto.getId() : UUID.randomUUID();
        return Refeicao24h.builder()
                .id(id)
                .nomeRefeicao(dto.getNomeRefeicao())
                .alimentos(dto.getAlimentos())
                .quantidades(dto.getQuantidades())
                .build();
    }

    default String buildAddress(Users user) {
        List<String> parts = new ArrayList<>();
        if (user.getStreet() != null && !user.getStreet().isBlank()) {
            parts.add(user.getStreet());
        }
        if (user.getCity() != null && !user.getCity().isBlank()) {
            parts.add(user.getCity());
        }
        if (user.getState() != null && !user.getState().isBlank()) {
            parts.add(user.getState());
        }
        if (user.getCountry() != null && !user.getCountry().isBlank()) {
            parts.add(user.getCountry());
        }
        return String.join(", ", parts);
    }

    default String buildFullName(Users user) {
        String firstName = user.getName();
        String lastName = user.getLastName();
        if (firstName == null || firstName.isBlank()) {
            return lastName != null ? lastName : "";
        }
        if (lastName == null || lastName.isBlank()) {
            return firstName;
        }
        return String.format("%s %s", firstName, lastName).trim();
    }
}
