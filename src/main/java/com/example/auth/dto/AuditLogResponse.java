package com.example.auth.dto;

import com.example.auth.entity.AuditSeverity;
import com.example.auth.entity.AuthAuditLog.AuditEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {
    private String id;
    private AuditEvent action;
    private AuditSeverity severity;
    private String actor;
    private String target;
    private String ip;
    private String location;
    private String device;
    private OffsetDateTime timestamp;
    private String details;
}
