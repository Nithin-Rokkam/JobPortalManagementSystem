package com.capg.jobportal.dto;

import java.time.LocalDateTime;

import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;

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