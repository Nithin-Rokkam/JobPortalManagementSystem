package com.capg.jobportal.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.capg.jobportal.entity.Job;
import com.capg.jobportal.enums.JobStatus;
import com.capg.jobportal.enums.JobType;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: JobRepository
 * DESCRIPTION:
 * This repository interface provides database access operations
 * for the Job entity using Spring Data JPA.
 *
 * It extends JpaRepository to inherit basic CRUD operations and
 * defines custom query methods for job retrieval, filtering, and search.
 *
 * KEY FEATURES:
 * - Pagination support for large datasets
 * - Soft-delete handling (excluding DELETED jobs)
 * - Recruiter-specific job retrieval
 * - Dynamic search functionality using JPQL
 *
 * PURPOSE:
 * Acts as the DAO (Data Access Object) layer for interacting with
 * the "jobs" table in a structured and efficient manner.
 * ================================================================
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    
	/*
     * ================================================================
     * METHOD: findByStatusNot
     * DESCRIPTION:
     * Fetches all jobs except those having the specified status.
     *
     * Typically used to exclude DELETED jobs from results.
     *
     * PARAMETERS:
     * - status   → status to exclude (e.g., DELETED)
     * - pageable → pagination information
     *
     * RETURNS:
     * - Paginated list of jobs
     * ================================================================
     */
    Page<Job> findByStatusNot(JobStatus status, Pageable pageable);

    
    /*
     * ================================================================
     * METHOD: findByIdAndStatusNot
     * DESCRIPTION:
     * Retrieves a job by its ID only if it is not marked as DELETED.
     *
     * Ensures soft-deleted jobs are not accessible.
     *
     * PARAMETERS:
     * - id     → job ID
     * - status → status to exclude (e.g., DELETED)
     *
     * RETURNS:
     * - Optional<Job> (empty if not found or deleted)
     * ================================================================
     */
    Optional<Job> findByIdAndStatusNot(Long id, JobStatus status);

    
    /*
     * ================================================================
     * METHOD: findByPostedByAndStatusNot
     * DESCRIPTION:
     * Retrieves jobs posted by a specific recruiter while excluding
     * jobs with a particular status (e.g., DELETED).
     *
     * Used to fetch recruiter-specific job listings.
     *
     * PARAMETERS:
     * - postedBy → recruiter/user ID
     * - status   → status to exclude
     * - pageable → pagination information
     *
     * RETURNS:
     * - Paginated list of jobs
     * ================================================================
     */
    Page<Job> findByPostedByAndStatusNot(Long postedBy, JobStatus status, Pageable pageable);

    
    /*
     * ================================================================
     * METHOD: searchJobs
     * DESCRIPTION:
     * Performs dynamic search on jobs based on optional filters.
     *
     * Only ACTIVE jobs are considered.
     *
     * FILTERS (optional):
     * - title           → partial match (case-insensitive)
     * - location        → partial match (case-insensitive)
     * - jobType         → exact match
     * - experienceYears → jobs requiring less than or equal experience
     *
     * PARAMETERS:
     * - title           → job title keyword
     * - location        → job location keyword
     * - jobType         → type of job (FULL_TIME, etc.)
     * - experienceYears → max experience filter
     * - pageable        → pagination info
     *
     * RETURNS:
     * - Paginated list of filtered jobs
     *
     * NOTE:
     * Uses JPQL query with dynamic conditions to handle null filters.
     * ================================================================
     */
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' " +
           "AND (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:experienceYears IS NULL OR j.experienceYears <= :experienceYears)")
    Page<Job> searchJobs(
            @Param("title") String title,
            @Param("location") String location,
            @Param("jobType") JobType jobType,
            @Param("experienceYears") Integer experienceYears,
            Pageable pageable
    );
}