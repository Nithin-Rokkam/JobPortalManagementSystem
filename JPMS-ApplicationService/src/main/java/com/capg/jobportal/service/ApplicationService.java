package com.capg.jobportal.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.client.JobServiceClient;
import com.capg.jobportal.dao.ApplicationRepository;
import com.capg.jobportal.dto.ApplicationResponse;
import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.dto.JobClientResponse;
import com.capg.jobportal.dto.RecruiterApplicationResponse;
import com.capg.jobportal.dto.StatusUpdateRequest;
import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
import com.capg.jobportal.exception.DuplicateApplicationException;
import com.capg.jobportal.exception.ForbiddenException;
import com.capg.jobportal.exception.InvalidStatusTransitionException;
import com.capg.jobportal.exception.ResourceNotFoundException;
import com.capg.jobportal.util.CloudinaryUtil;

@Service
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
    private final JobServiceClient jobServiceClient;
    private final CloudinaryUtil cloudinaryUtil;
 
    public ApplicationService(ApplicationRepository applicationRepository,
                               JobServiceClient jobServiceClient,
                               CloudinaryUtil cloudinaryUtil) {
        this.applicationRepository = applicationRepository;
        this.jobServiceClient = jobServiceClient;
        this.cloudinaryUtil = cloudinaryUtil;
    }
 
 
    public ApplicationResponse applyForJob(Long jobId, String coverLetter,
                                            boolean useExistingResume, String existingResumeUrl,
                                            MultipartFile resumeFile, Long seekerId) throws IOException {
 
        JobClientResponse job = jobServiceClient.getJobById(jobId, String.valueOf(seekerId), "JOB_SEEKER");
 
        if (job == null) {
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        }
 
        if ("DELETED".equals(job.getStatus()) || "CLOSED".equals(job.getStatus())) {
            throw new ResourceNotFoundException("This job is no longer accepting applications");
        }
 
        if (job.getDeadline() != null) {
            if (job.getDeadline().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Application deadline has passed: " + job.getDeadline());
            }
        }
 
        boolean alreadyApplied = applicationRepository.existsByUserIdAndJobId(seekerId, jobId);
        if (alreadyApplied) {
            throw new DuplicateApplicationException("You have already applied for this job");
        }
 
        String resumeUrl;
 
        if (useExistingResume) {
            if (existingResumeUrl == null || existingResumeUrl.isEmpty()) {
                throw new IllegalArgumentException("No saved resume found. Please upload a resume.");
            }
            resumeUrl = existingResumeUrl;
        } else {
            if (resumeFile == null || resumeFile.isEmpty()) {
                throw new IllegalArgumentException("Please upload a resume to apply");
            }
            resumeUrl = cloudinaryUtil.uploadResume(resumeFile);
        }
 
        Application application = new Application();
        application.setUserId(seekerId);
        application.setJobId(jobId);
        application.setResumeUrl(resumeUrl);
        application.setCoverLetter(coverLetter);
        application.setStatus(ApplicationStatus.APPLIED);
 
        Application savedApplication = applicationRepository.save(application);
 
        return ApplicationResponse.fromEntity(savedApplication);
    }
 
 
    public List<ApplicationResponse> getMyApplications(Long seekerId) {
        List<Application> applicationList = applicationRepository.findByUserId(seekerId);
 
        List<ApplicationResponse> responseList = new ArrayList<>();
        for (Application application : applicationList) {
            responseList.add(ApplicationResponse.fromEntity(application));
        }
 
        return responseList;
    }
 
 
    public ApplicationResponse getApplicationById(Long applicationId, Long seekerId) {
        Optional<Application> applicationOptional = applicationRepository.findByIdAndUserId(applicationId, seekerId);
 
        if (applicationOptional.isEmpty()) {
            throw new ForbiddenException("Application not found or you do not have permission to view it");
        }
 
        return ApplicationResponse.fromEntity(applicationOptional.get());
    }
 
 
    public List<RecruiterApplicationResponse> getApplicantsForJob(Long jobId, Long recruiterId) {
        JobClientResponse job = jobServiceClient.getJobById(jobId, String.valueOf(recruiterId), "RECRUITER");
 
        if (job == null) {
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        }
 
        if (!job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("You can only view applicants for jobs you have posted");
        }
 
        List<Application> applicationList = applicationRepository.findByJobId(jobId);
 
        List<RecruiterApplicationResponse> responseList = new ArrayList<>();
        for (Application application : applicationList) {
            responseList.add(RecruiterApplicationResponse.fromEntity(application));
        }
 
        return responseList;
    }
 
 
    public ApplicationResponse updateApplicationStatus(Long applicationId,
                                                        StatusUpdateRequest request,
                                                        Long recruiterId) {
 
        Optional<Application> applicationOptional = applicationRepository.findById(applicationId);
 
        if (applicationOptional.isEmpty()) {
            throw new ResourceNotFoundException("Application not found with id: " + applicationId);
        }
 
        Application application = applicationOptional.get();
 
        JobClientResponse job = jobServiceClient.getJobById(
                application.getJobId(), String.valueOf(recruiterId), "RECRUITER");
 
        if (job == null) {
            throw new ResourceNotFoundException("Job for this application was not found");
        }
 
        if (!job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("You can only update applications for jobs you have posted");
        }
 
        validateStatusTransition(application.getStatus(), request.getNewStatus());
 
        application.setStatus(request.getNewStatus());
 
        if (request.getRecruiterNote() != null && !request.getRecruiterNote().isEmpty()) {
            application.setRecruiterNote(request.getRecruiterNote());
        }
 
        Application updatedApplication = applicationRepository.save(application);
 
        return ApplicationResponse.fromEntity(updatedApplication);
    }
 
 
    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
 
        if (currentStatus == ApplicationStatus.REJECTED) {
            throw new InvalidStatusTransitionException(
                    "This application is already REJECTED. No further changes allowed.");
        }
 
        boolean isValid = false;
 
        if (currentStatus == ApplicationStatus.APPLIED) {
            if (newStatus == ApplicationStatus.UNDER_REVIEW) {
                isValid = true;
            }
        } else if (currentStatus == ApplicationStatus.UNDER_REVIEW) {
            if (newStatus == ApplicationStatus.SHORTLISTED || newStatus == ApplicationStatus.REJECTED) {
                isValid = true;
            }
        } else if (currentStatus == ApplicationStatus.SHORTLISTED) {
            if (newStatus == ApplicationStatus.REJECTED) {
                isValid = true;
            }
        }
 
        if (!isValid) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status from " + currentStatus + " to " + newStatus +
                    ". Valid transitions: APPLIED→UNDER_REVIEW, UNDER_REVIEW→SHORTLISTED/REJECTED, SHORTLISTED→REJECTED");
        }
    }
    
    
    public ApplicationStats getApplicationStats() {
        List<Application> all = applicationRepository.findAll();

        long applied = 0;
        long underReview = 0;
        long shortlisted = 0;
        long rejected = 0;

        for (Application app : all) {
            if (app.getStatus() == ApplicationStatus.APPLIED) applied++;
            else if (app.getStatus() == ApplicationStatus.UNDER_REVIEW) underReview++;
            else if (app.getStatus() == ApplicationStatus.SHORTLISTED) shortlisted++;
            else if (app.getStatus() == ApplicationStatus.REJECTED) rejected++;
        }

        ApplicationStats stats = new ApplicationStats();
        stats.setTotalApplications(all.size());
        stats.setAppliedCount(applied);
        stats.setUnderReviewCount(underReview);
        stats.setShortlistedCount(shortlisted);
        stats.setRejectedCount(rejected);

        return stats;
    }

}
