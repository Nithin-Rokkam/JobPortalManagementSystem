package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.capg.jobportal.dto.ApplicationStats;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: AdminAppClient
 * DESCRIPTION:
 * This Feign client is used for inter-service communication with
 * the Application Service.
 *
 * It allows the Admin Service to fetch aggregated application
 * statistics such as total applications and status-wise counts.
 *
 * PURPOSE:
 * Enables Admin Service to retrieve analytics data for reporting
 * and dashboard purposes without directly accessing the database.
 * ================================================================
 */
@FeignClient(name = "application-service")
public interface AdminAppClient {

    @GetMapping("/api/internal/applications/stats")
    ApplicationStats getStats();
}
