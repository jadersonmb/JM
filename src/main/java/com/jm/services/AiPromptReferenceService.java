package com.jm.services;

import com.jm.dto.AiPromptReferenceDTO;
import com.jm.entity.AiPromptReference;
import com.jm.enums.AiProvider;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.AiPromptReferenceMapper;
import com.jm.repository.AiPromptReferenceRepository;
import com.jm.speciation.AiPromptReferenceSpecification;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiPromptReferenceService {

    private static final Logger logger = LoggerFactory.getLogger(AiPromptReferenceService.class);

    private final AiPromptReferenceRepository repository;
    private final AiPromptReferenceMapper mapper;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public Page<AiPromptReferenceDTO> search(Pageable pageable, String code, String name, String model,
            AiProvider provider, Boolean active, String owner) {
        return repository.findAll(AiPromptReferenceSpecification.search(code, name, model, provider, active, owner), pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    public AiPromptReferenceDTO save(AiPromptReferenceDTO dto) {
        AiPromptReference entity;
        if (dto.getId() != null) {
            entity = repository.findById(dto.getId()).orElseThrow(this::promptNotFound);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
        }

        normalize(entity);
        AiPromptReference saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        AiPromptReference entity = repository.findById(id).orElseThrow(this::promptNotFound);
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public AiPromptReferenceDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(this::promptNotFound));
    }

    @Transactional(readOnly = true)
    public Optional<AiPromptReferenceDTO> resolvePrompt(String code, AiProvider provider, String model, String owner) {
        if (!StringUtils.hasText(code) || provider == null || !StringUtils.hasText(model)) {
            return Optional.empty();
        }

        String normalizedOwner = StringUtils.hasText(owner) ? owner.trim() : null;
        if (StringUtils.hasText(normalizedOwner)) {
            return repository
                    .findFirstByCodeIgnoreCaseAndProviderAndModelIgnoreCaseAndOwnerIgnoreCaseAndActiveTrueOrderByUpdatedAtDesc(
                            code, provider, model, normalizedOwner)
                    .map(mapper::toDTO)
                    .or(() -> repository
                            .findFirstByCodeIgnoreCaseAndProviderAndModelIgnoreCaseAndOwnerIsNullAndActiveTrueOrderByUpdatedAtDesc(
                                    code, provider, model)
                            .map(mapper::toDTO));
        }

        return repository
                .findFirstByCodeIgnoreCaseAndProviderAndModelIgnoreCaseAndOwnerIsNullAndActiveTrueOrderByUpdatedAtDesc(code,
                        provider, model)
                .map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AiPromptReferenceDTO> resolvePrompt(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }

        return repository.findFirstByCodeIgnoreCaseAndActiveTrueOrderByUpdatedAtDesc(code.trim())
                .map(mapper::toDTO);
    }

    private void normalize(AiPromptReference entity) {
        if (entity.getCode() != null) {
            entity.setCode(entity.getCode().trim().toUpperCase(Locale.ROOT));
        }
        if (entity.getModel() != null) {
            entity.setModel(entity.getModel().trim());
        }
        if (!StringUtils.hasText(entity.getOwner())) {
            entity.setOwner(null);
        } else {
            entity.setOwner(entity.getOwner().trim());
        }
        entity.setActive(entity.getActive() == null ? Boolean.TRUE : entity.getActive());
        if (!StringUtils.hasText(entity.getName())) {
            entity.setName(entity.getCode());
        }
    }

    private JMException promptNotFound() {
        ProblemType problemType = ProblemType.INVALID_DATA;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("ai.prompt.not-found", new Object[] { "" }, "Prompt reference not found",
                locale);
        logger.debug("Prompt reference not found");
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), message);
    }
}
