package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.capg.jobportal.dto.UserInfoResponse;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/api/internal/users/{id}/info")
    UserInfoResponse getUserInfo(@PathVariable("id") Long userId);
}