package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserListResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "7") int pageSize) {
        return ResponseEntity.ok(userService.listUsers(status, search, page, pageSize));
    }

    @PostMapping
    public ResponseEntity<ManagedUserResponse> invite(@Valid @RequestBody InviteUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.invite(req, resolveUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManagedUserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ManagedUserResponse> update(@PathVariable String id,
                                                       @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req, resolveUserId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        userService.deleteUser(id, resolveUserId());
    }

    private UUID resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getName());
    }
}
