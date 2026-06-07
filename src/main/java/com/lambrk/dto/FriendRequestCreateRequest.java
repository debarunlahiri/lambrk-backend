package com.lambrk.dto;

import jakarta.validation.constraints.Size;

public record FriendRequestCreateRequest(
    @Size(max = 50, message = "Source must be at most 50 characters")
    String source,

    @Size(max = 280, message = "Message must be at most 280 characters")
    String message
) {}
