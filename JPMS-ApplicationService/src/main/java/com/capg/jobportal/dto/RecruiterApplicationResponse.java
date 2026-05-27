package com.capg.jobportal.dto;

import java.time.LocalDateTime;

import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * DTO for recruiter-facing application view. Static factory method
 * fromEntity() maps the JPA entity; seekerName/seekerEmail are
 * populated separately via AuthServiceClient.
 */
@Data
@NoArgsConstructor
public class RecruiterApplicationResponse {

    private Long id;
    private Long userId;
    private Long jobId;
    private String resumeUrl;
    private String coverLetter;
    private ApplicationStatus status;
    private String recruiterNote;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private String seekerName;
    private String seekerEmail;

    public static RecruiterApplicationResponse fromEntity(Application application) {
        RecruiterApplicationResponse r = new RecruiterApplicationResponse();
        r.id            = application.getId();
        r.userId        = application.getUserId();
        r.jobId         = application.getJobId();
        r.resumeUrl     = application.getResumeUrl();
        r.coverLetter   = application.getCoverLetter();
        r.status        = application.getStatus();
        r.recruiterNote = application.getRecruiterNote();
        r.appliedAt     = application.getAppliedAt();
        r.updatedAt     = application.getUpdatedAt();
        return r;
    }
}
