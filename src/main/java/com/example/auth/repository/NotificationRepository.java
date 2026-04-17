package com.example.auth.repository;

import com.example.auth.entity.Notification;
import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // All notifications for a user: their own + broadcasts (user IS NULL)
    @Query("SELECT n FROM Notification n WHERE n.user = :user OR n.user IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findForUser(User user);

    @Query("SELECT n FROM Notification n WHERE (n.user = :user OR n.user IS NULL) AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadForUser(User user);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.user = :user OR n.user IS NULL) AND n.read = false")
    long countUnreadForUser(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user OR n.user IS NULL")
    void markAllReadForUser(User user);
}
