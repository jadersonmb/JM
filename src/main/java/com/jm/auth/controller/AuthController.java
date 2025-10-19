package com.jm.auth.controller;

import com.jm.auth.dto.LoginRequestDTO;
import com.jm.auth.dto.TokenRefreshRequestDTO;
import com.jm.auth.dto.TokenResponseDTO;
import com.jm.auth.service.AuthService;
import com.jm.dto.RecoverPasswordRequest;
import com.jm.dto.UserDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO login) {
        return ResponseEntity.ok(authService.authenticate(login));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody TokenRefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me() {
        return ResponseEntity.ok(authService.getAuthenticatedUser());
    }

    @PostMapping("/recover-password")
    public ResponseEntity<UserDTO> recoverPassword(@Valid @RequestBody RecoverPasswordRequest request) throws JMException {
        return ResponseEntity.ok(authService.recoverPassword(request));
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<Problem> handleJMException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }
}
