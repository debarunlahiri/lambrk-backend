package com.lambrk.websocket;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    private final Map<String, String> connectedUsers = new ConcurrentHashMap<>();

    @EventListener
    @Counted(value = "websocket.connected")
    @Timed(value = "websocket.connect.event.duration")
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        
        if (user != null) {
            String username = user.getName();
            String sessionId = headerAccessor.getSessionId();
            
            connectedUsers.put(sessionId, username);
            
            System.out.println("User connected: " + username + " with session: " + sessionId);
            
            // Could trigger additional logic like:
            // - Update user online status
            // - Send welcome message
            // - Initialize user-specific data
        }
    }

    @EventListener
    @Counted(value = "websocket.disconnected")
    @Timed(value = "websocket.disconnect.event.duration")
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        String username = connectedUsers.remove(sessionId);
        
        if (username != null) {
            System.out.println("User disconnected: " + username + " with session: " + sessionId);
            
            // Could trigger additional logic like:
            // - Update user offline status
            // - Clean up user-specific resources
            // - Notify other users about disconnection
        }
    }

    @EventListener
    @Counted(value = "websocket.subscribed")
    @Timed(value = "websocket.subscribe.event.duration")
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        String destination = headerAccessor.getDestination();
        
        if (user != null) {
            String username = user.getName();
            System.out.println("User " + username + " subscribed to: " + destination);
            
            // Handle subscription to specific topics
            handleSubscription(username, destination);
        }
    }

    @EventListener
    @Counted(value = "websocket.unsubscribed")
    @Timed(value = "websocket.unsubscribe.event.duration")
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        String destination = headerAccessor.getDestination();
        
        if (user != null) {
            String username = user.getName();
            System.out.println("User " + username + " unsubscribed from: " + destination);
            
            // Handle unsubscription from specific topics
            handleUnsubscription(username, destination);
        }
    }

    private void handleSubscription(String username, String destination) {
        // Handle different subscription types
        if (destination.startsWith("/topic/posts/")) {
            // User subscribed to post updates
            String postId = destination.substring("/topic/posts/".length());
            System.out.println("User " + username + " subscribed to post: " + postId);
            
        } else if (destination.startsWith("/topic/subreddits/")) {
            // User subscribed to subreddit updates
            String subredditId = destination.substring("/topic/subreddits/".length());
            System.out.println("User " + username + " subscribed to subreddit: " + subredditId);
            
        } else if (destination.equals("/topic/announcements")) {
            // User subscribed to system announcements
            System.out.println("User " + username + " subscribed to announcements");
            
        } else if (destination.startsWith("/topic/user-status/")) {
            // User subscribed to another user's status updates
            String targetUser = destination.substring("/topic/user-status/".length());
            System.out.println("User " + username + " subscribed to status updates for: " + targetUser);
        }
    }

    private void handleUnsubscription(String username, String destination) {
        // Handle different unsubscription types
        if (destination.startsWith("/topic/posts/")) {
            String postId = destination.substring("/topic/posts/".length());
            System.out.println("User " + username + " unsubscribed from post: " + postId);
            
        } else if (destination.startsWith("/topic/subreddits/")) {
            String subredditId = destination.substring("/topic/subreddits/".length());
            System.out.println("User " + username + " unsubscribed from subreddit: " + subredditId);
        }
    }

    public int getConnectedUserCount() {
        return connectedUsers.size();
    }

    public boolean isUserConnected(String username) {
        return connectedUsers.containsValue(username);
    }
}
