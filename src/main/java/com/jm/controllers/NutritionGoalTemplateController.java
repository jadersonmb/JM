package com.jm.controllers;

import com.jm.dto.NutritionGoalTemplateDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.NutritionGoalTemplateService;
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
@RequestMapping("/api/v1/goal-templates")
@AllArgsConstructor
@Tag(name = "Nutrition Goal Templates", description = "Operations about nutrition goal templates")
public class NutritionGoalTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(NutritionGoalTemplateService.class);

    private final NutritionGoalTemplateService service;

    @PostMapping
    public ResponseEntity<NutritionGoalTemplateDTO> create(@RequestBody NutritionGoalTemplateDTO dto) {
        logger.debug("REST request to create NutritionGoalTemplate");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<Page<NutritionGoalTemplateDTO>> list(Pageable pageable, NutritionGoalTemplateDTO filter) {
        logger.debug("REST request to get all nutrition goal templates");
        return ResponseEntity.ok(service.findAll(pageable, filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NutritionGoalTemplateDTO> findById(@PathVariable UUID id) {
        logger.debug("REST request to get nutrition goal template {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping
    public ResponseEntity<NutritionGoalTemplateDTO> update(@RequestBody NutritionGoalTemplateDTO dto) {
        logger.debug("REST request to update NutritionGoalTemplate : {}", dto);
        return ResponseEntity.ok(service.update(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete NutritionGoalTemplate : {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({ JMException.class })
    public ResponseEntity<?> Exception(JMException ex) {
        Problem problem = createProblemBuild(ex.getStatus(), ex.getDetails(), ex.getType(), ex.getTitle()).build();
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus())).body(problem);
    }

    private Problem.ProblemBuilder createProblemBuild(Integer status, String detail, String type, String title) {
        return Problem.builder()
                .status(status)
                .details(detail)
                .type(type)
                .title(title);
    }
}
