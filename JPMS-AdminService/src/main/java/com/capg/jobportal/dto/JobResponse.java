package com.capg.jobportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
<<<<<<< HEAD
=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
<<<<<<< HEAD
 * CLASS: ErrorResponse
 * DESCRIPTION:
 * This DTO defines a standard structure for error responses
 * returned by the application.
 *
 * It includes:
 * - HTTP status code
 * - Error type
 * - Detailed error message
 * - Timestamp of occurrence
 *
 * PURPOSE:
 * Ensures consistent error handling and improves debugging
 * and client-side error interpretation.
 * ================================================================
 */
public class JobResponse {

	private Long id;
=======
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
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(String skillsRequired) { this.skillsRequired = skillsRequired; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public Long getPostedBy() { return postedBy; }
    public void setPostedBy(Long postedBy) { this.postedBy = postedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
=======
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
