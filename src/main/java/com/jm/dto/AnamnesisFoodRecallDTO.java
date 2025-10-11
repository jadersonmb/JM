package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnamnesisFoodRecallDTO {

    private UUID id;
    private String mealName;
    private String observation;

    @Builder.Default
    private List<AnamnesisFoodRecallItemDTO> items = new ArrayList<>();
}
