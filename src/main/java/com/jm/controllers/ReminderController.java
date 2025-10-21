package com.jm.controllers;

import com.jm.dto.ReminderDTO;
import com.jm.dto.ReminderFilter;
import com.jm.dto.ReminderTargetDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.ReminderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
@Tag(name = "Reminders", description = "Operations about reminders")
@SecurityRequirement(name = "BearerAuth")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);

    private final ReminderService service;

    @PermissionRequired("ROLE_REMINDERS_READ")
    @GetMapping
    public ResponseEntity<Page<ReminderDTO>> list(Pageable pageable, ReminderFilter filter) {
        logger.debug("REST request to list reminders");
        return ResponseEntity.ok(service.findAll(pageable, filter));
    }

    @PermissionRequired("ROLE_REMINDERS_READ")
    @GetMapping("/{id}")
    public ResponseEntity<ReminderDTO> findById(@PathVariable UUID id) {
        logger.debug("REST request to get reminder {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PermissionRequired("ROLE_REMINDERS_CREATE")
    @PostMapping
    public ResponseEntity<ReminderDTO> create(@RequestBody ReminderDTO dto) {
        logger.debug("REST request to create reminder");
        ReminderDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_REMINDERS_UPDATE")
    @PutMapping("/{id}")
    public ResponseEntity<ReminderDTO> update(@PathVariable UUID id, @RequestBody ReminderDTO dto) {
        logger.debug("REST request to update reminder {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PermissionRequired("ROLE_REMINDERS_UPDATE")
    @PostMapping("/{id}/test")
    public ResponseEntity<Void> triggerTest(@PathVariable UUID id) {
        logger.debug("REST request to trigger reminder test {}", id);
        service.triggerTest(id);
        return ResponseEntity.accepted().build();
    }

    @PermissionRequired("ROLE_REMINDERS_DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete reminder {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_REMINDERS_UPDATE")
    @PatchMapping("/{id}/active")
    public ResponseEntity<ReminderDTO> toggleActive(@PathVariable UUID id, @RequestParam boolean active) {
        logger.debug("REST request to toggle reminder {} active={} ", id, active);
        return ResponseEntity.ok(service.toggleActive(id, active));
    }

    @PermissionRequired("ROLE_REMINDERS_UPDATE")
    @PatchMapping("/{id}/completed")
    public ResponseEntity<ReminderDTO> toggleCompleted(@PathVariable UUID id, @RequestParam boolean completed) {
        logger.debug("REST request to toggle reminder {} completed={} ", id, completed);
        return ResponseEntity.ok(service.toggleCompleted(id, completed));
    }

    @PermissionRequired("ROLE_REMINDERS_READ")
    @GetMapping("/targets")
    public ResponseEntity<List<ReminderTargetDTO>> listTargets(@RequestParam(required = false) String query) {
        logger.debug("REST request to list reminder targets");
        return ResponseEntity.ok(service.listTargets(query));
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<Problem> handleReminderException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus())).body(problem);
    }
}
