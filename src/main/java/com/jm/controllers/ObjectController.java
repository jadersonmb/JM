package com.jm.controllers;

import com.jm.dto.ObjectDTO;
import com.jm.dto.ObjectRequest;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.security.service.ObjectService;
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
@RequestMapping("/api/v1/objects")
@RequiredArgsConstructor
@Tag(name = "Objects", description = "Object management API")
public class ObjectController {

    private final ObjectService objectService;

    @GetMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<List<ObjectDTO>> listObjects() {
        return ResponseEntity.ok(objectService.listAll());
    }

    @PostMapping
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<ObjectDTO> createObject(@Valid @RequestBody ObjectRequest request) {
        ObjectDTO created = objectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<ObjectDTO> updateObject(@PathVariable UUID id, @Valid @RequestBody ObjectRequest request) {
        return ResponseEntity.ok(objectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("ROLE_ADMIN_MANAGE_ROLES")
    public ResponseEntity<Void> deleteObject(@PathVariable UUID id) {
        objectService.delete(id);
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
