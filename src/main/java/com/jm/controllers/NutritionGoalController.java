package com.jm.controllers;

import com.jm.dto.NutritionGoalCalculationResponseDTO;
import com.jm.dto.NutritionGoalCreateRequestDTO;
import com.jm.dto.NutritionGoalDTO;
import com.jm.dto.NutritionGoalOwnerDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.NutritionGoalService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({ "/api/v1/goals", "/api/v1/nutrition-goals" })
@AllArgsConstructor
@Tag(name = "Nutrition Goals", description = "Operations about nutrition goals")
public class NutritionGoalController {

    private static final Logger logger = LoggerFactory.getLogger(NutritionGoalService.class);

    private final NutritionGoalService service;

    @PermissionRequired("ROLE_GOALS_CREATE")
    @PostMapping
    public ResponseEntity<NutritionGoalDTO> create(@RequestBody NutritionGoalDTO dto) {
        logger.debug("REST request to create NutritionGoal");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PermissionRequired("ROLE_GOALS_CREATE")
    @PostMapping("/calculate")
    public ResponseEntity<NutritionGoalCalculationResponseDTO> calculate(
            @RequestBody NutritionGoalCreateRequestDTO request) {
        logger.debug("REST request to calculate AI nutrition goals");
        NutritionGoalCalculationResponseDTO response = service.calculateAndSaveUserGoals(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PermissionRequired("ROLE_GOALS_READ")
    @GetMapping
    public ResponseEntity<Page<NutritionGoalDTO>> list(Pageable pageable, NutritionGoalDTO filter) {
        logger.debug("REST request to get all nutrition goals");
        Page<NutritionGoalDTO> page = service.findAll(pageable, filter);
        return ResponseEntity.ok(page);
    }

    @PermissionRequired("ROLE_GOALS_READ")
    @GetMapping("/{id}")
    public ResponseEntity<NutritionGoalDTO> findById(@PathVariable UUID id) {
        logger.debug("REST request to get nutrition goal {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PermissionRequired("ROLE_GOALS_UPDATE")
    @PutMapping
    public ResponseEntity<NutritionGoalDTO> update(@RequestBody NutritionGoalDTO dto) {
        logger.debug("REST request to update NutritionGoal : {}", dto);
        return ResponseEntity.ok(service.update(dto));
    }

    @PermissionRequired("ROLE_GOALS_DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete NutritionGoal : {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_GOALS_READ")
    @GetMapping("/owners")
    public ResponseEntity<List<NutritionGoalOwnerDTO>> listOwners(@RequestParam(required = false) String query) {
        logger.debug("REST request to list goal owners");
        return ResponseEntity.ok(service.listOwners(query));
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
