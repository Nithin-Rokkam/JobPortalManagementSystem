package com.capg.jobportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobRequestDTO {

    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    private BigDecimal salary;

    @Min(value = 0, message = "Experience years must be 0 or more")
    private Integer experienceYears;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @Size(max = 1000, message = "Skills must not exceed 1000 characters")
    private String skillsRequired;

    @NotBlank(message = "Job description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private String status;

    @Future(message = "Deadline must be a future date")
    private LocalDate deadline;
}
