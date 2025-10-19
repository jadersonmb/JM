package com.jm.controllers;

import com.jm.dto.AnamnesisDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.AnamnesisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/anamnesis")
@AllArgsConstructor
@Tag(name = "Anamnesis", description = "Operations about nutritional anamnesis")
public class AnamnesisController {

    private static final Logger logger = LoggerFactory.getLogger(AnamnesisService.class);

    private final AnamnesisService service;

    @PermissionRequired("ROLE_ANAMNESIS_CREATE")
    @PostMapping
    public ResponseEntity<AnamnesisDTO> create(@RequestBody AnamnesisDTO dto) {
        logger.debug("REST request to create Anamnesis : {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PermissionRequired("ROLE_ANAMNESIS_READ")
    @GetMapping
    public ResponseEntity<Page<AnamnesisDTO>> listAll(Pageable pageable, AnamnesisDTO filter) {
        logger.debug("REST request to get all Anamneses");
        Page<AnamnesisDTO> anamnesisPage = service.findAll(pageable, filter);
        return ResponseEntity.ok(anamnesisPage);
    }

    @PermissionRequired("ROLE_ANAMNESIS_UPDATE")
    @PutMapping
    public ResponseEntity<AnamnesisDTO> update(@RequestBody AnamnesisDTO dto) {
        logger.debug("REST request to update Anamnesis : {}", dto);
        return ResponseEntity.ok(service.update(dto));
    }

    @PermissionRequired("ROLE_ANAMNESIS_READ")
    @GetMapping("/{id}")
    public ResponseEntity<AnamnesisDTO> findById(@PathVariable UUID id) {
        logger.debug("REST request to get Anamnesis : {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PermissionRequired("ROLE_ANAMNESIS_DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete Anamnesis : {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({JMException.class})
    public ResponseEntity<?> exception(JMException ex) {
        Problem problem = createProblemBuild(ex.getStatus(), ex.getDetails(), ex.getType(), ex.getTitle()).build();
        return ResponseEntity.badRequest().body(problem);
    }

    private Problem.ProblemBuilder createProblemBuild(Integer status, String detail, String type, String title) {
        return Problem.builder()
                .status(status)
                .details(detail)
                .type(type)
                .title(title);
    }
}
