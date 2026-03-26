package com.capg.jobportal.event;

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