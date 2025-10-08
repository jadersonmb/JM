package com.jm.services;

import com.jm.dto.CountryDTO;
import com.jm.mappers.CountryMapper;
import com.jm.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public List<CountryDTO> findAll(String language) {
        List<com.jm.entity.Country> countries;
        if (StringUtils.hasText(language)) {
            countries = countryRepository.findByLanguageIgnoreCaseOrLanguageIsNullOrderByNameAsc(language);
        } else {
            countries = countryRepository.findAllByOrderByNameAsc();
        }
        return countries.stream()
                .map(countryMapper::toDTO)
                .collect(Collectors.toList());
    }
}
