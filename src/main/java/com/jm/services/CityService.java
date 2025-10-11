package com.jm.services;

import com.jm.dto.CityDTO;
import com.jm.entity.Anamnesis;
import com.jm.entity.City;
import com.jm.entity.Country;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.CityMapper;
import com.jm.repository.CityRepository;
import com.jm.repository.CountryRepository;
import com.jm.speciation.CitySpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
public class CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final CityMapper cityMapper;
    private final MessageSource messageSource;

    public List<CityDTO> findByCountry(UUID countryId, String language) {
        if (countryId == null) {
            return List.of();
        }

        Map<UUID, City> accumulator = new LinkedHashMap<>();
        if (StringUtils.hasText(language)) {
            cityRepository.findByCountryIdAndLanguageIgnoreCaseOrderByNameAsc(countryId, language)
                    .forEach(city -> accumulator.put(city.getId(), city));
        } else {
            cityRepository.findByCountryIdOrderByNameAsc(countryId)
                    .forEach(city -> accumulator.put(city.getId(), city));
        }

        cityRepository.findByCountryIdAndLanguageIsNullOrderByNameAsc(countryId)
                .forEach(city -> accumulator.putIfAbsent(city.getId(), city));

        return accumulator.values().stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<CityDTO> search(Pageable pageable, UUID countryId, String name, String stateCode, String language) {
        return cityRepository.findAll(CitySpecification.search(countryId, name, stateCode, language), pageable)
                .map(cityMapper::toDTO);
    }

    @Transactional
    public CityDTO save(CityDTO dto) {
        if (dto.getCountryId() == null) {
            throw invalidBody();
        }
        City entity;
        if (dto.getId() != null) {
            entity = cityRepository.findById(dto.getId()).orElseThrow(this::cityNotFound);
            cityMapper.updateEntityFromDto(dto, entity);
        } else {
            entity = cityMapper.toEntity(dto);
        }
        Country country = resolveCountry(dto.getCountryId());
        entity.setCountry(country);
        City saved = cityRepository.save(entity);
        return cityMapper.toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        City entity = cityRepository.findById(id).orElseThrow(this::cityNotFound);
        cityRepository.delete(entity);
    }

    private Country resolveCountry(UUID id) {
        return countryRepository.findById(id).orElseThrow(this::countryNotFound);
    }

    public City findEntityById(UUID id) {
        return cityRepository.findById(id).orElseThrow(this::cityNotFound);
    }

    private JMException invalidBody() {
        ProblemType problemType = ProblemType.INVALID_BODY;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), message);
    }

    private JMException cityNotFound() {
        ProblemType problemType = ProblemType.CITY_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), message);
    }

    private JMException countryNotFound() {
        ProblemType problemType = ProblemType.COUNTRY_NOT_FOUND;
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(problemType.getMessageSource(), new Object[] { "" }, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), message);
    }

}
