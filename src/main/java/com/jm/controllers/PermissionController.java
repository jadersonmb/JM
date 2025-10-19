package com.jm.controllers;

import com.jm.dto.PermissionDTO;
import com.jm.dto.PermissionRequest;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.security.service.PermissionService;
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
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission management API")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<List<PermissionDTO>> listPermissions() {
        return ResponseEntity.ok(permissionService.listAll());
    }

    @PostMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionRequest request) {
        PermissionDTO created = permissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<PermissionDTO> updatePermission(@PathVariable UUID id, @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<Void> deletePermission(@PathVariable UUID id) {
        permissionService.delete(id);
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
