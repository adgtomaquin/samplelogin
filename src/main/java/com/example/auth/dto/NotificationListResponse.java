package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private long unreadCount;
}
