package com.jm.controllers;

import com.jm.dto.LoginRequestDTO;
import com.jm.dto.RecoverPasswordRequest;
import com.jm.dto.TokenRefreshRequestDTO;
import com.jm.dto.TokenResponseDTO;
import com.jm.dto.UserDTO;
import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.execption.ProblemType;
import com.jm.services.AuthService;
import com.jm.services.UserService;
import com.jm.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication API")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO login) {
        logger.debug("Login attempt for {}", login.getEmail());
        return ResponseEntity.ok(authService.authenticate(login));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody TokenRefreshRequestDTO request) {
        logger.debug("Refreshing token");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/recover-password")
    public ResponseEntity<UserDTO> recoverPassword(@Valid @RequestBody RecoverPasswordRequest request) throws JMException {
        logger.debug("Password recovery request for {}", request.getEmail());
        UserDTO dto = authService.recoverPassword(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> me() throws JMException {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new JMException(HttpStatus.UNAUTHORIZED.value(),
                        ProblemType.INVALID_TOKEN.getUri(),
                        ProblemType.INVALID_TOKEN.getTitle(),
                        "Authentication required"));
        return ResponseEntity.ok(userService.findById(userId));
    }

    @ExceptionHandler(JMException.class)
    public ResponseEntity<?> handleJMException(JMException ex) {
        Problem problem = Problem.builder()
                .status(ex.getStatus())
                .details(ex.getDetails())
                .type(ex.getType())
                .title(ex.getTitle())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(problem);
    }
}
