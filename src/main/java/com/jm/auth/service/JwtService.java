package com.jm.auth.service;

import com.jm.auth.dto.TokenResponseDTO;
import com.jm.auth.model.RefreshToken;
import com.jm.dto.UserDTO;
import com.jm.entity.Role;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.UserMapper;
import com.jm.repository.UserRepository;
import com.jm.security.entity.Permission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationTime;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageSource messageSource;

    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", extractRoles(user));
        claims.put("permissions", extractPermissions(user));
        claims.put("userId", user.getId() != null ? user.getId().toString() : null);
        claims.put("name", user.getName());
        return createToken(claims, user.getEmail(), EXPIRATION_TIME);
    }

    public String generateRefreshToken(Users user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(createToken(Map.of("type", "refresh"), user.getEmail(), refreshExpirationTime))
                .expiresAt(Instant.ofEpochMilli(System.currentTimeMillis() + refreshExpirationTime))
                .build();
        return refreshToken.getToken();
    }

    public long getExpirationTime() {
        return EXPIRATION_TIME / 1000;
    }

    public TokenResponseDTO refreshToken(String refreshToken) {
        try {
            Claims claims = extractAllClaims(refreshToken);
            if (claims.getExpiration().before(new Date())) {
                throw expiredToken();
            }
            if (!"refresh".equals(claims.get("type"))) {
                throw invalidToken();
            }

            String email = claims.getSubject();
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(this::userNotFound);

            String newAccessToken = generateToken(user);
            String newRefreshToken = generateRefreshToken(user);

            UserDTO dto = userMapper.toDTO(user);
            if (dto != null) {
                dto.setPassword(null);
            }

            return new TokenResponseDTO(newAccessToken, newRefreshToken, getExpirationTime(), dto);
        } catch (ExpiredJwtException ex) {
            throw expiredToken();
        } catch (JwtException | IllegalArgumentException ex) {
            throw invalidToken();
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        String tokenUsername = extractUsername(token);
        return tokenUsername.equalsIgnoreCase(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Set<String> extractRoles(Users user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> extractPermissions(Users user) {
        return user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    private JMException invalidToken() {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(ProblemType.INVALID_TOKEN.getMessageSource(), null, locale);
        return new JMException(HttpStatus.UNAUTHORIZED.value(), ProblemType.INVALID_TOKEN.getUri(),
                ProblemType.INVALID_TOKEN.getTitle(), message);
    }

    private JMException expiredToken() {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("auth.token.expired", null, locale);
        return new JMException(HttpStatus.UNAUTHORIZED.value(), ProblemType.EXPIRED_TOKEN.getUri(),
                ProblemType.EXPIRED_TOKEN.getTitle(), message);
    }

    private JMException userNotFound() {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(ProblemType.USER_NOT_FOUND.getMessageSource(), null, locale);
        return new JMException(HttpStatus.NOT_FOUND.value(), ProblemType.USER_NOT_FOUND.getUri(),
                ProblemType.USER_NOT_FOUND.getTitle(), message);
    }
}
