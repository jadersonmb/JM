// com.jm.controllers.OllamaController
package com.jm.controllers;

import com.jm.dto.OllamaRequestDTO;
import com.jm.dto.OllamaResponseDTO;
import com.jm.entity.Ollama;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.OllamaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ollama")
@AllArgsConstructor
@Tag(name = "Ollama", description = "Endpoints to interact with the Ollama API")
public class OllamaController {

    private static final Logger logger = LoggerFactory.getLogger(OllamaController.class);
    private final OllamaService service;

    @PermissionRequired("ROLE_OLLAMA_EXECUTE")
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generate(@RequestBody OllamaRequestDTO request) {
        OllamaResponseDTO response = service.generate(request);
        return ResponseEntity.ok(response);
    }

    @PermissionRequired("ROLE_OLLAMA_EXECUTE")
    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody OllamaRequestDTO dto) {
        service.createAndDispatchRequest(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PermissionRequired("ROLE_OLLAMA_EXECUTE")
    @PostMapping(value = "/generate-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generateWithImage(
            @RequestPart("model") String model,
            @RequestPart("prompt") String prompt,
            @RequestPart(value = "stream", required = false) Boolean stream,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        OllamaRequestDTO dto = OllamaRequestDTO.builder()
                .model(model)
                .prompt(prompt)
                .stream(stream != null ? stream : Boolean.FALSE)
                .images(service.encodeImages(images))
                .build();

        logger.info("REST request to Ollama (with images) model={}", model);
        OllamaResponseDTO response = service.generate(dto);
        return ResponseEntity.ok(response);
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
