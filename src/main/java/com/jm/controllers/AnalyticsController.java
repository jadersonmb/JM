package com.jm.controllers;

import com.jm.dto.analytics.BodyCompositionResponseDTO;
import com.jm.dto.analytics.GoalAdherenceResponseDTO;
import com.jm.dto.analytics.HydrationResponseDTO;
import com.jm.dto.analytics.MacroDistributionResponseDTO;
import com.jm.dto.analytics.TopFoodsResponseDTO;
import com.jm.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/goals/adherence")
    public ResponseEntity<GoalAdherenceResponseDTO> getGoalsAdherence(
            @RequestParam(name = "range", required = false) Integer range,
            @RequestParam(name = "groupBy", required = false) String groupBy,
            @RequestParam(name = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(analyticsService.getGoalsAdherence(range, groupBy, userId));
    }

    @GetMapping("/macros/distribution")
    public ResponseEntity<MacroDistributionResponseDTO> getMacroDistribution(
            @RequestParam(name = "range", required = false) Integer range,
            @RequestParam(name = "groupBy", required = false) String groupBy,
            @RequestParam(name = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(analyticsService.getMacroDistribution(range, groupBy, userId));
    }

    @GetMapping("/hydration")
    public ResponseEntity<HydrationResponseDTO> getHydration(
            @RequestParam(name = "range", required = false) Integer range,
            @RequestParam(name = "groupBy", required = false) String groupBy,
            @RequestParam(name = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(analyticsService.getHydration(range, groupBy, userId));
    }

    @GetMapping("/foods/top")
    public ResponseEntity<TopFoodsResponseDTO> getTopFoods(
            @RequestParam(name = "range", required = false) Integer range,
            @RequestParam(name = "groupBy", required = false) String groupBy,
            @RequestParam(name = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(analyticsService.getTopFoods(range, groupBy, userId));
    }

    @GetMapping("/body/biometrics")
    public ResponseEntity<BodyCompositionResponseDTO> getBodyComposition(
            @RequestParam(name = "range", required = false) Integer range,
            @RequestParam(name = "groupBy", required = false) String groupBy,
            @RequestParam(name = "userId", required = false) UUID userId
    ) {
        return ResponseEntity.ok(analyticsService.getBodyComposition(range, groupBy, userId));
    }
}
