package com.jm.controllers;

import com.jm.dto.ExerciseDTO;
import com.jm.dto.ExerciseFilter;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.ExerciseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercises", description = "Operations about exercises")
public class ExerciseController {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseController.class);

    private final ExerciseService exerciseService;

    @PermissionRequired("ROLE_EXERCISES_READ")
    @GetMapping
    public ResponseEntity<Page<ExerciseDTO>> list(Pageable pageable, ExerciseFilter filter) {
        logger.debug("REST request to get all exercises");
        return ResponseEntity.ok(exerciseService.findAll(pageable, filter));
    }

    @PermissionRequired("ROLE_EXERCISES_READ")
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDTO> findById(@PathVariable UUID id) {
        logger.debug("REST request to get exercise {}", id);
        return ResponseEntity.ok(exerciseService.findById(id));
    }

    @PermissionRequired("ROLE_EXERCISES_CREATE")
    @PostMapping
    public ResponseEntity<ExerciseDTO> create(@RequestBody ExerciseDTO exerciseDTO) {
        logger.debug("REST request to create exercise");
        ExerciseDTO saved = exerciseService.save(exerciseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PermissionRequired("ROLE_EXERCISES_UPDATE")
    @PutMapping
    public ResponseEntity<ExerciseDTO> update(@RequestBody ExerciseDTO exerciseDTO) {
        logger.debug("REST request to update exercise {}", exerciseDTO.getId());
        ExerciseDTO saved = exerciseService.save(exerciseDTO);
        return ResponseEntity.ok(saved);
    }

    @PermissionRequired("ROLE_EXERCISES_DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete exercise {}", id);
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<Problem> handleJMException(JMException ex) {
        Problem problem = createProblemBuild(ex.getStatus(), ex.getDetails(), ex.getType(), ex.getTitle()).build();
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus())).body(problem);
    }

    private Problem.ProblemBuilder createProblemBuild(Integer status, String detail, String type, String title) {
        return Problem.builder().status(status).details(detail).type(type).title(title);
    }
}
