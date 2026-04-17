package com.example.auth.controller;

import com.example.auth.dto.UpdateUserPreferencesRequest;
import com.example.auth.dto.UserPreferencesResponse;
import com.example.auth.service.PreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/preferences")
@RequiredArgsConstructor
public class PreferencesController {

    private final PreferencesService preferencesService;

    @GetMapping
    public ResponseEntity<UserPreferencesResponse> get() {
        return ResponseEntity.ok(preferencesService.get(resolveUserId()));
    }

    @PatchMapping
    public ResponseEntity<UserPreferencesResponse> update(@RequestBody UpdateUserPreferencesRequest req) {
        return ResponseEntity.ok(preferencesService.update(resolveUserId(), req));
    }

    private UUID resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getName());
    }
}
