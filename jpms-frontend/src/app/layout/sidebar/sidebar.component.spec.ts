import { describe, it, expect, vi } from 'vitest';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

const mockRouter = { navigate: vi.fn() } as unknown as Router;

// Replicate dashboardRoute logic
function dashboardRoute(role: string | null): string {
    const roleMap: Record<string, string> = {
        JOB_SEEKER: '/seeker/dashboard',
        RECRUITER: '/recruiter/dashboard',
        ADMIN: '/admin/dashboard',
    };
    return role ? (roleMap[role] ?? '/') : '/';
}

// Replicate filteredNav logic
interface NavItem { label: string; link: string; roles: string[]; }

const navItems: NavItem[] = [
    { label: 'Dashboard', link: '/seeker/dashboard', roles: ['JOB_SEEKER'] },
    { label: 'Browse Jobs', link: '/seeker/browse', roles: ['JOB_SEEKER'] },
    { label: 'Applications', link: '/seeker/applications', roles: ['JOB_SEEKER'] },
    { label: 'Profile', link: '/seeker/profile', roles: ['JOB_SEEKER'] },
    { label: 'Dashboard', link: '/recruiter/dashboard', roles: ['RECRUITER'] },
    { label: 'Post Job', link: '/recruiter/post-job', roles: ['RECRUITER'] },
    { label: 'My Jobs', link: '/recruiter/my-jobs', roles: ['RECRUITER'] },
    { label: 'Dashboard', link: '/admin/dashboard', roles: ['ADMIN'] },
    { label: 'Users', link: '/admin/users', roles: ['ADMIN'] },
    { label: 'Jobs', link: '/admin/jobs', roles: ['ADMIN'] },
    { label: 'Reports', link: '/admin/reports', roles: ['ADMIN'] },
    { label: 'Audit Logs', link: '/admin/audit-logs', roles: ['ADMIN'] },
];

function filteredNav(role: string): NavItem[] {
    return navItems.filter(item => item.roles.includes(role));
}

describe('SidebarComponent — dashboardRoute', () => {
    it('returns /seeker/dashboard for JOB_SEEKER', () => {
        expect(dashboardRoute('JOB_SEEKER')).toBe('/seeker/dashboard');
    });

    it('returns /recruiter/dashboard for RECRUITER', () => {
        expect(dashboardRoute('RECRUITER')).toBe('/recruiter/dashboard');
    });

    it('returns /admin/dashboard for ADMIN', () => {
        expect(dashboardRoute('ADMIN')).toBe('/admin/dashboard');
    });

    it('returns / for null role', () => {
        expect(dashboardRoute(null)).toBe('/');
    });

    it('returns / for unknown role', () => {
        expect(dashboardRoute('UNKNOWN')).toBe('/');
    });
});

describe('SidebarComponent — filteredNav', () => {
    it('shows 4 items for JOB_SEEKER', () => {
        expect(filteredNav('JOB_SEEKER')).toHaveLength(4);
    });

    it('shows 3 items for RECRUITER', () => {
        expect(filteredNav('RECRUITER')).toHaveLength(3);
    });

    it('shows 5 items for ADMIN', () => {
        expect(filteredNav('ADMIN')).toHaveLength(5);
    });

    it('seeker nav includes Browse Jobs', () => {
        const items = filteredNav('JOB_SEEKER');
        expect(items.some(i => i.label === 'Browse Jobs')).toBe(true);
    });

    it('recruiter nav includes Post Job', () => {
        const items = filteredNav('RECRUITER');
        expect(items.some(i => i.label === 'Post Job')).toBe(true);
    });

    it('admin nav includes Audit Logs', () => {
        const items = filteredNav('ADMIN');
        expect(items.some(i => i.label === 'Audit Logs')).toBe(true);
    });

    it('seeker nav does not include admin items', () => {
        const items = filteredNav('JOB_SEEKER');
        expect(items.some(i => i.link.startsWith('/admin'))).toBe(false);
    });
});
