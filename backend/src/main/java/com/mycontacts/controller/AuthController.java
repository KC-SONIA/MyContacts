package com.mycontacts.controller;

import com.mycontacts.dto.AuthResponse;
import com.mycontacts.dto.LoginRequest;
import com.mycontacts.dto.SignupRequest;
import com.mycontacts.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication endpoints – signup, login, logout, and session check.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpSession session) {
        AuthResponse response = authService.login(request, session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /** Check if the user has an active session. Used by the frontend to restore auth state. */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> currentUser(HttpSession session) {
        var user = authService.getCurrentUser(session);
        return ResponseEntity.ok(new AuthResponse("Authenticated", user.getUsername(), user.getEmail()));
    }
}
