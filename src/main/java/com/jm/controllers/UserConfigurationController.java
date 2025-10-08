package com.jm.controllers;

import com.jm.dto.UserConfigurationDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.UserConfigurationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/settings")
@Tag(name = "User settings", description = "Configuration scoped to a single user")
@RequiredArgsConstructor
public class UserConfigurationController {

    private final UserConfigurationService service;

    @GetMapping
    public ResponseEntity<UserConfigurationDTO> get(@PathVariable UUID userId) throws JMException {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<UserConfigurationDTO> update(@PathVariable UUID userId,
            @RequestBody UserConfigurationDTO dto) throws JMException {
        dto.setUserId(userId);
        return ResponseEntity.ok(service.update(userId, dto));
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<Problem> handle(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.badRequest().body(problem);
    }
}
