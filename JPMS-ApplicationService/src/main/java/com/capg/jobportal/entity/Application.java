package com.capg.jobportal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.capg.jobportal.enums.ApplicationStatus;
<<<<<<< HEAD


=======
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: Application
 * DESCRIPTION:
<<<<<<< HEAD
 * This entity represents the "applications" table in the database.
 * It stores details of job applications submitted by users
 * (job seekers) for specific jobs.
 *
 * KEY FEATURES:
 * - Maintains relationship between user (userId) and job (jobId)
 * - Stores resume URL and optional cover letter
 * - Tracks application status using ApplicationStatus enum
 * - Includes recruiter feedback via recruiterNote
 * - Enforces unique constraint (user_id + job_id) to prevent
 *   duplicate applications
 *
 * LIFECYCLE METHODS:
 * - @PrePersist → Automatically sets appliedAt and updatedAt
 *   when a new application is created
 * - @PreUpdate → Updates updatedAt when the application is modified
 *
 * PURPOSE:
 * Acts as the persistence model for managing job applications,
 * ensuring data integrity, tracking application progress, and
 * supporting recruiter review workflows.
 * ================================================================
 */
=======
 * JPA entity for the "applications" table. Lombok @Getter/@Setter
 * generate all accessors. Lifecycle callbacks are kept manually.
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
@Entity
@Table(
    name = "applications",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "job_id"},
        name = "uk_user_job"
    )
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "resume_url", nullable = false, columnDefinition = "TEXT")
    private String resumeUrl;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
<<<<<<< HEAD
    @Column(nullable = false)
=======
    @Column(name = "status", nullable = false, length = 50)
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "recruiter_note", columnDefinition = "TEXT")
    private String recruiterNote;

    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
<<<<<<< HEAD

    public Application() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getRecruiterNote() { return recruiterNote; }
    public void setRecruiterNote(String recruiterNote) { this.recruiterNote = recruiterNote; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
=======
}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
