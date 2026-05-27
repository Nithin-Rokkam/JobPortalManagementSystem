package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.capg.jobportal.dto.UserInfoResponse;
import java.util.Map;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: AuthServiceClient
 * DESCRIPTION:
 * This Feign client is used for inter-service communication with
 * the Job Service in a microservices architecture.
 *
 * It allows the Application Service (or other services) to fetch
 * job-related data by making REST API calls to the auth Service.
 *
 * KEY FEATURES:
 * - Uses OpenFeign for declarative REST client implementation
 * - Communicates with "auth-service" registered in Eureka
 * - Passes user context via headers (X-User-Id, X-User-Role)
 *
 * PURPOSE:
 * Enables seamless and type-safe communication between
 * microservices without manually writing HTTP client code.
 * ================================================================
 */

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/api/internal/users/{id}/info")
    UserInfoResponse getUserInfo(@PathVariable("id") Long userId);

    @PutMapping("/api/internal/users/{seekerId}/selected-company")
    void updateSelectedByCompany(@PathVariable("seekerId") Long seekerId,
                                  @RequestBody Map<String, String> body);
}