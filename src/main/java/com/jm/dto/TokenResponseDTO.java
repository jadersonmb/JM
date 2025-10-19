package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserDTO user;
}
