package com.example.auth.dto;

import com.example.auth.entity.NotifType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private String id;
    private NotifType type;
    private String title;
    private String message;
    private String time;     // human-readable relative string
    private boolean read;
    private String userId;   // target user id or "all"
}
