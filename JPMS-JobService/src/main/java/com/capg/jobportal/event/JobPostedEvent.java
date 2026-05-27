package com.capg.jobportal.event;

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

    /** Kept for backward compatibility with existing JobService call site */
    public JobPostedEvent(Long jobId, long recruiterId, String title, String companyName,
                          String location, String jobType,
                          BigDecimal salary, Integer experienceYears,
                          String description) {
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
