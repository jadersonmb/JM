package com.jm.controllers;

import com.jm.dto.RoleDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.RoleService;
import com.jm.security.annotation.PermissionRequired;
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
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management API")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<List<RoleDTO>> listRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @PostMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO dto) {
        RoleDTO created = roleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable UUID id, @Valid @RequestBody RoleDTO dto) {
        return ResponseEntity.ok(roleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<?> handleException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }
}
