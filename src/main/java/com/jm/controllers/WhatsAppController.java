package com.jm.controllers;

import com.jm.dto.WhatsAppRegisterDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.WhatsAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/whatsapp")
@AllArgsConstructor
@Tag(name = "WhatsApp", description = "Operations for WhatsApp integration")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUserPhone(@Valid @RequestBody WhatsAppRegisterDTO dto) {
        whatsAppService.registerAndWelcomeUser(dto.getUserId());
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<Problem> handleException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .title(ex.getTitle())
                .details(ex.getDetails())
                .type(ex.getType())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }
}
