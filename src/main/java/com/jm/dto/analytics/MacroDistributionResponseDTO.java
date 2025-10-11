package com.jm.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MacroDistributionResponseDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String groupBy;
    private List<MacroDistributionEntryDTO> series;
}
