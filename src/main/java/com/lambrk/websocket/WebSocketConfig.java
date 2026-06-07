package com.lambrk.websocket;

import com.lambrk.config.JwtTokenProvider;
import com.lambrk.config.UserPrincipal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

  private final JwtTokenProvider tokenProvider;
  private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  public WebSocketConfig(
      JwtTokenProvider tokenProvider,
      org.springframework.security.core.userdetails.UserDetailsService userDetailsService) {
    this.tokenProvider = tokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setHandshakeHandler(jwtHandshakeHandler())
        .setAllowedOriginPatterns("*")
        .withSockJS();

    registry
        .addEndpoint("/ws")
        .setHandshakeHandler(jwtHandshakeHandler())
        .setAllowedOriginPatterns("*");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        new ChannelInterceptor() {
          @Override
          public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor == null) {
              return message;
            }
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
              authenticateConnectFrame(accessor);
            }
            return message;
          }
        });
  }

  private void authenticateConnectFrame(StompHeaderAccessor accessor) {
    String token = resolveBearerToken(accessor);
    if (token == null) {
      return;
    }

    try {
      UsernamePasswordAuthenticationToken authentication = buildAuthentication(token);
      accessor.setUser(authentication);
      log.debug("Authenticated WebSocket CONNECT for {}", authentication.getName());
    } catch (Exception ex) {
      log.warn("Could not authenticate WebSocket CONNECT frame: {}", ex.getMessage());
    }
  }

  private String resolveBearerToken(StompHeaderAccessor accessor) {
    String bearerToken = accessor.getFirstNativeHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    String token = accessor.getFirstNativeHeader("token");
    if (token == null || token.isBlank()) {
      token = accessor.getFirstNativeHeader("access_token");
    }
    return token != null && !token.isBlank() ? token : null;
  }

  private DefaultHandshakeHandler jwtHandshakeHandler() {
    return new DefaultHandshakeHandler() {
      @Override
      protected Principal determineUser(
          org.springframework.http.server.ServerHttpRequest request,
          WebSocketHandler wsHandler,
          Map<String, Object> attributes) {

        String token = resolveHandshakeToken(request);
        if (token == null) {
          return super.determineUser(request, wsHandler, attributes);
        }

        try {
          UsernamePasswordAuthenticationToken authentication = buildAuthentication(token);
          log.debug("Authenticated WebSocket handshake for {}", authentication.getName());
          return authentication;
        } catch (Exception ex) {
          log.warn("Could not authenticate WebSocket handshake: {}", ex.getMessage());
          return super.determineUser(request, wsHandler, attributes);
        }
      }
    };
  }

  private String resolveHandshakeToken(org.springframework.http.server.ServerHttpRequest request) {
    String bearerToken = request.getHeaders().getFirst("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    var queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
    String token = queryParams.getFirst("token");
    if (token == null || token.isBlank()) {
      token = queryParams.getFirst("access_token");
    }
    return token != null && !token.isBlank() ? token : null;
  }

  private UsernamePasswordAuthenticationToken buildAuthentication(String token) {
    if (!tokenProvider.validateToken(token) || !tokenProvider.isAccessToken(token)) {
      throw new IllegalArgumentException("Invalid access token");
    }

    String username = tokenProvider.getUsernameFromJWT(token);
    List<String> roles = tokenProvider.getRolesFromJWT(token);
    java.util.UUID userId = tokenProvider.getUserIdFromJWT(token);
    List<SimpleGrantedAuthority> authorities =
        (roles != null ? roles : List.<String>of())
            .stream().map(SimpleGrantedAuthority::new).toList();

    UserPrincipal userPrincipal;
    if (userId != null) {
      userPrincipal = new UserPrincipal(userId, username, "", authorities);
    } else {
      org.springframework.security.core.userdetails.UserDetails userDetails =
          userDetailsService.loadUserByUsername(username);
      if (userDetails instanceof UserPrincipal up) {
        userPrincipal = up;
      } else {
        userPrincipal = new UserPrincipal(null, username, "", authorities);
      }
    }

    return new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());
  }
}
