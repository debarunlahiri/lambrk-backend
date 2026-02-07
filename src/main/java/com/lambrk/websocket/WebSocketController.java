package com.lambrk.websocket;

import com.lambrk.dto.NotificationResponse;
import com.lambrk.dto.PostResponse;
import com.lambrk.dto.CommentResponse;
import com.lambrk.service.NotificationService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Autowired
    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               NotificationService notificationService) {
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }

    @MessageMapping("/connect")
    @Counted(value = "websocket.connections")
    @Timed(value = "websocket.connect.duration")
    public void handleConnect(Principal principal) {
        // Handle new WebSocket connection
        String username = principal.getName();
        
        // Send connection confirmation
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/connected",
            "Connected to Reddit WebSocket"
        );
        
        // Send unread notifications count
        sendUnreadCount(username);
    }

    @MessageMapping("/subscribe/notifications")
    @Counted(value = "websocket.notifications.subscribed")
    @Timed(value = "websocket.notifications.subscribe.duration")
    public void subscribeToNotifications(Principal principal) {
        String username = principal.getName();
        
        // Send recent notifications
        // This would need to be implemented in NotificationService
        List<NotificationResponse> notifications = List.of(); // Placeholder
        
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            notifications
        );
    }

    @MessageMapping("/subscribe/posts/{postId}")
    @Counted(value = "websocket.posts.subscribed")
    @Timed(value = "websocket.posts.subscribe.duration")
    public void subscribeToPost(@Payload Long postId, Principal principal) {
        // User wants real-time updates for a specific post
        String username = principal.getName();
        
        // Send confirmation
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/post/" + postId + "/subscribed",
            "Subscribed to post updates: " + postId
        );
    }

    @MessageMapping("/subscribe/subreddit/{subredditId}")
    @Counted(value = "websocket.subreddits.subscribed")
    @Timed(value = "websocket.subreddits.subscribe.duration")
    public void subscribeToSubreddit(@Payload Long subredditId, Principal principal) {
        // User wants real-time updates for a specific subreddit
        String username = principal.getName();
        
        // Send confirmation
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/subreddit/" + subredditId + "/subscribed",
            "Subscribed to subreddit updates: " + subredditId
        );
    }

    // Methods for sending real-time updates

    public void sendNotificationUpdate(String username, NotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            notification
        );
    }

    public void sendPostUpdate(String username, PostResponse post) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/posts",
            post
        );
    }

    public void sendCommentUpdate(String username, CommentResponse comment) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/comments",
            comment
        );
    }

    public void broadcastPostUpdate(PostResponse post) {
        messagingTemplate.convertAndSend(
            "/topic/posts/" + post.id(),
            post
        );
    }

    public void broadcastCommentUpdate(CommentResponse comment) {
        messagingTemplate.convertAndSend(
            "/topic/posts/" + comment.postId() + "/comments",
            comment
        );
    }

    public void broadcastSubredditUpdate(Long subredditId, Object update) {
        messagingTemplate.convertAndSend(
            "/topic/subreddits/" + subredditId,
            update
        );
    }

    public void sendUnreadCount(String username) {
        // This would get the actual unread count from NotificationService
        long unreadCount = 0; // Placeholder
        
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications/unread-count",
            unreadCount
        );
    }

    public void sendKarmaUpdate(String username, int newKarma) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/karma",
            newKarma
        );
    }

    public void sendVoteUpdate(String username, Object voteUpdate) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/votes",
            voteUpdate
        );
    }

    public void sendSystemAnnouncement(String message) {
        messagingTemplate.convertAndSend(
            "/topic/announcements",
            message
        );
    }

    public void sendUserStatusUpdate(String username, String status) {
        messagingTemplate.convertAndSend(
            "/topic/user-status/" + username,
            status
        );
    }
}
