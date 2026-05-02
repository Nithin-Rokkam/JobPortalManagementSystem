package com.capg.jobportal.event;

<<<<<<< HEAD



import java.math.BigDecimal;

=======
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobPostedEvent
 * DESCRIPTION:
 * RabbitMQ event payload published when a new job is posted.
 * Lombok generates all boilerplate. Custom all-args constructor
 * kept for the existing call site in JobService.
 * ================================================================
 */
@Data
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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

<<<<<<< HEAD
    public JobPostedEvent() {
    }

=======
    /** Kept for backward compatibility with existing JobService call site */
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    public JobPostedEvent(Long jobId, long recruiterId, String title, String companyName,
                          String location, String jobType,
                          BigDecimal salary, Integer experienceYears,
                          String description) {
<<<<<<< HEAD
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
=======
        this.jobId           = jobId;
        this.recruiterId     = recruiterId;
        this.title           = title;
        this.companyName     = companyName;
        this.location        = location;
        this.jobType         = jobType;
        this.salary          = salary;
        this.experienceYears = experienceYears;
        this.description     = description;
    }
}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
