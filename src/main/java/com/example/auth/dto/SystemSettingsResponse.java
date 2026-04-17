package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemSettingsResponse {
    private int lockoutThreshold;
    private int lockoutDurationMinutes;
    private int accessTokenTtlSeconds;
    private int refreshTokenTtlDays;
    private int rememberMeTtlDays;
    private boolean requireMfa;
    private String allowedDomains;
    private int sessionConcurrencyLimit;
    private int auditRetentionDays;
}
