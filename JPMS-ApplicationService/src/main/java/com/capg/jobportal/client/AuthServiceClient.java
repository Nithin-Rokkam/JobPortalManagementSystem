package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
<<<<<<< HEAD

import com.capg.jobportal.dto.UserInfoResponse;
=======
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.capg.jobportal.dto.UserInfoResponse;
import java.util.Map;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/api/internal/users/{id}/info")
    UserInfoResponse getUserInfo(@PathVariable("id") Long userId);
<<<<<<< HEAD
=======

    @PutMapping("/api/internal/users/{seekerId}/selected-company")
    void updateSelectedByCompany(@PathVariable("seekerId") Long seekerId,
                                  @RequestBody Map<String, String> body);
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}