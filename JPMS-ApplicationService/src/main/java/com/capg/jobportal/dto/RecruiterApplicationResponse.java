package com.capg.jobportal.dto;

import java.time.LocalDateTime;

import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
<<<<<<< HEAD


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: RecruiterApplicationResponse
 * DESCRIPTION:
 * This DTO represents application details specifically for
 * recruiters viewing applicants.
 *
 * It includes:
 * - Application details
 * - Resume and cover letter
 * - Application status
 * - Recruiter-specific note/feedback
 * - Timestamps
 *
 * KEY FEATURE:
 * - Includes a static factory method to map Application entity
 *   into recruiter-specific response format.
 *
 * PURPOSE:
 * Provides enhanced application data tailored for recruiters.
 * ================================================================
 */
=======
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * DTO for recruiter-facing application view. Static factory method
 * fromEntity() maps the JPA entity; seekerName/seekerEmail are
 * populated separately via AuthServiceClient.
 */
@Data
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD

    public static RecruiterApplicationResponse fromEntity(Application application) {
        RecruiterApplicationResponse response = new RecruiterApplicationResponse();
        response.id = application.getId();
        response.userId = application.getUserId();
        response.jobId = application.getJobId();
        response.resumeUrl = application.getResumeUrl();
        response.coverLetter = application.getCoverLetter();
        response.status = application.getStatus();
        response.recruiterNote = application.getRecruiterNote();
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

	public String getRecruiterNote() {
		return recruiterNote;
	}

	public void setRecruiterNote(String recruiterNote) {
		this.recruiterNote = recruiterNote;
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

    
    
}
=======
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
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
