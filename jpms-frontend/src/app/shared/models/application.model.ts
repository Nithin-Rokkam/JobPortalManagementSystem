export type ApplicationStatus = 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'SELECTED' | 'REJECTED';

export interface Application {
    id: number;
    jobId: number;
    jobTitle?: string;
    companyName?: string;
    userId: number;
    resumeUrl: string;
    coverLetter?: string;
    status: ApplicationStatus;
    appliedAt: string;
    updatedAt?: string;
}
