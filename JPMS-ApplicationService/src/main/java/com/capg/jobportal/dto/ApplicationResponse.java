package com.capg.jobportal.dto;

import java.time.LocalDateTime;

import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ApplicationResponse
 * DESCRIPTION:
 * DTO for job application responses. Lombok generates all boilerplate.
 * Static factory method fromEntity() maps the JPA entity to this DTO.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class ApplicationResponse {

    private Long id;
    private Long userId;
    private Long jobId;
    private String resumeUrl;
    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    public static ApplicationResponse fromEntity(Application application) {
        ApplicationResponse r = new ApplicationResponse();
        r.id           = application.getId();
        r.userId       = application.getUserId();
        r.jobId        = application.getJobId();
        r.resumeUrl    = application.getResumeUrl();
        r.coverLetter  = application.getCoverLetter();
        r.status       = application.getStatus();
        r.appliedAt    = application.getAppliedAt();
        r.updatedAt    = application.getUpdatedAt();
        return r;
    }
}
