package com.example.auth.service;

import com.example.auth.dto.AuditLogListResponse;
import com.example.auth.dto.AuditLogResponse;
import com.example.auth.dto.AuditStatsResponse;
import com.example.auth.entity.AuditSeverity;
import com.example.auth.entity.AuthAuditLog;
import com.example.auth.entity.AuthAuditLog.AuditEvent;
import com.example.auth.repository.AuthAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuthAuditLogRepository auditLogRepository;

    public AuditLogListResponse getLogs(String severity, String action,
                                        String search, int page, int pageSize) {
        AuditSeverity sev   = severity != null ? AuditSeverity.valueOf(severity) : null;
        AuditEvent    event = action   != null ? AuditEvent.valueOf(action)      : null;

        Page<AuthAuditLog> results = auditLogRepository.findFiltered(
                sev, event, search, PageRequest.of(page - 1, pageSize));

        List<AuditLogResponse> logs = results.getContent()
                .stream().map(this::toDto).toList();

        return AuditLogListResponse.builder()
                .logs(logs)
                .total(results.getTotalElements())
                .build();
    }

    public AuditStatsResponse getStats() {
        long total    = auditLogRepository.count();
        long info     = auditLogRepository.countBySeverity(AuditSeverity.info);
        long warning  = auditLogRepository.countBySeverity(AuditSeverity.warning);
        long critical = auditLogRepository.countBySeverity(AuditSeverity.critical);

        return AuditStatsResponse.builder()
                .total(total).info(info).warning(warning).critical(critical)
                .build();
    }

    private AuditLogResponse toDto(AuthAuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId().toString())
                .action(a.getEvent())
                .severity(a.getSeverity())
                .actor(a.getActor())
                .target(a.getTarget())
                .ip(a.getIpAddress())
                .location(a.getLocation())
                .device(a.getDevice())       // ← now uses dedicated device field
                .timestamp(a.getCreatedAt())
                .details(a.getDetail())
                .build();
    }
}
