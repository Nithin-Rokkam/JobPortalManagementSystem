package com.capg.jobportal.controller;



/*
 * Log4j2 imports
 * LogManager → creates logger
 * Logger     → used for logging messages
 */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.service.ApplicationService;


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: InternalApplicationController
 * DESCRIPTION:
 * This controller provides internal APIs for application-related
 * operations such as fetching aggregated application statistics.
 *
 * NOTE:
 * This controller is used only for inter-service communication
 * (e.g., AdminService) and is NOT exposed to external clients
 * via API Gateway.
 * ================================================================
 */
@RestController
@RequestMapping("/api/internal")
public class InternalApplicationController {

    /*
     * Logger instance for tracking internal API calls
     */
    private static final Logger logger =
            LogManager.getLogger(InternalApplicationController.class);

    private final ApplicationService applicationService;

    public InternalApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    

    /* ================================================================
     * METHOD: getStats
     * DESCRIPTION:
     * Retrieves aggregated application statistics including total
     * applications and status-wise counts. Used internally by admin
     * services for generating reports.
     * ================================================================ */
    @GetMapping("/applications/stats")
    public ResponseEntity<ApplicationStats> getStats() {

        logger.info("Internal API call → Fetching application statistics");

        ApplicationStats stats = applicationService.getApplicationStats();

        logger.info("Application statistics fetched successfully");

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}