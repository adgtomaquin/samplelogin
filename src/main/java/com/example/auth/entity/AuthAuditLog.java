package com.example.auth.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_audit_log")
public class AuthAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AuditEvent event;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuditSeverity severity = AuditSeverity.info;

    private String actor;       // email of the acting user
    private String target;      // email of the affected user (if different)
    private String ipAddress;
    private String location;
    private String userAgent;
    private String device;      // device + browser string (e.g. "MacBook Pro / Chrome 124")
    private String requestId;
    private String detail;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public enum AuditEvent {
        LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, LOGOUT_ALL,
        TOKEN_REFRESH, TOKEN_EXPIRED,
        PASSWORD_CHANGE, PASSWORD_RESET,
        USER_CREATED, USER_UPDATED, USER_DELETED,
        USER_LOCKED, USER_UNLOCKED,
        SETTINGS_CHANGED, INVITE_SENT,
        // Legacy aliases kept for backward-compat
        LOGIN_OK, LOGIN_FAIL, REFRESH,
        ACCOUNT_LOCKED, REPLAY_DETECTED
    }
}
