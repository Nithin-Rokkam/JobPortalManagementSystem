package com.capg.jobportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobResponse
 * DESCRIPTION:
 * DTO for job data returned by AdminService.
 * Lombok generates all boilerplate.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class JobResponse {

    private Long id;
    private String title;
    private String companyName;
    private String location;
    private BigDecimal salary;
    private Integer experienceYears;
    private String jobType;
    private String skillsRequired;
    private String description;
    private String status;
    private LocalDate deadline;
    private Long postedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
