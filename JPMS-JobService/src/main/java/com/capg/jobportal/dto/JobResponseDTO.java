package com.capg.jobportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobResponseDTO {

    private Long id;
    private String title;
    private String companyName;
    private String location;
    private BigDecimal salary;
    private Integer experienceYears;
    private String jobType;
    private String skillsRequired;
    private String description;
    private String status;
    private LocalDate deadline;
    private Long postedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
