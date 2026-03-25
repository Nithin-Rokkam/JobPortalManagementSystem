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

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // GET all jobs — paginated, excludes DELETED
    Page<Job> findByStatusNot(JobStatus status, Pageable pageable);

    // GET single job — only if not deleted
    Optional<Job> findByIdAndStatusNot(Long id, JobStatus status);

    // GET recruiter's own jobs — paginated, excludes DELETED
    Page<Job> findByPostedByAndStatusNot(Long postedBy, JobStatus status, Pageable pageable);

    // SEARCH jobs — paginated, only ACTIVE
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