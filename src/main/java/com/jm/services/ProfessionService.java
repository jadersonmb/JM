package com.jm.services;

import com.jm.dto.ProfessionDTO;
import com.jm.entity.Profession;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.ProfessionMapper;
import com.jm.repository.ProfessionRepository;
import com.jm.speciation.ProfessionSpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
public class ProfessionService {

    private final ProfessionRepository repository;
    private final ProfessionMapper mapper;
    private final MessageSource messageSource;

    public List<ProfessionDTO> findAll(String language) {
        Map<UUID, Profession> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(profession -> accumulator.put(profession.getId(), profession));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(profession -> accumulator.put(profession.getId(), profession));
        }
        return accumulator.values().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<ProfessionDTO> search(Pageable pageable, String name, String code, String language) {
        return repository.findAll(ProfessionSpecification.search(name, code, language), pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    public ProfessionDTO save(ProfessionDTO dto) {
        Profession entity;
        if (dto.getId() != null) {
            entity = repository.findById(dto.getId()).orElseThrow(this::professionNotFound);
            mapper.updateEntityFromDto(dto, entity);
        } else {
            entity = mapper.toEntity(dto);
        }
        Profession saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Profession entity = repository.findById(id).orElseThrow(this::professionNotFound);
        repository.delete(entity);
    }

    public ProfessionDTO findById(UUID id) {
        return mapper.toDTO(repository.findById(id).orElseThrow(this::professionNotFound));
    }

    public Profession findEntityById(UUID id) {
        return repository.findById(id).orElseThrow(this::professionNotFound);
    }

    private JMException professionNotFound() {
        ProblemType problemType = ProblemType.PROFESSION_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), message);
    }
}
