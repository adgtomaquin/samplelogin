package com.example.auth.entity;

import lombok.*;
import jakarta.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    private Long id = 1L;

    @Builder.Default private int lockoutThreshold         = 5;
    @Builder.Default private int lockoutDurationMinutes   = 15;
    @Builder.Default private int accessTokenTtlSeconds    = 900;
    @Builder.Default private int refreshTokenTtlDays      = 7;
    @Builder.Default private int rememberMeTtlDays        = 30;
    @Builder.Default private boolean requireMfa           = false;
    @Builder.Default private String allowedDomains        = "example.com, corp.com";
    @Builder.Default private int sessionConcurrencyLimit  = 5;
    @Builder.Default private int auditRetentionDays       = 90;
}
