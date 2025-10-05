package com.jm.dto.payment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CardRequest {

    @NotNull
    private UUID customerId;

    @NotBlank
    private String cardToken;

    @NotBlank
    @Size(min = 2, max = 50)
    private String brand;

    @NotBlank
    @Size(min = 4, max = 4)
    private String lastFour;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer expiryMonth;

    @NotNull
    @Min(2024)
    @Max(2124)
    private Integer expiryYear;

    private Boolean defaultCard = Boolean.FALSE;
}
