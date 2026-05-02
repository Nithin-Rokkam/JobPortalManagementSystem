package com.capg.jobportal.dto;

import com.capg.jobportal.enums.ApplicationStatus;
<<<<<<< HEAD

import jakarta.validation.constraints.NotNull;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: StatusUpdateRequest
 * DESCRIPTION:
 * This DTO is used to update the status of a job application
 * by recruiters.
 *
 * It includes:
 * - New application status (mandatory)
 * - Optional recruiter note or feedback
 *
 * PURPOSE:
 * Acts as a request payload for updating application status
 * during the recruitment process.
 * ================================================================
 */
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
    
    
    

=======
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private ApplicationStatus newStatus;

    private String recruiterNote;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
