package com.mycontacts.controller;

import com.mycontacts.dto.ContactRequest;
import com.mycontacts.dto.ContactResponse;
import com.mycontacts.entity.User;
import com.mycontacts.service.AuthService;
import com.mycontacts.service.ContactService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contact CRUD + search + bin endpoints.
 * All endpoints require an active session (handled by Spring Security).
 */
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final AuthService authService;

    // ─────────────────────── CRUD ───────────────────────

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody ContactRequest request,
                                                          HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ResponseEntity.ok(contactService.createContact(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(@PathVariable Long id,
                                                          @Valid @RequestBody ContactRequest request,
                                                          HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ResponseEntity.ok(contactService.updateContact(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> softDelete(@PathVariable Long id,
                                                           HttpSession session) {
        User user = authService.getCurrentUser(session);
        contactService.softDeleteContact(id, user);
        return ResponseEntity.ok(Map.of("message", "Contact moved to bin"));
    }

    // ─────────────────────── LIST + SEARCH ───────────────────────

    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ResponseEntity.ok(contactService.getContacts(user, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContactResponse>> searchContacts(@RequestParam String query,
                                                                  HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ResponseEntity.ok(contactService.searchContacts(user, query));
    }

    // ─────────────────────── BIN ───────────────────────

    @GetMapping("/bin")
    public ResponseEntity<List<ContactResponse>> getBinContacts(HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ResponseEntity.ok(contactService.getBinContacts(user));
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<ContactResponse> restoreContact(@PathVariable Long id,
                                                            HttpSession session) {
        User user = authService.getCurrentUser(session);
        return ResponseEntity.ok(contactService.restoreContact(id, user));
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<Map<String, String>> permanentDelete(@PathVariable Long id,
                                                                 HttpSession session) {
        User user = authService.getCurrentUser(session);
        contactService.permanentlyDeleteContact(id, user);
        return ResponseEntity.ok(Map.of("message", "Contact permanently deleted"));
    }
}
