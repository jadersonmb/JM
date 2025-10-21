package com.jm.controllers;

import com.jm.dto.ActionDTO;
import com.jm.dto.ActionRequest;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.security.service.ActionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/actions")
@RequiredArgsConstructor
@Tag(name = "Actions", description = "Action management API")
@SecurityRequirement(name = "BearerAuth")
public class ActionController {

    private final ActionService actionService;

    @GetMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<List<ActionDTO>> listActions() {
        return ResponseEntity.ok(actionService.listAll());
    }

    @PostMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<ActionDTO> createAction(@Valid @RequestBody ActionRequest request) {
        ActionDTO created = actionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<ActionDTO> updateAction(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) {
        return ResponseEntity.ok(actionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<Void> deleteAction(@PathVariable UUID id) {
        actionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<Problem> handleException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }
}
