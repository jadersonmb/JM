package com.jm.dto.food;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSubstitutionSuggestionDTO {

    private String reference;
    private FoodSuggestionDTO original;
    @Builder.Default
    private List<FoodSuggestionDTO> alternatives = new ArrayList<>();
}
