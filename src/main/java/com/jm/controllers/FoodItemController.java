package com.jm.controllers;

import com.jm.dto.food.FoodSubstitutionRequestDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.FoodItemService;
import com.jm.services.FoodSubstitutionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/foods")
@AllArgsConstructor
@Tag(name = "Diet foods", description = "Food items for diet plans")
@SecurityRequirement(name = "BearerAuth")
public class FoodItemController {

    private final FoodItemService service;
    private final FoodSubstitutionService substitutionService;

    @PermissionRequired("ROLE_FOODS_READ")
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(service.findAll(active));
    }

    @PermissionRequired("ROLE_FOODS_READ")
    @PostMapping("/substitutions")
    public ResponseEntity<?> suggestSubstitutions(@RequestBody FoodSubstitutionRequestDTO request) {
        return ResponseEntity.ok(substitutionService.generateSuggestions(request));
    }

    @PermissionRequired("ROLE_FOODS_READ")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @ExceptionHandler({ JMException.class })
    public ResponseEntity<?> handleJMException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.badRequest().body(problem);
    }
}

