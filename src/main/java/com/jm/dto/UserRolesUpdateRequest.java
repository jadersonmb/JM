package com.jm.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserRolesUpdateRequest {

    @NotEmpty
    private Set<UUID> roleIds;
}
