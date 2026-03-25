package com.capg.jobportal.dto;

import com.capg.jobportal.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

	@NotNull(message = "New status is required")
    private ApplicationStatus newStatus;
 
    private String recruiterNote;

	public ApplicationStatus getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(ApplicationStatus newStatus) {
		this.newStatus = newStatus;
	}

	public String getRecruiterNote() {
		return recruiterNote;
	}

	public void setRecruiterNote(String recruiterNote) {
		this.recruiterNote = recruiterNote;
	}	
    
    
    

}
