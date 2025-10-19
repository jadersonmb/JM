package com.jm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ObjectRequest {

    @NotBlank
    private String name;

    private String description;
}
