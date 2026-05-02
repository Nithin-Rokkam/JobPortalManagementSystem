export type UserRole = 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'BANNED';

export interface User {
    id: number;
    name: string;
    email: string;
    phone?: string;
    role: UserRole;
    status: UserStatus;
    profilePictureUrl?: string;
    resumeUrl?: string;
    createdAt: string;
    updatedAt: string;
}
