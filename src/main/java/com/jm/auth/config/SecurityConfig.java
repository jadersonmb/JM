package com.jm.auth.config;

import com.jm.auth.filter.JwtAuthFilter;
import com.jm.auth.model.AuthenticatedUser;
import com.jm.entity.Role;
import com.jm.entity.Users;
import com.jm.repository.UserRepository;
import com.jm.security.entity.Permission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/public/**",
                                "/public/api/v1/webhook/whatsapp",
                                "/public/api/v1/webhook/whatsapp/**",
                                "/swagger",
                                "/swagger/**",
                                "/api-docs/**",
                                "/api/v1/plans",
                                "/api/v1/plans/**",
                                "/v1/api/payment-plans",
                                "/v1/api/payment-plans/**",
                                "/api/v1/users",
                                "/api/v1/users/**",
                                "/v1/api/payments/subscription",
                                "/v1/api/payments/subscription/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            Users user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Set<GrantedAuthority> authorities = new HashSet<>();

            user.getRoles().stream()
                    .filter(Objects::nonNull)
                    .forEach(role -> {
                        String roleName = role.getName();
                        if (roleName != null && !roleName.isBlank()) {
                            String normalizedRole = roleName.startsWith("ROLE_")
                                    ? roleName.toUpperCase(Locale.ROOT)
                                    : "ROLE_" + roleName.toUpperCase(Locale.ROOT);
                            authorities.add(new SimpleGrantedAuthority(normalizedRole));
                        }

                        if (role.getPermissions() != null) {
                            role.getPermissions().stream()
                                    .filter(Objects::nonNull)
                                    .map(Permission::getCode)
                                    .filter(code -> code != null && !code.isBlank())
                                    .map(code -> code.toUpperCase(Locale.ROOT))
                                    .map(SimpleGrantedAuthority::new)
                                    .forEach(authorities::add);
                        }
                    });

            return new AuthenticatedUser(user.getId(), user.getEmail(), user.getPassword(), authorities);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
