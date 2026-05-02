package com.capg.jobportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
<<<<<<< HEAD


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobRequestDTO
 * DESCRIPTION:
 * This DTO is used to capture job-related input data from clients
 * (typically recruiters) when creating or updating a job posting.
 *
 * It contains all necessary job details such as:
 * - Job title, company name, and location
 * - Salary and experience requirements
 * - Job type and required skills
 * - Job description and status
 * - Application deadline
 *
 * PURPOSE:
 * Acts as a request payload to transfer job data from the client
 * layer to the service layer in a structured and validated format.
 * ================================================================
 */
=======
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
public class JobRequestDTO {

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
<<<<<<< HEAD

    // Getters and Setters
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
}
=======
}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
