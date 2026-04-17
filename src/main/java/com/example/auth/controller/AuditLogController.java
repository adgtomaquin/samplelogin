package com.example.auth.controller;

import com.example.auth.dto.AuditLogListResponse;
import com.example.auth.dto.AuditStatsResponse;
import com.example.auth.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<AuditLogListResponse> list(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "8") int pageSize) {
        return ResponseEntity.ok(auditLogService.getLogs(severity, action, search, page, pageSize));
    }

    @GetMapping("/stats")
    public ResponseEntity<AuditStatsResponse> stats() {
        return ResponseEntity.ok(auditLogService.getStats());
    }
}
