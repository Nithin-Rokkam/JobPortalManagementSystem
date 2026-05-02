package com.capg.jobportal.dto;

import java.time.LocalDateTime;

import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
<<<<<<< HEAD


=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ApplicationResponse
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents the response data for a job application.
 *
 * It includes:
 * - Application details (id, userId, jobId)
 * - Resume and cover letter information
 * - Current application status
 * - Timestamps (appliedAt, updatedAt)
 *
 * KEY FEATURE:
 * - Contains a static factory method (fromEntity) to convert
 *   Application entity into response DTO.
 *
 * PURPOSE:
 * Provides a structured response for job application-related APIs.
 * ================================================================
 */
public class ApplicationResponse {

	private Long id;
=======
 * DTO for job application responses. Lombok generates all boilerplate.
 * Static factory method fromEntity() maps the JPA entity to this DTO.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class ApplicationResponse {

    private Long id;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    private Long userId;
    private Long jobId;
    private String resumeUrl;
    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
<<<<<<< HEAD
 
    public static ApplicationResponse fromEntity(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.id = application.getId();
        response.userId = application.getUserId();
        response.jobId = application.getJobId();
        response.resumeUrl = application.getResumeUrl();
        response.coverLetter = application.getCoverLetter();
        response.status = application.getStatus();
        response.appliedAt = application.getAppliedAt();
        response.updatedAt = application.getUpdatedAt();
        return response;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getResumeUrl() {
		return resumeUrl;
	}

	public void setResumeUrl(String resumeUrl) {
		this.resumeUrl = resumeUrl;
	}

	public String getCoverLetter() {
		return coverLetter;
	}

	public void setCoverLetter(String coverLetter) {
		this.coverLetter = coverLetter;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	public LocalDateTime getAppliedAt() {
		return appliedAt;
	}

	public void setAppliedAt(LocalDateTime appliedAt) {
		this.appliedAt = appliedAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

=======

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
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
