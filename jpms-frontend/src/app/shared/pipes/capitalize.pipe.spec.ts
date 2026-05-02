import { describe, it, expect } from 'vitest';
import { CapitalizePipe } from './capitalize.pipe';

describe('CapitalizePipe', () => {
    const pipe = new CapitalizePipe();

    it('capitalizes first letter of each word', () => {
        expect(pipe.transform('hello world')).toBe('Hello World');
    });

    it('converts SCREAMING_CASE to Title Case', () => {
        expect(pipe.transform('UNDER_REVIEW')).toBe('Under Review');
    });

    it('converts JOB_SEEKER to Job Seeker', () => {
        expect(pipe.transform('JOB_SEEKER')).toBe('Job Seeker');
    });

    it('handles already capitalized string', () => {
        expect(pipe.transform('Hello')).toBe('Hello');
    });

    it('handles lowercase string', () => {
        expect(pipe.transform('hello')).toBe('Hello');
    });

    it('handles empty string', () => {
        expect(pipe.transform('')).toBe('');
    });

    it('handles null gracefully', () => {
        expect(pipe.transform(null as any)).toBe('');
    });

    it('handles undefined gracefully', () => {
        expect(pipe.transform(undefined as any)).toBe('');
    });

    it('converts FULL_TIME to Full Time', () => {
        expect(pipe.transform('FULL_TIME')).toBe('Full Time');
    });

    it('converts SHORTLISTED to Shortlisted', () => {
        expect(pipe.transform('SHORTLISTED')).toBe('Shortlisted');
    });
});
