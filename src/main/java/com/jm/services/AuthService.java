package com.jm.services;

import com.jm.dto.LoginRequestDTO;
import com.jm.dto.RecoverPasswordRequest;
import com.jm.dto.TokenRefreshRequestDTO;
import com.jm.dto.TokenResponseDTO;
import com.jm.dto.UserDTO;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.UserMapper;
import com.jm.repository.UserRepository;
import com.jm.services.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

        private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final UserService userService;
        private final UserMapper userMapper;
        private final MessageSource messageSource;

        public TokenResponseDTO authenticate(LoginRequestDTO request) {
                try {
                        logger.debug("Authenticating user {}", request.getEmail());
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                        Users user = userRepository.findByEmail(request.getEmail())
                                        .orElseThrow(this::userNotFoundException);

                        String accessToken = jwtService.generateToken(user);
                        String refreshToken = jwtService.generateRefreshToken(user);

                        UserDTO userDTO = userMapper.toDTO(user);
                        if (userDTO != null) {
                                userDTO.setPassword(null);
                        }

                        return new TokenResponseDTO(accessToken, refreshToken, jwtService.getExpirationTime(), userDTO);
                } catch (BadCredentialsException ex) {
                        throw invalidCredentials();
                }
        }

        public TokenResponseDTO refreshToken(TokenRefreshRequestDTO request) {
                return jwtService.refreshToken(request.getRefreshToken());
        }

        public UserDTO recoverPassword(RecoverPasswordRequest request) throws JMException {
                logger.info("Recovering password for {}", request.getEmail());
                return userService.updatePasswordByEmail(request.getEmail(), request.getNewPassword());
        }

        private JMException userNotFoundException() {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage(ProblemType.USER_NOT_FOUND.getMessageSource(), null, locale);
                return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.USER_NOT_FOUND.getUri(),
                                ProblemType.USER_NOT_FOUND.getTitle(), message);
        }

        private JMException invalidCredentials() {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("auth.invalid.credentials", null, locale);
                return new JMException(HttpStatus.UNAUTHORIZED.value(), ProblemType.INVALID_CREDENTIALS.getUri(),
                                ProblemType.INVALID_CREDENTIALS.getTitle(), message);
        }
}