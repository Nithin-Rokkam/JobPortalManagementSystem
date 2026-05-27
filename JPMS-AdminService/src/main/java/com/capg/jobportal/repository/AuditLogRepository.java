package com.capg.jobportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capg.jobportal.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
