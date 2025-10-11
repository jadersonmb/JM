package com.jm.services;

import com.jm.dto.BiochemicalExamDTO;
import com.jm.entity.BiochemicalExam;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.repository.BiochemicalExamRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BiochemicalExamService {

    private final BiochemicalExamRepository repository;
    private final MessageSource messageSource;

    public List<BiochemicalExamDTO> findAll(String language) {
        Map<UUID, BiochemicalExam> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            repository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language)
                    .forEach(exam -> accumulator.put(exam.getId(), exam));
        } else {
            repository.findAllByOrderByNameAsc()
                    .forEach(exam -> accumulator.put(exam.getId(), exam));
        }
        return accumulator.values().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private BiochemicalExamDTO toDto(BiochemicalExam entity) {
        return BiochemicalExamDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .measurementUnitId(entity.getMeasurementUnit() != null ? entity.getMeasurementUnit().getId() : null)
                .measurementUnitDescription(
                        entity.getMeasurementUnit() != null ? entity.getMeasurementUnit().getDescription() : null)
                .minReferenceValue(entity.getMinReferenceValue())
                .maxReferenceValue(entity.getMaxReferenceValue())
                .build();
    }

    public BiochemicalExam findByEntityId(UUID id) {
        return repository.findById(id).orElseThrow(this::biochemicalExamNotFound);
    }

    private JMException biochemicalExamNotFound() {
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        String messageDetails = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" },
                LocaleContextHolder.getLocale());
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(),
                messageDetails);
    }
}
