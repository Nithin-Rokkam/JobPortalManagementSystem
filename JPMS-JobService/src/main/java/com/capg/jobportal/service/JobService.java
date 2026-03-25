package com.capg.jobportal.service;

import java.util.ArrayList;
import java.util.List;

/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: JobService
 * DESCRIPTION:
 * This service contains business logic for job management including
 * job creation, retrieval, search, update, deletion, and admin-level
 * operations. It also handles pagination and filtering.
 * ================================================================
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.capg.jobportal.Exceptions.*;
import com.capg.jobportal.dto.*;
import com.capg.jobportal.entity.Job;
import com.capg.jobportal.enums.JobStatus;
import com.capg.jobportal.enums.JobType;
import com.capg.jobportal.repository.JobRepository;

@Service
public class JobService {

    /*
     * Logger instance for tracking business logic execution
     */
    private static final Logger logger = LogManager.getLogger(JobService.class);

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    
    /* ================================================================
     * METHOD: postJob
     * DESCRIPTION:
     * Allows a recruiter to create and post a new job.
     * ================================================================ */
    public JobResponseDTO postJob(JobRequestDTO dto, Long postedBy, String userRole) {

        logger.info("Recruiter [{}] posting job: {}", postedBy, dto.getTitle());

        if (!userRole.equals("RECRUITER")) {
            logger.warn("Unauthorized role '{}' tried to post job", userRole);
            throw new ForbiddenException("Only recruiters can post jobs");
        }

        Job job = convertToEntity(dto);
        job.setPostedBy(postedBy);
        job.setStatus(JobStatus.ACTIVE);

        Job saved = jobRepository.save(job);

        logger.info("Job [{}] created successfully", saved.getId());

        return convertToResponseDTO(saved);
    }
    

    /* ================================================================
     * METHOD: getAllJobs
     * DESCRIPTION:
     * Retrieves all active jobs with pagination support.
     * ================================================================ */
    public PagedResponse<JobResponseDTO> getAllJobs(int page, int size) {

        logger.debug("Fetching jobs — page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findByStatusNot(JobStatus.DELETED, pageable);

        logger.info("Fetched {} jobs", jobPage.getNumberOfElements());

        return buildPagedResponse(jobPage);
    }

    
    /* ================================================================
     * METHOD: getJobById
     * DESCRIPTION:
     * Fetches job details using job ID.
     * ================================================================ */
    public JobResponseDTO getJobById(Long id) {

        logger.debug("Fetching job [{}]", id);

        Job job = jobRepository.findByIdAndStatusNot(id, JobStatus.DELETED)
                .orElseThrow(() -> {
                    logger.warn("Job [{}] not found", id);
                    return new ResourceNotFoundException("Job not found with id: " + id);
                });

        return convertToResponseDTO(job);
    }

    
    /* ================================================================
     * METHOD: searchJobs
     * DESCRIPTION:
     * Searches jobs based on filters such as title, location,
     * job type, and experience with pagination.
     * ================================================================ */
    public PagedResponse<JobResponseDTO> searchJobs(String title, String location,
                                                    String jobType, Integer experienceYears,
                                                    int page, int size) {

        logger.info("Searching jobs — title: {}, location: {}", title, location);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        JobType jobTypeEnum = null;

        if (jobType != null && !jobType.isEmpty()) {
            try {
                jobTypeEnum = JobType.valueOf(jobType.toUpperCase());
            } catch (Exception e) {
                logger.warn("Invalid job type: {}", jobType);
                throw new InvalidJobTypeException("Invalid job type: " + jobType);
            }
        }

        Page<Job> jobPage = jobRepository.searchJobs(
                title, location, jobTypeEnum, experienceYears, pageable);

        logger.info("Search returned {} results", jobPage.getTotalElements());

        return buildPagedResponse(jobPage);
    }

    
    /* ================================================================
     * METHOD: updateJob
     * DESCRIPTION:
     * Updates job details if the requester is the owner recruiter.
     * ================================================================ */
    public JobResponseDTO updateJob(Long id, JobRequestDTO dto,
                                   Long currentUserId, String currentUserRole) {

        logger.info("Updating job [{}] by user [{}]", id, currentUserId);

        if (!currentUserRole.equals("RECRUITER")) {
            throw new ForbiddenException("Only recruiters can update jobs");
        }

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getPostedBy().equals(currentUserId)) {
            throw new ForbiddenException("Not your job");
        }

        job.setTitle(dto.getTitle());
        job.setCompanyName(dto.getCompanyName());
        job.setLocation(dto.getLocation());
        job.setSalary(dto.getSalary());
        job.setExperienceYears(dto.getExperienceYears());
        job.setJobType(JobType.valueOf(dto.getJobType().toUpperCase()));
        job.setSkillsRequired(dto.getSkillsRequired());
        job.setDescription(dto.getDescription());
        job.setDeadline(dto.getDeadline());

        Job updated = jobRepository.save(job);

        logger.info("Job [{}] updated successfully", id);

        return convertToResponseDTO(updated);
    }
    

    /* ================================================================
     * METHOD: deleteJob
     * DESCRIPTION:
     * Performs soft delete of job if requester is owner recruiter.
     * ================================================================ */
    public void deleteJob(Long id, Long userId, String role) {

        logger.info("Deleting job [{}] by user [{}]", id, userId);

        if (!role.equals("RECRUITER")) {
            throw new ForbiddenException("Only recruiters can delete jobs");
        }

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getPostedBy().equals(userId)) {
            throw new ForbiddenException("Not your job");
        }

        job.setStatus(JobStatus.DELETED);
        jobRepository.save(job);

        logger.info("Job [{}] soft deleted", id);
    }

    
    /* ================================================================
     * METHOD: getMyJobs
     * DESCRIPTION:
     * Retrieves jobs posted by a recruiter with pagination.
     * ================================================================ */
    public PagedResponse<JobResponseDTO> getMyJobs(Long userId, String role, int page, int size) {

        logger.info("Fetching jobs for recruiter [{}]", userId);

        if (!role.equals("RECRUITER")) {
            throw new ForbiddenException("Access denied");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobPage =
                jobRepository.findByPostedByAndStatusNot(userId, JobStatus.DELETED, pageable);

        return buildPagedResponse(jobPage);
    }

    
    /* ================================================================
     * METHOD: getAllJobsForAdmin
     * DESCRIPTION:
     * Retrieves all jobs including deleted ones for admin usage.
     * ================================================================ */
    public List<JobResponseDTO> getAllJobsForAdmin() {

        logger.info("Admin fetching all jobs");

        List<Job> jobs = jobRepository.findAll();
        List<JobResponseDTO> result = new ArrayList<>();

        for (Job job : jobs) {
            result.add(convertToResponseDTO(job));
        }

        return result;
    }

    
    /* ================================================================
     * METHOD: deleteJobByAdmin
     * DESCRIPTION:
     * Allows admin to soft delete any job without ownership check.
     * ================================================================ */
    public void deleteJobByAdmin(Long id) {

        logger.info("Admin deleting job [{}]", id);

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        job.setStatus(JobStatus.DELETED);
        jobRepository.save(job);
    }
    

    /* ================================================================
     * PRIVATE HELPER METHODS
     * ================================================================ */

    private PagedResponse<JobResponseDTO> buildPagedResponse(Page<Job> jobPage) {
        return new PagedResponse<>(
                jobPage.getContent().stream().map(this::convertToResponseDTO).toList(),
                jobPage.getNumber(),
                jobPage.getTotalPages(),
                jobPage.getTotalElements(),
                jobPage.isLast()
        );
    }

    private Job convertToEntity(JobRequestDTO dto) {
        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setCompanyName(dto.getCompanyName());
        job.setLocation(dto.getLocation());
        job.setSalary(dto.getSalary());
        job.setExperienceYears(dto.getExperienceYears());
        job.setJobType(JobType.valueOf(dto.getJobType().toUpperCase()));
        job.setSkillsRequired(dto.getSkillsRequired());
        job.setDescription(dto.getDescription());
        job.setDeadline(dto.getDeadline());
        return job;
    }

    private JobResponseDTO convertToResponseDTO(Job job) {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setCompanyName(job.getCompanyName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setExperienceYears(job.getExperienceYears());
        dto.setJobType(job.getJobType().name());
        dto.setSkillsRequired(job.getSkillsRequired());
        dto.setDescription(job.getDescription());
        dto.setStatus(job.getStatus().name());
        dto.setDeadline(job.getDeadline());
        dto.setPostedBy(job.getPostedBy());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        return dto;
    }
}