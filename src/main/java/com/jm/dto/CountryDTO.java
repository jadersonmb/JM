package com.jm.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.google.auto.value.AutoValue.Builder;

import lombok.Data;

@Data
@Builder
public class CountryDTO {

    private UUID id;
    private String code;
    private String language;
    private LocalDateTime createdAt;
}
