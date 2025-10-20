package com.jm.controllers;

import com.jm.dto.PhotoEvolutionComparisonDTO;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.PhotoEvolutionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/photo-evolution")
@RequiredArgsConstructor
@Tag(name = "Photo evolution comparison", description = "Body evolution comparison dashboard")
public class PhotoEvolutionComparisonController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoEvolutionComparisonController.class);

    private final PhotoEvolutionService service;

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_READ")
    @GetMapping("/{userId}/comparison")
    public ResponseEntity<PhotoEvolutionComparisonDTO> getComparison(@PathVariable UUID userId) {
        logger.debug("REST request to get photo evolution comparison for user {}", userId);
        return ResponseEntity.ok(service.getComparisonByUser(userId));
    }
}
