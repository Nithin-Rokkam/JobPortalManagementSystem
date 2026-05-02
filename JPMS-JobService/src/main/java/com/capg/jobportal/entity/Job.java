package com.capg.jobportal.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.capg.jobportal.enums.JobStatus;
import com.capg.jobportal.enums.JobType;

<<<<<<< HEAD
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

=======
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: Job
 * DESCRIPTION:
<<<<<<< HEAD
 * This entity represents the "jobs" table in the database.
 * It stores all details related to job postings created by recruiters.
 *
 * KEY FEATURES:
 * - Maps job-related attributes such as title, company, salary, etc.
 * - Uses ENUM types for jobType and status for better consistency
 * - Maintains audit fields (createdAt, updatedAt)
 * - Tracks the recruiter (postedBy) who created the job
 *
 * LIFECYCLE METHODS:
 * - @PrePersist → Automatically sets createdAt and updatedAt
 *   when a new job record is inserted.
 * - @PreUpdate → Updates updatedAt timestamp whenever the job is modified.
 *
 * PURPOSE:
 * Acts as a persistence model for storing and managing job data
 * in the database using JPA/Hibernate.
 * ================================================================
 */
=======
 * JPA entity for the "jobs" table. Lombok @Getter/@Setter generate
 * all accessors. Lifecycle callbacks are kept manually.
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "company_name", length = 150, nullable = false)
    private String companyName;

    @Column(name = "location", length = 150, nullable = false)
    private String location;

    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 20, nullable = false)
    private JobType jobType;

    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private JobStatus status = JobStatus.ACTIVE;

    @Column(name = "deadline")
    private LocalDate deadline;

<<<<<<< HEAD
    // Stores the userId received from gateway header X-User-Id
=======
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    @Column(name = "posted_by")
    private Long postedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
<<<<<<< HEAD

    // Getters and Setters
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

    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }

    public String getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(String skillsRequired) { this.skillsRequired = skillsRequired; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public Long getPostedBy() { return postedBy; }
    public void setPostedBy(Long postedBy) { this.postedBy = postedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
=======
}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
