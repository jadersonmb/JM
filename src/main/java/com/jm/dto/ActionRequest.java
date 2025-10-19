package com.jm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActionRequest {

    @NotBlank
    private String name;
}
