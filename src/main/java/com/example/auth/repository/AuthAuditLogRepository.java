package com.example.auth.repository;

import com.example.auth.entity.AuditSeverity;
import com.example.auth.entity.AuthAuditLog;
import com.example.auth.entity.AuthAuditLog.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, Long> {

    @Query("""
        SELECT a FROM AuthAuditLog a
        WHERE (:severity IS NULL OR a.severity = :severity)
          AND (:event    IS NULL OR a.event    = :event)
          AND (:search   IS NULL
               OR LOWER(a.actor)  LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(a.target) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(a.detail) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY a.createdAt DESC
        """)
    Page<AuthAuditLog> findFiltered(AuditSeverity severity, AuditEvent event,
                                    String search, Pageable pageable);

    long countBySeverity(AuditSeverity severity);
}
