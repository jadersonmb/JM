package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private LocalTime typicalTime;
    private Integer sortOrder;
    private String language;
}
