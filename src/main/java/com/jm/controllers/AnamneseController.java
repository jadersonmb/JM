package com.jm.controllers;

import com.jm.dto.AnamneseDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.AnamneseService;
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
@RequestMapping("/api/v1/anamnese")
@AllArgsConstructor
@Tag(name = "Anamnese", description = "Operations about nutritional anamnesis")
public class AnamneseController {

    private static final Logger logger = LoggerFactory.getLogger(AnamneseService.class);

    private final AnamneseService service;

    @PostMapping
    public ResponseEntity<AnamneseDTO> create(@RequestBody AnamneseDTO dto) {
        logger.debug("REST request to create Anamnese : {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<AnamneseDTO>> listAll(Pageable pageable, AnamneseDTO filter) {
        logger.debug("REST request to get all Anamneses");
        Page<AnamneseDTO> anamneses = service.findAll(pageable, filter);
        return ResponseEntity.ok(anamneses);
    }

    @PutMapping
    public ResponseEntity<AnamneseDTO> update(@RequestBody AnamneseDTO dto) {
        logger.debug("REST request to update Anamnese : {}", dto);
        return ResponseEntity.ok(service.update(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnamneseDTO> findById(@PathVariable UUID id) {
        logger.debug("REST request to get Anamnese : {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete Anamnese : {}", id);
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
