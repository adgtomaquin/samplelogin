package com.example.auth.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rate_limit_buckets")
public class RateLimitBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bucketKey;
    private String endpoint;
    private int requestCount = 0;
    private OffsetDateTime windowStart;
    private OffsetDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        windowStart = OffsetDateTime.now();
    }
}