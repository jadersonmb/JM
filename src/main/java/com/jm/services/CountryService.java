package com.jm.services;

import com.jm.dto.CountryDTO;
import com.jm.entity.Country;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.CountryMapper;
import com.jm.repository.CountryRepository;
import com.jm.speciation.CountrySpecification;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;
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
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;
    private final MessageSource messageSource;

    public List<CountryDTO> findAll(String language) {
        List<Country> countries;
        if (StringUtils.hasText(language)) {
            countries = countryRepository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language);
        } else {
            countries = countryRepository.findAllByOrderByNameAsc();
        }
        return countries.stream()
                .map(countryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<CountryDTO> search(Pageable pageable, String name, String code, String language) {
        return countryRepository.findAll(CountrySpecification.search(name, code, language), pageable)
                .map(countryMapper::toDTO);
    }

    @Transactional
    public CountryDTO save(CountryDTO dto) {
        Country entity;
        if (dto.getId() != null) {
            entity = countryRepository.findById(dto.getId()).orElseThrow(this::countryNotFound);
            countryMapper.updateEntityFromDto(dto, entity);
        } else {
            entity = countryMapper.toEntity(dto);
        }
        if (StringUtils.hasText(entity.getCode())) {
            entity.setCode(entity.getCode().trim().toUpperCase(Locale.ROOT));
        }
        Country saved = countryRepository.save(entity);
        return countryMapper.toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Country entity = countryRepository.findById(id).orElseThrow(this::countryNotFound);
        countryRepository.delete(entity);
    }

    public CountryDTO findById(@NotNull UUID id) {
        return countryMapper.toDTO(countryRepository.findById(id).orElseThrow(this::countryNotFound));
    }

    public Country findEntityById(UUID id) {
        return countryRepository.findById(id).orElseThrow(this::countryNotFound);
    }

    private JMException countryNotFound() {
        ProblemType problemType = ProblemType.COUNTRY_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), message);
    }
}
