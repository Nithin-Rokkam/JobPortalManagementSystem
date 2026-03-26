package com.capg.jobportal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: AuditLog
 * DESCRIPTION:
 * This entity represents the "audit_logs" table in the database.
 * It is used to track administrative and system-level actions
 * performed within the platform.
 *
 * KEY FEATURES:
 * - Stores action type (e.g., DELETE_USER, BAN_USER, DELETE_JOB)
 * - Tracks who performed the action (performedBy)
 * - Captures additional details related to the action
 * - Automatically records timestamp (createdAt)
 *
 * LIFECYCLE METHODS:
 * - @PrePersist → Automatically sets createdAt when a new log is created
 *
 * PURPOSE:
 * Provides audit tracking for critical operations, enabling
 * monitoring, debugging, and maintaining system accountability.
 * ================================================================
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "performed_by", nullable = false, length = 150)
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog() {}

    public AuditLog(String action, String performedBy, String details) {
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
