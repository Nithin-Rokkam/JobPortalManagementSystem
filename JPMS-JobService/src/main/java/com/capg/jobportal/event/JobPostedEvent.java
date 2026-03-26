package com.capg.jobportal.event;




import java.math.BigDecimal;

public class JobPostedEvent {

    private Long jobId;
    private Long recruiterId;
    private String title;
    private String companyName;
    private String location;
    private String jobType;
    private BigDecimal salary;
    private Integer experienceYears;
    private String description;

    public JobPostedEvent() {
    }

    public JobPostedEvent(Long jobId, long recruiterId, String title, String companyName,
                          String location, String jobType,
                          BigDecimal salary, Integer experienceYears,
                          String description) {
        this.jobId = jobId;
        this.recruiterId = recruiterId;
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.jobType = jobType;
        this.salary = salary;
        this.experienceYears = experienceYears;
        this.description = description;
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Long getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Long recruiterId) { this.recruiterId = recruiterId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}