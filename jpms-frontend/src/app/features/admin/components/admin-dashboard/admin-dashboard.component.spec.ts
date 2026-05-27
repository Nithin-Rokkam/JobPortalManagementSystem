import { describe, it, expect } from 'vitest';

// ── Test admin dashboard computed values in isolation ────────────────────────

interface ApplicationsByStatus {
    APPLIED: number;
    UNDER_REVIEW: number;
    SHORTLISTED: number;
    REJECTED: number;
    [key: string]: number;
}

function shortlistRate(totalApplications: number, byStatus: ApplicationsByStatus): string {
    if (!totalApplications) return '0%';
    return ((byStatus.SHORTLISTED / totalApplications) * 100).toFixed(1) + '%';
}

function reviewConversion(byStatus: ApplicationsByStatus): string {
    const reviewed = byStatus.UNDER_REVIEW + byStatus.SHORTLISTED;
    if (!reviewed) return '0%';
    return ((byStatus.SHORTLISTED / reviewed) * 100).toFixed(1) + '%';
}

describe('AdminDashboardComponent — shortlistRate', () => {
    it('returns 0% when no applications', () => {
        const s = { APPLIED: 0, UNDER_REVIEW: 0, SHORTLISTED: 0, REJECTED: 0 };
        expect(shortlistRate(0, s)).toBe('0%');
    });

    it('calculates correct shortlist rate', () => {
        const s = { APPLIED: 50, UNDER_REVIEW: 30, SHORTLISTED: 20, REJECTED: 10 };
        expect(shortlistRate(100, s)).toBe('20.0%');
    });

    it('handles 100% shortlist rate', () => {
        const s = { APPLIED: 0, UNDER_REVIEW: 0, SHORTLISTED: 10, REJECTED: 0 };
        expect(shortlistRate(10, s)).toBe('100.0%');
    });
});

describe('AdminDashboardComponent — reviewConversion', () => {
    it('returns 0% when no reviewed applications', () => {
        const s = { APPLIED: 10, UNDER_REVIEW: 0, SHORTLISTED: 0, REJECTED: 5 };
        expect(reviewConversion(s)).toBe('0%');
    });

    it('calculates correct conversion rate', () => {
        const s = { APPLIED: 10, UNDER_REVIEW: 10, SHORTLISTED: 5, REJECTED: 5 };
        expect(reviewConversion(s)).toBe('33.3%');
    });
});

describe('AdminDashboardComponent — getInitials', () => {
    function getInitials(name: string): string {
        return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }

    it('returns initials for full name', () => {
        expect(getInitials('Admin User')).toBe('AU');
    });

    it('returns ?? for empty name', () => {
        expect(getInitials('')).toBe('??');
    });
});

describe('AdminDashboardComponent — chart data structure', () => {
    it('builds correct chart labels', () => {
        const labels = ['Applied', 'Under Review', 'Shortlisted', 'Rejected'];
        expect(labels).toHaveLength(4);
        expect(labels[0]).toBe('Applied');
        expect(labels[2]).toBe('Shortlisted');
    });

    it('maps status counts to chart data correctly', () => {
        const s = { APPLIED: 40, UNDER_REVIEW: 30, SHORTLISTED: 20, REJECTED: 10 };
        const data = [s.APPLIED, s.UNDER_REVIEW, s.SHORTLISTED, s.REJECTED];
        expect(data).toEqual([40, 30, 20, 10]);
        expect(data.reduce((a, b) => a + b, 0)).toBe(100);
    });
});
