package com.capg.jobportal.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobClientResponse
 * DESCRIPTION:
 * DTO for job details received from Job Service via Feign.
 * Lombok generates all boilerplate.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class JobClientResponse {

    private Long id;
    private String title;
    private String status;
    private Long postedBy;
    private LocalDate deadline;
    private String companyName;
}
