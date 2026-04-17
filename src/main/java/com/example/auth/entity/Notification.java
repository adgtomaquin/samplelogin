package com.example.auth.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    private NotifType type;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    private boolean read = false;

    // null means broadcast to all users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
