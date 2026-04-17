package com.example.auth.controller;

import com.example.auth.dto.SystemSettingsResponse;
import com.example.auth.dto.UpdateSystemSettingsRequest;
import com.example.auth.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/system")
    public ResponseEntity<SystemSettingsResponse> get() {
        return ResponseEntity.ok(settingsService.get());
    }

    @PatchMapping("/system")
    public ResponseEntity<SystemSettingsResponse> update(@Valid @RequestBody UpdateSystemSettingsRequest req) {
        return ResponseEntity.ok(settingsService.update(req, resolveUserId()));
    }

    private UUID resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getName());
    }
}
