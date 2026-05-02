package com.capg.jobportal.event;

<<<<<<< HEAD
=======
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobAppliedEvent
 * DESCRIPTION:
 * RabbitMQ event published when a seeker applies for a job.
 * Lombok generates all boilerplate.
 * ================================================================
 */
@Data
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
public class JobAppliedEvent {
    private Long jobId;
    private String jobTitle;
    private Long seekerId;
    private String seekerName;
    private String seekerEmail;
<<<<<<< HEAD
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
=======
    private Long recruiterId;
}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
