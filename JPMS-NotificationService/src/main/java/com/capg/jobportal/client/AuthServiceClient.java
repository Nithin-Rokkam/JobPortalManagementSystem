package com.capg.jobportal.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.capg.jobportal.dto.UserInfoResponse;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: AuthServiceClient
 * DESCRIPTION:
 * This Feign client is used for internal communication with the
 * Auth Service microservice.
 *
 * It provides methods to:
 * 1. Fetch all job seeker email IDs.
 * 2. Retrieve detailed user information by user ID.
 *
 * NOTE:
 * This client is intended for internal microservice communication
 * and should not be exposed directly to external clients.
 * ================================================================
 */
@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthServiceClient {

    @GetMapping("/api/internal/users/job-seeker-emails")
    List<String> getJobSeekerEmails();
    
    @GetMapping("/api/internal/users/{id}/info")
    UserInfoResponse getUserInfo(@PathVariable("id") Long userId);
}