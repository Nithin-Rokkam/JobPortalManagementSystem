package com.capg.jobportal.dto;

import com.capg.jobportal.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private ApplicationStatus newStatus;

    private String recruiterNote;
}
