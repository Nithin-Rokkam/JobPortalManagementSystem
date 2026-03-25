package com.capg.jobportal.dto;

public class ApplicationStats {
	
	private long totalApplications;
    private long appliedCount;
    private long underReviewCount;
    private long shortlistedCount;
    private long rejectedCount;


    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long v) { this.totalApplications = v; }

    public long getAppliedCount() { return appliedCount; }
    public void setAppliedCount(long v) { this.appliedCount = v; }

    public long getUnderReviewCount() { return underReviewCount; }
    public void setUnderReviewCount(long v) { this.underReviewCount = v; }

    public long getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(long v) { this.shortlistedCount = v; }

    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long v) { this.rejectedCount = v; }    
    
}
