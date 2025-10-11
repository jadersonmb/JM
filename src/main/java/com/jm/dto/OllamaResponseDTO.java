package com.jm.dto;

import lombok.Data;

@Data
public class OllamaResponseDTO {
    private String model;
    private String response;
    private boolean done;
}
