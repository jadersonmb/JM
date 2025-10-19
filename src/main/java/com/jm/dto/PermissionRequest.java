package com.jm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PermissionRequest {

    @NotBlank
    private String code;

    private String description;

    @NotNull
    private UUID actionId;

    @NotNull
    private UUID objectId;
}
