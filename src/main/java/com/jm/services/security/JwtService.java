package com.jm.services.security;

import com.jm.dto.TokenResponseDTO;
import com.jm.dto.UserDTO;
import com.jm.entity.Role;
import com.jm.entity.Users;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.mappers.UserMapper;
import com.jm.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationTime;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1h

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageSource messageSource;

    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(Role::getName).toList());
        claims.put("userId", user.getId().toString());
        claims.put("name", user.getName());
        return createToken(claims, user.getEmail(), EXPIRATION_TIME);
    }

    public String generateRefreshToken(Users user) {
        Map<String, Object> claims = Map.of("type", "refresh");
        return createToken(claims, user.getEmail(), refreshExpirationTime);
    }

    public long getExpirationTime() {
        return EXPIRATION_TIME / 1000;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public TokenResponseDTO refreshToken(String refreshToken) {
        try {
            Claims claims = extractAllClaims(refreshToken);
            if (claims.getExpiration().before(new Date())) {
                throw expiredToken();
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

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
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

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private JMException invalidToken() {
        return new JMException(
                HttpStatus.UNAUTHORIZED.value(),
                ProblemType.INVALID_TOKEN.getUri(),
                ProblemType.INVALID_TOKEN.getTitle(),
                messageSource.getMessage(ProblemType.INVALID_TOKEN.getMessageSource(), null, LocaleContextHolder.getLocale())
        );
    }

    private JMException expiredToken() {
        return new JMException(
                HttpStatus.UNAUTHORIZED.value(),
                ProblemType.EXPIRED_TOKEN.getUri(),
                ProblemType.EXPIRED_TOKEN.getTitle(),
                messageSource.getMessage(ProblemType.EXPIRED_TOKEN.getMessageSource(), null, LocaleContextHolder.getLocale())
        );
    }

    private JMException userNotFound() {
        return new JMException(
                HttpStatus.NOT_FOUND.value(),
                ProblemType.USER_NOT_FOUND.getUri(),
                ProblemType.USER_NOT_FOUND.getTitle(),
                messageSource.getMessage(ProblemType.USER_NOT_FOUND.getMessageSource(), null, LocaleContextHolder.getLocale())
        );
    }
}
