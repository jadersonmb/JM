package com.jm.services;

import com.jm.dto.CityDTO;
import com.jm.entity.City;
import com.jm.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

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
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CityDTO toDto(City city) {
        if (city == null) {
            return null;
        }
        return CityDTO.builder()
                .id(city.getId())
                .countryId(city.getCountry() != null ? city.getCountry().getId() : null)
                .stateCode(city.getStateCode())
                .stateName(city.getStateName())
                .cityCode(city.getCityCode())
                .name(city.getName())
                .latitude(city.getLatitude())
                .longitude(city.getLongitude())
                .population(city.getPopulation())
                .timezone(city.getTimezone())
                .capital(Boolean.TRUE.equals(city.getIsCapital()))
                .build();
    }
}
