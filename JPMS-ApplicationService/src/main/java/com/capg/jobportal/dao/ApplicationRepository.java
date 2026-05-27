package com.capg.jobportal.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capg.jobportal.entity.Application;

import java.util.List;
import java.util.Optional;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: ApplicationRepository
 * DESCRIPTION:
 * This repository interface provides database access operations
 * for the Application entity using Spring Data JPA.
 *
 * It supports operations related to job applications submitted
 * by users (job seekers).
 *
 * KEY FEATURES:
 * - Fetch applications by user or job
 * - Prevent duplicate applications
 * - Secure access by validating ownership (userId)
 *
 * PURPOSE:
 * Acts as the DAO layer for managing job application data
 * and ensuring efficient interaction with the database.
 * ================================================================
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	/*
	 * METHOD: findByUserId
	 * DESCRIPTION:
	 * Retrieves all applications submitted by a specific user.
	 *
	 * Used for fetching "My Applications" for job seekers.
	 */
    List<Application> findByUserId(Long userId);

    
    /*
     * METHOD: findByJobId
     * DESCRIPTION:
     * Retrieves all applications for a specific job.
     *
     * Used by recruiters to view applicants for their job postings.
     */
    List<Application> findByJobId(Long jobId);

    
    /*
     * METHOD: findByJobId
     * DESCRIPTION:
     * Retrieves all applications for a specific job.
     *
     * Used by recruiters to view applicants for their job postings.
     */
    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    
    /*
     * METHOD: findByIdAndUserId
     * DESCRIPTION:
     * Retrieves a specific application only if it belongs to
     * the given user.
     *
     * Ensures secure access by validating ownership.
     */
    Optional<Application> findByIdAndUserId(Long id, Long userId);
}