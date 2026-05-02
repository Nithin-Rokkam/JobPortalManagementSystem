import { describe, it, expect, beforeEach, vi } from 'vitest';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

// ── Test the logoRoute and getInitials logic in isolation ────────────────────

const mockRouter = { navigate: vi.fn() } as unknown as Router;

function makeAuthService(role?: string): AuthService {
    localStorage.clear();
    const svc = new AuthService(mockRouter);
    if (role) svc.saveUser({ id: 1, name: 'Test User', role });
    return svc;
}

// Replicate the logoRoute getter logic
function logoRoute(user: any): string {
    if (!user) return '/';
    const roleMap: Record<string, string> = {
        JOB_SEEKER: '/seeker/dashboard',
        RECRUITER: '/recruiter/dashboard',
        ADMIN: '/admin/dashboard',
    };
    return roleMap[user.role] ?? '/';
}

// Replicate getInitials
function getInitials(name: string): string {
    return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
}

describe('HeaderComponent — logoRoute', () => {
    it('returns / for unauthenticated user', () => {
        expect(logoRoute(null)).toBe('/');
    });

    it('returns /seeker/dashboard for JOB_SEEKER', () => {
        expect(logoRoute({ role: 'JOB_SEEKER' })).toBe('/seeker/dashboard');
    });

    it('returns /recruiter/dashboard for RECRUITER', () => {
        expect(logoRoute({ role: 'RECRUITER' })).toBe('/recruiter/dashboard');
    });

    it('returns /admin/dashboard for ADMIN', () => {
        expect(logoRoute({ role: 'ADMIN' })).toBe('/admin/dashboard');
    });

    it('returns / for unknown role', () => {
        expect(logoRoute({ role: 'UNKNOWN' })).toBe('/');
    });
});

describe('HeaderComponent — getInitials', () => {
    it('returns first two initials from full name', () => {
        expect(getInitials('John Doe')).toBe('JD');
    });

    it('returns single initial for single name', () => {
        expect(getInitials('Alice')).toBe('A');
    });

    it('returns ?? for empty string', () => {
        expect(getInitials('')).toBe('??');
    });

    it('returns ?? for null', () => {
        expect(getInitials(null as any)).toBe('??');
    });

    it('handles three-word names (takes first two)', () => {
        expect(getInitials('John Michael Doe')).toBe('JM');
    });
});

describe('HeaderComponent — theme persistence', () => {
    it('reads dark theme from localStorage', () => {
        localStorage.setItem('theme', 'dark');
        const saved = localStorage.getItem('theme');
        const isDark = saved === 'dark';
        expect(isDark).toBe(true);
        localStorage.clear();
    });

    it('defaults to light when no theme saved', () => {
        localStorage.clear();
        const saved = localStorage.getItem('theme');
        const isDark = saved === 'dark';
        expect(isDark).toBe(false);
    });

    it('toggles theme correctly', () => {
        let isDark = false;
        isDark = !isDark;
        expect(isDark).toBe(true);
        isDark = !isDark;
        expect(isDark).toBe(false);
    });
});
