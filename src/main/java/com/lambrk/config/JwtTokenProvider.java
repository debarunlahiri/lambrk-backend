package com.lambrk.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String METRIC_GENERATION = "jwt.token.generation";
    private static final String METRIC_VALIDATION = "jwt.token.validation";
    private static final String METRIC_ERROR = "jwt.validation.error";

    private final MeterRegistry meterRegistry;
    private final Timer tokenGenerationTimer;
    private final Timer tokenValidationTimer;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400}")
    private int jwtExpirationInSeconds;

    @Value("${app.jwt.refresh-expiration:604800}")
    private int jwtRefreshExpirationInSeconds;

    @Value("${app.jwt.issuer:reddit-backend}")
    private String jwtIssuer;

    @Value("${app.jwt.audience:reddit-frontend}")
    private String jwtAudience;

    private SecretKey key;

    public JwtTokenProvider(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.tokenGenerationTimer = Timer.builder(METRIC_GENERATION)
            .description("Time taken to generate JWT tokens")
            .register(meterRegistry);
        this.tokenValidationTimer = Timer.builder(METRIC_VALIDATION)
            .description("Time taken to validate JWT tokens")
            .register(meterRegistry);
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        return tokenGenerationTimer.recordCallable(() -> {
            UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
            Instant now = Instant.now();
            Instant expiryDate = now.plus(jwtExpirationInSeconds, ChronoUnit.SECONDS);

            return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .claim("roles", userPrincipal.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .toList())
                .claim("tokenType", "access")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        });
    }

    public String generateRefreshToken(String username) {
        return tokenGenerationTimer.recordCallable(() -> {
            Instant now = Instant.now();
            Instant expiryDate = now.plus(jwtRefreshExpirationInSeconds, ChronoUnit.SECONDS);

            return Jwts.builder()
                .setSubject(username)
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .claim("tokenType", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        });
    }

    public String getUsernameFromJWT(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public List<String> getRolesFromJWT(String token) {
        return getClaimFromToken(token, claims -> claims.get("roles", List.class));
    }

    public String getTokenTypeFromJWT(String token) {
        return getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
    }

    public Instant getExpirationDateFromJWT(String token) {
        return getClaimFromToken(token, Claims::getExpiration).toInstant();
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claimsResolver.apply(claims);
    }

    public boolean validateToken(String token) {
        return tokenValidationTimer.recordCallable(() -> {
            try {
                Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
                return true;
            } catch (SecurityException ex) {
                meterRegistry.counter("jwt.validation.error", "type", "security").increment();
                return false;
            } catch (MalformedJwtException ex) {
                meterRegistry.counter("jwt.validation.error", "type", "malformed").increment();
                return false;
            } catch (ExpiredJwtException ex) {
                meterRegistry.counter("jwt.validation.error", "type", "expired").increment();
                return false;
            } catch (UnsupportedJwtException ex) {
                meterRegistry.counter("jwt.validation.error", "type", "unsupported").increment();
                return false;
            } catch (IllegalArgumentException ex) {
                meterRegistry.counter("jwt.validation.error", "type", "illegal").increment();
                return false;
            }
        });
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenTypeFromJWT(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenTypeFromJWT(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return getExpirationDateFromJWT(token).isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }

    public String refreshToken(String refreshToken) {
        if (!validateToken(refreshToken) || !isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = getUsernameFromJWT(refreshToken);
        return generateTokenFromUsername(username);
    }

    private String generateTokenFromUsername(String username) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtExpirationInSeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiryDate))
            .claim("tokenType", "access")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }
}
