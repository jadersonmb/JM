package com.jm.dto;

import com.jm.entity.AnamnesisFoodPreference.PreferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnamnesisFoodPreferenceDTO {

    private UUID id;
    private UUID foodId;
    private String foodName;
    private PreferenceType preferenceType;
    private String notes;
}
