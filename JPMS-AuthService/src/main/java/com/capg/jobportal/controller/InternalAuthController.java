package com.capg.jobportal.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.service.AuthService;

@RestController
@RequestMapping("/api/internal")
public class InternalAuthController {

    private final AuthService authService;

    public InternalAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        List<UserProfileResponse> users = authService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long id) {
        authService.updateUserStatus(id, "BANNED");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id) {
        authService.updateUserStatus(id, "ACTIVE");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/users/{id}/invalidate-token")
    public ResponseEntity<Void> invalidateToken(@PathVariable Long id) {
        authService.invalidateTokenByUserId(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}