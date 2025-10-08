package com.jm.controllers;

import com.jm.dto.LoginRequest;
import com.jm.dto.LoginResponse;
import com.jm.dto.RecoverPasswordRequest;
import com.jm.dto.UserDTO;
import com.jm.execption.JMException;
import com.jm.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.debug("Login attempt for {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recoverPassword")
    public ResponseEntity<UserDTO> recoverPassword(@Valid @RequestBody RecoverPasswordRequest request)
            throws JMException {
        logger.debug("Password recovery request for {}", request.getEmail());
        UserDTO dto = authService.recoverPassword(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }
}