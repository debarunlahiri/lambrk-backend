package com.lambrk.config;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final Tracer tracer;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
                                   Tracer tracer) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            Optional<String> jwt = getJwtFromRequest(request);

            if (jwt.isPresent() && tokenProvider.validateToken(jwt.get())) {
                String username = tokenProvider.getUsernameFromJWT(jwt.get());
                List<String> roles = tokenProvider.getRolesFromJWT(jwt.get());
                java.util.UUID userId = tokenProvider.getUserIdFromJWT(jwt.get());
                List<SimpleGrantedAuthority> authorities = (roles != null ? roles : List.<String>of()).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

                UserPrincipal userPrincipal;
                if (userId != null) {
                    userPrincipal = new UserPrincipal(userId, username, "", authorities);
                } else {
                    // Fallback for old tokens without userId claim: load from DB
                    org.springframework.security.core.userdetails.UserDetails ud =
                        userDetailsService.loadUserByUsername(username);
                    if (ud instanceof UserPrincipal) {
                        userPrincipal = (UserPrincipal) ud;
                    } else {
                        userPrincipal = new UserPrincipal(null, username, "", authorities);
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (tracer.currentSpan() != null) {
                    tracer.currentSpan().tag("user.id", username);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        // WebSocket/SockJS connections pass token as query parameter
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return Optional.of(tokenParam);
        }
        return Optional.empty();
    }
}
