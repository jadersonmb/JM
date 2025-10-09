package com.jm.controllers;

import com.jm.dto.DietOwnerDTO;
import com.jm.dto.DietPlanDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.DietPlanService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/diets")
@AllArgsConstructor
@Tag(name = "Diets", description = "Operations about diet plans")
public class DietPlanController {

    private static final Logger logger = LoggerFactory.getLogger(DietPlanService.class);

    private final DietPlanService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DietPlanDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<?> listAll(Pageable pageable, DietPlanDTO filter) {
        logger.debug("REST request to get all diets");
        Page<DietPlanDTO> page = service.findAll(pageable, filter);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody DietPlanDTO dto) {
        logger.debug("REST request to update Diet : {}", dto);
        return ResponseEntity.ok(service.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owners")
    public ResponseEntity<List<DietOwnerDTO>> listOwners(@RequestParam(required = false, name = "query") String query) {
        return ResponseEntity.ok(service.listOwners(query));
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

