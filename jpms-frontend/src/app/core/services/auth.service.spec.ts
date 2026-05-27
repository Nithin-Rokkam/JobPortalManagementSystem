import { describe, it, expect, beforeEach, vi } from 'vitest';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

// ── Minimal Router mock ──────────────────────────────────────────────────────
const mockRouter = { navigate: vi.fn() } as unknown as Router;

// ── Helpers ──────────────────────────────────────────────────────────────────
function makeService(): AuthService {
    // Clear localStorage before each test
    localStorage.clear();
    return new AuthService(mockRouter);
}

// ── Tests ────────────────────────────────────────────────────────────────────
describe('AuthService', () => {

    describe('saveTokens / getAccessToken / getRefreshToken', () => {
        it('stores and retrieves access token', () => {
            const svc = makeService();
            svc.saveTokens('access-abc', 'refresh-xyz');
            expect(svc.getAccessToken()).toBe('access-abc');
        });

        it('stores and retrieves refresh token', () => {
            const svc = makeService();
            svc.saveTokens('access-abc', 'refresh-xyz');
            expect(svc.getRefreshToken()).toBe('refresh-xyz');
        });
    });

    describe('saveUser / getUser / getRole', () => {
        it('saves and retrieves user object', () => {
            const svc = makeService();
            const user = { id: 1, name: 'Alice', email: 'alice@test.com', role: 'JOB_SEEKER' };
            svc.saveUser(user);
            expect(svc.getUser()).toEqual(user);
        });

        it('returns correct role', () => {
            const svc = makeService();
            svc.saveUser({ role: 'RECRUITER' });
            expect(svc.getRole()).toBe('RECRUITER');
        });

        it('returns null role when no user', () => {
            const svc = makeService();
            expect(svc.getRole()).toBeNull();
        });
    });

    describe('isLoggedIn', () => {
        it('returns false when no token', () => {
            const svc = makeService();
            expect(svc.isLoggedIn()).toBe(false);
        });

        it('returns true when token exists', () => {
            const svc = makeService();
            svc.saveTokens('tok', 'ref');
            expect(svc.isLoggedIn()).toBe(true);
        });
    });

    describe('logout', () => {
        it('clears tokens and user, then navigates to login', () => {
            const svc = makeService();
            svc.saveTokens('tok', 'ref');
            svc.saveUser({ id: 1, role: 'JOB_SEEKER' });
            svc.logout();
            expect(svc.getAccessToken()).toBeNull();
            expect(svc.getUser()).toBeNull();
            expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
        });
    });

    describe('redirectByRole', () => {
        it('navigates seeker to /seeker/dashboard', () => {
            const svc = makeService();
            svc.saveUser({ role: 'JOB_SEEKER' });
            svc.redirectByRole();
            expect(mockRouter.navigate).toHaveBeenCalledWith(['/seeker/dashboard']);
        });

        it('navigates recruiter to /recruiter/dashboard', () => {
            const svc = makeService();
            svc.saveUser({ role: 'RECRUITER' });
            svc.redirectByRole();
            expect(mockRouter.navigate).toHaveBeenCalledWith(['/recruiter/dashboard']);
        });

        it('navigates admin to /admin/dashboard', () => {
            const svc = makeService();
            svc.saveUser({ role: 'ADMIN' });
            svc.redirectByRole();
            expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
        });

        it('navigates to login when no role', () => {
            const svc = makeService();
            svc.redirectByRole();
            expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
        });
    });

    describe('currentUser$ observable', () => {
        it('emits null initially when no saved user', () => {
            const svc = makeService();
            let emitted: any;
            svc.currentUser$.subscribe(u => (emitted = u));
            expect(emitted).toBeNull();
        });

        it('emits updated user after saveUser', () => {
            const svc = makeService();
            const user = { id: 2, role: 'ADMIN' };
            let emitted: any;
            svc.currentUser$.subscribe(u => (emitted = u));
            svc.saveUser(user);
            expect(emitted).toEqual(user);
        });
    });
});
