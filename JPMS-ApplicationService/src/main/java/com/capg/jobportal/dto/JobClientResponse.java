package com.capg.jobportal.dto;

import java.time.LocalDate;
<<<<<<< HEAD

=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobClientResponse
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents job details received from the Job Service
 * via Feign client communication.
 *
 * It includes:
 * - Basic job information (id, title, status)
 * - Recruiter reference (postedBy)
 * - Application deadline
 *
 * PURPOSE:
 * Acts as a data transfer object for inter-service communication
 * between Application Service and Job Service.
 * ================================================================
 */
public class JobClientResponse {

	private Long id;
    private String title;
    private String status;
    private Long postedBy;
    private LocalDate deadline;   // changed from String to LocalDate

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getPostedBy() { return postedBy; }
    public void setPostedBy(Long postedBy) { this.postedBy = postedBy; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    
    

=======
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
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
