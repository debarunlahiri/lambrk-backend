package com.lambrk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MODERATOR = "MODERATOR";
    private static final List<String> EXPOSED_HEADERS = List.of(
        "Authorization", "Content-Type", "X-Total-Count", "X-Correlation-Id"
    );

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String corsAllowedOrigins;
    private final String corsAllowedMethods;
    private final String corsAllowedHeaders;
    private final boolean corsAllowCredentials;
    private final long corsMaxAge;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${spring.cors.allowed-origins:http://localhost:3000}") String corsAllowedOrigins,
            @Value("${spring.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}") String corsAllowedMethods,
            @Value("${spring.cors.allowed-headers:*}") String corsAllowedHeaders,
            @Value("${spring.cors.allow-credentials:true}") boolean corsAllowCredentials,
            @Value("${spring.cors.max-age:3600}") long corsMaxAge) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsAllowedOrigins = corsAllowedOrigins;
        this.corsAllowedMethods = corsAllowedMethods;
        this.corsAllowedHeaders = corsAllowedHeaders;
        this.corsAllowCredentials = corsAllowCredentials;
        this.corsMaxAge = corsMaxAge;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/health", "/actuator/health").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                .requestMatchers("/api/logs/**").hasRole(ROLE_ADMIN)
                // Moderator endpoints
                .requestMatchers("/api/moderator/**").hasAnyRole(ROLE_MODERATOR, ROLE_ADMIN)
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(parseList(corsAllowedOrigins));
        configuration.setAllowedMethods(parseList(corsAllowedMethods));
        configuration.setAllowedHeaders(parseList(corsAllowedHeaders));
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setAllowCredentials(corsAllowCredentials);
        configuration.setMaxAge(corsMaxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseList(String commaSeparated) {
        return Arrays.asList(commaSeparated.split(","));
    }
}
