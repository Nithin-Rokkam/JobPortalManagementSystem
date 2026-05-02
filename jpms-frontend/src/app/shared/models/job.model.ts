export type JobType = 'FULL_TIME' | 'PART_TIME' | 'REMOTE' | 'CONTRACT';
export type JobStatus = 'ACTIVE' | 'CLOSED' | 'DRAFT' | 'DELETED';

export interface Job {
    id: number;
    title: string;
    companyName: string;
    location: string;
    description: string;
    jobType: JobType;
    status: JobStatus;
    salaryMin: number;
    salaryMax: number;
    experienceYears: number;
    deadline: string;
    postedBy: number;
    createdAt: string;
    updatedAt: string;
}

export interface PagedResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    currentPage: number;
    size: number;
}
