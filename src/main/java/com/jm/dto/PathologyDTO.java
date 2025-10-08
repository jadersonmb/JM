package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathologyDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String category;
    private Boolean chronic;
    private Boolean requiresMonitoring;
}
