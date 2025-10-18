package com.jm.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChangePasswordDTO {
    private UUID userId;
    private String oldPassword;
    private String newPassword;
}
