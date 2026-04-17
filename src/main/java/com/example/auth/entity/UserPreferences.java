package com.example.auth.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Builder.Default private String  timezone              = "Asia/Manila";
    @Builder.Default private String  language              = "en";
    @Builder.Default private boolean emailOnNewLogin       = true;
    @Builder.Default private boolean emailOnPasswordChange = true;
    @Builder.Default private boolean emailOnTokenExpiry    = false;
    @Builder.Default private boolean compactMode           = false;
    @Builder.Default private String  theme                 = "dark";
}
