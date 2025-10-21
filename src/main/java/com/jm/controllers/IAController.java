package com.jm.controllers;


import com.jm.dto.ChatRequest;
import com.jm.dto.ChatResponse;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.DeepSeekService;
import com.jm.services.GeminiService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@Data
@AllArgsConstructor
@Tag(name = "AI", description = "AI-powered interactions and assistants")
@SecurityRequirement(name = "BearerAuth")
public class IAController {

    private final DeepSeekService deepSeekService;
    private final GeminiService geminiService;

    @PermissionRequired("ROLE_AI_OPERATIONS_EXECUTE")
    @PostMapping("/deepseek/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest chatRequest) {
        ChatResponse response = deepSeekService.sendMessage(chatRequest.getMessage());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PermissionRequired("ROLE_AI_OPERATIONS_EXECUTE")
    @PostMapping("/gemini/chat")
    public Mono<ResponseEntity<String>> chatGemini(@Valid @RequestBody ChatRequest chatRequest) {
        return geminiService.generateTextFromPrompt(chatRequest.getMessage())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar texto: " + e.getMessage())));
    }

    @PermissionRequired("ROLE_AI_OPERATIONS_EXECUTE")
    @PostMapping("/gemini/image")
    public Mono<ResponseEntity<String>> processImageGemini(@RequestParam("prompt") String prompt,
                                                     @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("Por favor, selecione um arquivo para upload."));
        }

        try {
            byte[] imageBytes = file.getBytes();
            String mimeType = file.getContentType();
            if (mimeType == null || (!mimeType.startsWith("image/jpeg") && !mimeType.startsWith("image/png") && !mimeType.startsWith("image/gif"))) {
                return Mono.just(ResponseEntity.badRequest().body("Formato de imagem não suportado. Use JPEG, PNG ou GIF."));
            }

            return geminiService.generateTextFromImage(prompt, imageBytes, mimeType)
                    .map(ResponseEntity::ok)
                    .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar imagem: " + e.getMessage())));

        } catch (IOException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao ler o arquivo: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is running!");
    }

    @ExceptionHandler({JMException.class})
    public ResponseEntity<Object> Exception(JMException ex) {
        Problem problem = createProblemBuild(ex.getStatus(), ex.getDetails(), ex.getType(), ex.getTitle())
                .build();
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
