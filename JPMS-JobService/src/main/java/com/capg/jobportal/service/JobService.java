package com.capg.jobportal.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.capg.jobportal.Exceptions.ForbiddenException;
import com.capg.jobportal.Exceptions.InvalidJobTypeException;
import com.capg.jobportal.Exceptions.ResourceNotFoundException;
import com.capg.jobportal.dto.JobRequestDTO;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.dto.PagedResponse;
import com.capg.jobportal.entity.Job;
import com.capg.jobportal.enums.JobStatus;
import com.capg.jobportal.enums.JobType;
import com.capg.jobportal.repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    // -----------------------------------------------
    // POST /api/jobs — RECRUITER only
    // -----------------------------------------------
    public JobResponseDTO postJob(JobRequestDTO dto, Long postedBy, String userRole) {

        // Only RECRUITER can post jobs
        if (!userRole.equals("RECRUITER")) {
        	throw new ForbiddenException("Only recruiters can post jobs");
        }

        Job job = convertToEntity(dto);
        job.setPostedBy(postedBy);
        job.setStatus(JobStatus.ACTIVE);

        Job saved = jobRepository.save(job);
        return convertToResponseDTO(saved);
    }

    // -----------------------------------------------
    // GET /api/jobs — Public — PAGINATED
    // page=0 means first page, size=10 means 10 jobs per page
    // -----------------------------------------------
    public PagedResponse<JobResponseDTO> getAllJobs(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobPage = jobRepository.findByStatusNot(JobStatus.DELETED, pageable);

        return buildPagedResponse(jobPage);
    }

    // -----------------------------------------------
    // GET /api/jobs/{id} — Public
    // -----------------------------------------------
    public JobResponseDTO getJobById(Long id) {

        Job job = jobRepository.findByIdAndStatusNot(id, JobStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + id));

        return convertToResponseDTO(job);
    }

    // -----------------------------------------------
    // GET /api/jobs/search — Public — PAGINATED
    // -----------------------------------------------
    public PagedResponse<JobResponseDTO> searchJobs(String title, String location,
                                                     String jobType, Integer experienceYears,
                                                     int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Convert String → JobType enum safely
        JobType jobTypeEnum = null;
        if (jobType != null && !jobType.isEmpty()) {
            try {
                jobTypeEnum = JobType.valueOf(jobType.toUpperCase());
            } catch (IllegalArgumentException e) {
            	 throw new InvalidJobTypeException(
                         "Invalid job type: " + jobType +
                         ". Valid values are: FULL_TIME, PART_TIME, REMOTE, CONTRACT");
            }
        }
        
        
        Page<Job> jobPage = jobRepository.searchJobs(
                title, location, jobTypeEnum, experienceYears, pageable);

        return buildPagedResponse(jobPage);
    }

    // -----------------------------------------------
    // PUT /api/jobs/{id} — RECRUITER + owner only
    // -----------------------------------------------
    public JobResponseDTO updateJob(Long id, JobRequestDTO dto,
                                    Long currentUserId, String currentUserRole) {

        // Only RECRUITER role allowed
        if (!currentUserRole.equals("RECRUITER")) {
        	throw new ForbiddenException("Only recruiters can update jobs");
        }

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + id));

        // Ownership check — recruiter must own this job
        if (!job.getPostedBy().equals(currentUserId)) {
        	throw new ForbiddenException("You can only update your own jobs");
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
        return convertToResponseDTO(updated);
    }

    // -----------------------------------------------
    // DELETE /api/jobs/{id} — RECRUITER + owner only
    // Soft delete — status = DELETED, record stays in DB
    // -----------------------------------------------
    public void deleteJob(Long id, Long currentUserId, String currentUserRole) {

        // Only RECRUITER role allowed
        if (!currentUserRole.equals("RECRUITER")) {
        	throw new ForbiddenException("Only recruiters can delete jobs");
        }

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + id));

        // Ownership check — recruiter must own this job
        if (!job.getPostedBy().equals(currentUserId)) {
        	throw new ForbiddenException("You can only delete your own jobs");
        }

        // Soft delete — never physically remove from DB
        job.setStatus(JobStatus.DELETED);
        jobRepository.save(job);
    }

    // -----------------------------------------------
    // GET /api/jobs/my-jobs — RECRUITER only — PAGINATED
    // -----------------------------------------------
    public PagedResponse<JobResponseDTO> getMyJobs(Long postedBy,
                                                    String userRole,
                                                    int page, int size) {

        // Only RECRUITER can see their own jobs
        if (!userRole.equals("RECRUITER")) {
        	throw new ForbiddenException("Only recruiters can access this endpoint");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobPage = jobRepository.findByPostedByAndStatusNot(
                postedBy, JobStatus.DELETED, pageable);

        return buildPagedResponse(jobPage);
    }
    
    
    public List<JobResponseDTO> getAllJobsForAdmin() {
        List<Job> jobs = jobRepository.findAll();
        List<JobResponseDTO> result = new ArrayList<>();
        for (Job job : jobs) {
            result.add(convertToResponseDTO(job));  // reuse existing private helper
        }
        return result;
    }
    

    public void deleteJobByAdmin(Long id) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            throw new ResourceNotFoundException("Job not found with id: " + id);
        }
        job.setStatus(JobStatus.DELETED);
        jobRepository.save(job);
    }
    

    // -----------------------------------------------
    // PRIVATE HELPER — builds PagedResponse from Page<Job>
    // -----------------------------------------------
    private PagedResponse<JobResponseDTO> buildPagedResponse(Page<Job> jobPage) {
        return new PagedResponse<>(
                jobPage.getContent()
                       .stream()
                       .map(this::convertToResponseDTO)
                       .toList(),
                jobPage.getNumber(),       // current page number
                jobPage.getTotalPages(),   // total pages available
                jobPage.getTotalElements(), // total records in DB
                jobPage.isLast()           // is this the last page?
        );
    }

    // -----------------------------------------------
    // PRIVATE HELPER — RequestDTO → Entity
    // -----------------------------------------------
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

    // -----------------------------------------------
    // PRIVATE HELPER — Entity → ResponseDTO
    // -----------------------------------------------
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