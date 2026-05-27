package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.capg.jobportal.dto.UserResponse;

import java.util.List;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: AuthServiceClient
 * DESCRIPTION:
 * This Feign client enables communication between the Admin
 * Service and the Auth Service.
 *
 * It supports user management operations such as:
 * - Fetching all users
 * - Deleting users
 * - Banning and unbanning users
 * - Invalidating user tokens
 *
 * PURPOSE:
 * Allows centralized admin control over user accounts while
 * maintaining separation of concerns across microservices.
 * ================================================================
 */
@FeignClient(name = "auth-service")
public interface AuthServiceClient {

	@GetMapping("/api/internal/users")
    List<UserResponse> getAllUsers();
 
    @DeleteMapping("/api/internal/users/{id}")
    void deleteUser(@PathVariable Long id);
 
    @PutMapping("/api/internal/users/{id}/ban")
    void banUser(@PathVariable Long id);
 
    @PutMapping("/api/internal/users/{id}/unban")
    void unbanUser(@PathVariable Long id);
 
    @PutMapping("/api/internal/users/{id}/invalidate-token")
    void invalidateToken(@PathVariable Long id);
}
