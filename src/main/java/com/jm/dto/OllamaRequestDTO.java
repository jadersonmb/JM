package com.jm.dto;

import java.util.List;

import lombok.Data;

@Data
public class OllamaRequestDTO {
    private String model;
    private String prompt;
    private Boolean stream;
    private List<String> images;
}
