package com.capg.jobportal.event;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobAppliedEvent
 * DESCRIPTION:
 * This event class represents the data payload for a job application
 * event in the system.
 *
 * It is used in asynchronous communication (e.g., RabbitMQ)
 * to notify other microservices when a job seeker applies for a job.
 *
 * It contains details such as:
 * 1. Job information (ID, Title)
 * 2. Job seeker details (ID, Name, Email)
 * 3. Recruiter ID (who posted the job)
 *
 * NOTE:
 * This class is used as a message payload for event-driven
 * microservice communication.
 * ================================================================
 */
public class JobAppliedEvent {
    private Long jobId;
    private String jobTitle;
    private Long seekerId;
    private String seekerName;
    private String seekerEmail;
    private Long recruiterId;   // postedBy from job

    public JobAppliedEvent() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Long getSeekerId() { return seekerId; }
    public void setSeekerId(Long seekerId) { this.seekerId = seekerId; }

    public String getSeekerName() { return seekerName; }
    public void setSeekerName(String seekerName) { this.seekerName = seekerName; }

    public String getSeekerEmail() { return seekerEmail; }
    public void setSeekerEmail(String seekerEmail) { this.seekerEmail = seekerEmail; }

    public Long getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Long recruiterId) { this.recruiterId = recruiterId; }
}