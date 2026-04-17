package com.example.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateSystemSettingsRequest {
    @Min(1) @Max(20)   private Integer lockoutThreshold;
    @Min(1) @Max(1440) private Integer lockoutDurationMinutes;
    @Min(60) @Max(3600) private Integer accessTokenTtlSeconds;
    @Min(1) @Max(90)   private Integer refreshTokenTtlDays;
    @Min(7) @Max(365)  private Integer rememberMeTtlDays;
    private Boolean requireMfa;
    private String allowedDomains;
    @Min(1) @Max(20)   private Integer sessionConcurrencyLimit;
    @Min(7) @Max(365)  private Integer auditRetentionDays;
}
