export interface ApplicationsByStatus {
    APPLIED: number;
    UNDER_REVIEW: number;
    SHORTLISTED: number;
    REJECTED: number;
}

export interface PlatformReport {
    totalUsers: number;
    totalJobs: number;
    totalApplications: number;
    applicationsByStatus: ApplicationsByStatus;
}

export interface AuditLog {
    id: number;
    action: string;
    performedBy: string;
    details: string;
    createdAt: string;
}
