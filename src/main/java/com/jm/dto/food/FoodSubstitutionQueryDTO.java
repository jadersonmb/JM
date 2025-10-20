package com.jm.dto.food;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSubstitutionQueryDTO {

    private String reference;
    private UUID foodId;
    private Integer limit;
}
