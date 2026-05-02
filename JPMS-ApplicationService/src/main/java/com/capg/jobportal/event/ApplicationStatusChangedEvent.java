package com.capg.jobportal.event;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ApplicationStatusChangedEvent
 * DESCRIPTION:
 * RabbitMQ event published when an application status changes.
 * Lombok generates all boilerplate.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class ApplicationStatusChangedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long seekerId;
    private String seekerName;
    private String seekerEmail;
    private String newStatus;
}
