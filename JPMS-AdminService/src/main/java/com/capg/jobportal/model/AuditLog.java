package com.capg.jobportal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: AuditLog
 * DESCRIPTION:
 * JPA entity for the "audit_logs" table. Lombok @Getter/@Setter
 * generate all accessors. Custom constructor and @PrePersist
 * lifecycle callback are kept manually.
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
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

    public AuditLog(String action, String performedBy, String details) {
        this.action      = action;
        this.performedBy = performedBy;
        this.details     = details;
    }
}
