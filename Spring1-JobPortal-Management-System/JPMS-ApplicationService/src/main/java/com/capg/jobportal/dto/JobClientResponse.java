package com.capg.jobportal.dto;

import java.time.LocalDate;

public class JobClientResponse {

	private Long id;
    private String title;
    private String status;
    private Long postedBy;
    private LocalDate deadline;   // changed from String to LocalDate

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getPostedBy() { return postedBy; }
    public void setPostedBy(Long postedBy) { this.postedBy = postedBy; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    
    

}
