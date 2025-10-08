package com.jm.mappers;

import com.jm.dto.AnamneseDTO;
import com.jm.dto.ExameBioquimicoDTO;
import com.jm.dto.Refeicao24hDTO;
import com.jm.entity.Anamnese;
import com.jm.entity.ExameBioquimico;
import com.jm.entity.Refeicao24h;
import com.jm.entity.Users;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AnamneseMapper {

    public AnamneseDTO toDTO(Anamnese entity) {
        if (entity == null) {
            return null;
        }
        AnamneseDTO dto = new AnamneseDTO();
        BeanUtils.copyProperties(entity, dto);

        if (Objects.nonNull(entity.getExamesBioquimicos())) {
            dto.setExamesBioquimicos(entity.getExamesBioquimicos().stream()
                    .map(this::mapExameBioquimicoToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setExamesBioquimicos(new ArrayList<>());
        }

        if (Objects.nonNull(entity.getRefeicoes24h())) {
            dto.setRefeicoes24h(entity.getRefeicoes24h().stream()
                    .map(this::mapRefeicaoToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setRefeicoes24h(new ArrayList<>());
        }

        if (entity.getUser() != null) {
            Users user = entity.getUser();
            dto.setUserId(user.getId());
            dto.setCountryId(user.getCountry() != null ? user.getCountry().getId() : null);
            dto.setCountryName(user.getCountry() != null ? user.getCountry().getName() : null);
            dto.setCityId(user.getCity() != null ? user.getCity().getId() : null);
            dto.setCityName(user.getCity() != null ? user.getCity().getName() : null);
            dto.setEducationLevelId(user.getEducationLevel() != null ? user.getEducationLevel().getId() : null);
            dto.setProfessionId(user.getProfession() != null ? user.getProfession().getId() : null);
            dto.setPaciente(buildFullName(user));
            dto.setEndereco(buildAddress(user));
            dto.setDataNascimento(user.getBirthDate());
            dto.setIdade(user.getAge());
            dto.setTelefone(user.getPhoneNumber());
            dto.setEscolaridade(user.getEducationLevel() != null ? user.getEducationLevel().getName() : dto.getEscolaridade());
            dto.setProfissao(user.getProfession() != null ? user.getProfession().getName() : dto.getProfissao());
            dto.setObjetivoConsulta(user.getConsultationGoal());
        }

        return dto;
    }

    public Anamnese toEntity(AnamneseDTO dto) {
        if (dto == null) {
            return null;
        }
        Anamnese entity = new Anamnese();
        BeanUtils.copyProperties(dto, entity, "examesBioquimicos", "refeicoes24h", "userId");

        if (Objects.nonNull(dto.getExamesBioquimicos())) {
            List<ExameBioquimico> exames = dto.getExamesBioquimicos().stream()
                    .map(this::mapExameBioquimicoToEntity)
                    .collect(Collectors.toList());
            exames.forEach(exame -> exame.setAnamnese(entity));
            entity.setExamesBioquimicos(exames);
        } else {
            entity.setExamesBioquimicos(new ArrayList<>());
        }

        if (Objects.nonNull(dto.getRefeicoes24h())) {
            List<Refeicao24h> refeicoes = dto.getRefeicoes24h().stream()
                    .map(this::mapRefeicaoToEntity)
                    .collect(Collectors.toList());
            refeicoes.forEach(refeicao -> refeicao.setAnamnese(entity));
            entity.setRefeicoes24h(refeicoes);
        } else {
            entity.setRefeicoes24h(new ArrayList<>());
        }

        return entity;
    }

    private ExameBioquimicoDTO mapExameBioquimicoToDto(ExameBioquimico entity) {
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

    private ExameBioquimico mapExameBioquimicoToEntity(ExameBioquimicoDTO dto) {
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

    private Refeicao24hDTO mapRefeicaoToDto(Refeicao24h entity) {
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

    private Refeicao24h mapRefeicaoToEntity(Refeicao24hDTO dto) {
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

    private String buildAddress(Users user) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(user.getStreet())) {
            parts.add(user.getStreet());
        }
        if (user.getCity() != null && StringUtils.hasText(user.getCity().getName())) {
            parts.add(user.getCity().getName());
        }
        if (StringUtils.hasText(user.getState())) {
            parts.add(user.getState());
        }
        if (user.getCountry() != null && StringUtils.hasText(user.getCountry().getName())) {
            parts.add(user.getCountry().getName());
        }
        return String.join(", ", parts);
    }

    private String buildFullName(Users user) {
        String firstName = user.getName();
        String lastName = user.getLastName();
        if (!StringUtils.hasText(firstName)) {
            return StringUtils.hasText(lastName) ? lastName : "";
        }
        if (!StringUtils.hasText(lastName)) {
            return firstName;
        }
        return String.format("%s %s", firstName, lastName).trim();
    }
}
