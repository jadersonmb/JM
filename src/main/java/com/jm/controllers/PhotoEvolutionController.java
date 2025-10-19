package com.jm.controllers;

import com.jm.dto.PhotoEvolutionCreateRequest;
import com.jm.dto.PhotoEvolutionDTO;
import com.jm.dto.PhotoEvolutionOwnerDTO;
import com.jm.dto.PhotoEvolutionPrefillDTO;
import com.jm.dto.PhotoEvolutionUpdateRequest;
import com.jm.enums.BodyPart;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.PhotoEvolutionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/photo-evolutions")
@RequiredArgsConstructor
@Tag(name = "Photo evolution", description = "Manage photographic evolution with nutrition and body metrics")
public class PhotoEvolutionController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoEvolutionController.class);

    private final PhotoEvolutionService service;

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_CREATE")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PhotoEvolutionDTO>> create(
            @RequestPart("entries") List<PhotoEvolutionCreateRequest> requests,
            @RequestPart("images") List<MultipartFile> images) {
        logger.debug("REST request to create photo evolution entries");
        List<PhotoEvolutionDTO> created = service.create(requests, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_UPDATE")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoEvolutionDTO> update(@PathVariable UUID id,
                                                    @RequestPart("payload") PhotoEvolutionUpdateRequest request,
                                                    @RequestPart(value = "image", required = false) MultipartFile image) {
        logger.debug("REST request to update photo evolution entry {}", id);
        return ResponseEntity.ok(service.update(id, request, image));
    }

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_READ")
    @GetMapping
    public ResponseEntity<List<PhotoEvolutionDTO>> list(@RequestParam(required = false) UUID userId,
                                                        @RequestParam(required = false) BodyPart bodyPart) {
        logger.debug("REST request to list photo evolutions for user {}", userId);
        return ResponseEntity.ok(service.list(userId, bodyPart));
    }

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_READ")
    @GetMapping("/prefill")
    public ResponseEntity<PhotoEvolutionPrefillDTO> prefill(@RequestParam(required = false) UUID userId) {
        logger.debug("REST request to prefill photo evolution metrics for user {}", userId);
        return ResponseEntity.ok(service.prefill(userId));
    }

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_READ")
    @GetMapping("/{id}")
    public ResponseEntity<PhotoEvolutionDTO> findById(@PathVariable UUID id) {
        logger.debug("REST request to get photo evolution entry {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logger.debug("REST request to delete photo evolution entry {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PermissionRequired("ROLE_PHOTO_EVOLUTION_READ")
    @GetMapping("/owners")
    public ResponseEntity<List<PhotoEvolutionOwnerDTO>> listOwners(@RequestParam(required = false) String query) {
        logger.debug("REST request to list photo evolution owners");
        return ResponseEntity.ok(service.listOwners(query));
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<Problem> handleJMException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus())).body(problem);
    }
}
