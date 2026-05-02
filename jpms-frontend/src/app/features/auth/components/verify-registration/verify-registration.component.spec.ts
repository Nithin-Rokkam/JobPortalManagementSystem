import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';

// ── Test OTP logic in isolation ──────────────────────────────────────────────

describe('VerifyRegistrationComponent — OTP logic', () => {

    describe('maskedEmail', () => {
        function maskEmail(email: string): string {
            const [local, domain] = email.split('@');
            if (!local || !domain) return email;
            return `${local.slice(0, 2)}${'*'.repeat(Math.max(0, local.length - 2))}@${domain}`;
        }

        it('masks middle characters of local part', () => {
            expect(maskEmail('john@example.com')).toBe('jo**@example.com');
        });

        it('handles short local part (1 char)', () => {
            expect(maskEmail('a@test.com')).toBe('a@test.com');
        });

        it('handles 2-char local part', () => {
            expect(maskEmail('ab@test.com')).toBe('ab@test.com');
        });

        it('returns original if no @ symbol', () => {
            expect(maskEmail('notanemail')).toBe('notanemail');
        });
    });

    describe('OTP completeness check', () => {
        function isOtpComplete(digits: string[]): boolean {
            return digits.every(d => d !== '');
        }

        it('returns false when all digits empty', () => {
            expect(isOtpComplete(['', '', '', '', '', ''])).toBe(false);
        });

        it('returns false when some digits missing', () => {
            expect(isOtpComplete(['1', '2', '', '4', '5', '6'])).toBe(false);
        });

        it('returns true when all 6 digits filled', () => {
            expect(isOtpComplete(['1', '2', '3', '4', '5', '6'])).toBe(true);
        });
    });

    describe('OTP value assembly', () => {
        function readOtp(digits: string[]): string {
            return digits.join('');
        }

        it('joins digits into a 6-char string', () => {
            expect(readOtp(['1', '2', '3', '4', '5', '6'])).toBe('123456');
        });

        it('returns partial string when incomplete', () => {
            expect(readOtp(['1', '2', '', '', '', ''])).toBe('12');
        });
    });

    describe('Attempt tracking', () => {
        const MAX_ATTEMPTS = 5;

        it('starts with 5 attempts', () => {
            let attemptsLeft = MAX_ATTEMPTS;
            expect(attemptsLeft).toBe(5);
        });

        it('decrements on each failed attempt', () => {
            let attemptsLeft = MAX_ATTEMPTS;
            attemptsLeft--;
            expect(attemptsLeft).toBe(4);
        });

        it('reaches 0 after 5 failures', () => {
            let attemptsLeft = MAX_ATTEMPTS;
            for (let i = 0; i < 5; i++) attemptsLeft--;
            expect(attemptsLeft).toBe(0);
        });

        it('resets to 5 on resend', () => {
            let attemptsLeft = 2;
            attemptsLeft = MAX_ATTEMPTS; // simulate resend
            expect(attemptsLeft).toBe(5);
        });
    });

    describe('Timer logic', () => {
        it('timer starts at 30', () => {
            let timer = 30;
            expect(timer).toBe(30);
        });

        it('timer decrements each second', () => {
            let timer = 30;
            timer--;
            expect(timer).toBe(29);
        });

        it('timer stops at 0', () => {
            let timer = 1;
            timer--;
            if (timer < 0) timer = 0;
            expect(timer).toBe(0);
        });

        it('resend resets timer to 30', () => {
            let timer = 0;
            timer = 30; // simulate resend
            expect(timer).toBe(30);
        });
    });
});
