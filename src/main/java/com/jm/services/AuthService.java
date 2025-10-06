package com.jm.services;

import com.jm.dto.LoginRequest;
import com.jm.dto.LoginResponse;
import com.jm.dto.RecoverPasswordRequest;
import com.jm.dto.UserDTO;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.mappers.UserMapper;
import com.jm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

        private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

        private final AuthenticationManager authenticationManager;
        private final JwtEncoder jwtEncoder;
        private final UserRepository userRepository;
        private final UserService userService;
        private final UserMapper userMapper;

        public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
                        UserRepository userRepository, UserService userService, UserMapper userMapper) {
                this.authenticationManager = authenticationManager;
                this.jwtEncoder = jwtEncoder;
                this.userRepository = userRepository;
                this.userService = userService;
                this.userMapper = userMapper;
        }

        public LoginResponse login(LoginRequest request) {
                logger.debug("Authenticating user {}", request.getEmail());
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                Users user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                Instant now = Instant.now();
                Instant expiresAt = now.plus(1, ChronoUnit.HOURS);

                Set<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toSet());

                JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer("jm-api")
                                .issuedAt(now)
                                .expiresAt(expiresAt)
                                .subject(user.getId().toString())
                                .claim("email", user.getEmail())
                                .claim("name", user.getName())
                                .claim("role", user.getType() != null ? user.getType().name()
                                                : Users.Type.CLIENT.name())
                                .claim("authorities", authorities)
                                .build();

                String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
                UserDTO userDTO = userMapper.toDTO(user);

                return new LoginResponse(tokenValue, "Bearer", ChronoUnit.SECONDS.between(now, expiresAt), userDTO);
        }

        public UserDTO recoverPassword(RecoverPasswordRequest request) throws JMException {
                logger.info("Recovering password for {}", request.getEmail());
                return userService.updatePasswordByEmail(request.getEmail(), request.getNewPassword());
        }
}