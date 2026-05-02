package com.capg.jobportal.event;

import java.math.BigDecimal;
<<<<<<< HEAD

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobPostedEvent
 * DESCRIPTION:
 * This event class represents the data payload for a job posting
 * event in the system.
 *
 * It is used in asynchronous communication (e.g., RabbitMQ)
 * to notify other microservices when a new job is posted.
 *
 * It contains details such as:
 * 1. Job information (ID, Title, Description)
 * 2. Recruiter information (Recruiter ID)
 * 3. Job attributes (Company, Location, Type, Salary, Experience)
 *
 * NOTE:
 * This class is used as a message payload for event-driven
 * microservice communication.
 * ================================================================
 */
=======
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public JobPostedEvent() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public Long getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Long recruiterId) { this.recruiterId = recruiterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

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
}
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
