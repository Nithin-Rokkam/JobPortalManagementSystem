import { describe, it, expect } from 'vitest';
import { TimeAgoPipe } from './time-ago.pipe';

describe('TimeAgoPipe', () => {
    const pipe = new TimeAgoPipe();

    function dateSecondsAgo(seconds: number): Date {
        return new Date(Date.now() - seconds * 1000);
    }

    it('returns "just now" for < 60 seconds ago', () => {
        expect(pipe.transform(dateSecondsAgo(30))).toBe('just now');
    });

    it('returns minutes ago for < 1 hour', () => {
        expect(pipe.transform(dateSecondsAgo(120))).toBe('2m ago');
    });

    it('returns hours ago for < 1 day', () => {
        expect(pipe.transform(dateSecondsAgo(7200))).toBe('2h ago');
    });

    it('returns days ago for < 1 month', () => {
        expect(pipe.transform(dateSecondsAgo(86400 * 3))).toBe('3d ago');
    });

    it('returns months ago for < 1 year', () => {
        expect(pipe.transform(dateSecondsAgo(2592000 * 2))).toBe('2mo ago');
    });

    it('returns years ago for >= 1 year', () => {
        expect(pipe.transform(dateSecondsAgo(31536000 * 2))).toBe('2y ago');
    });

    it('returns empty string for falsy value', () => {
        expect(pipe.transform('')).toBe('');
        expect(pipe.transform(null as any)).toBe('');
    });

    it('accepts string date input', () => {
        const twoMinutesAgo = new Date(Date.now() - 120000).toISOString();
        expect(pipe.transform(twoMinutesAgo)).toBe('2m ago');
    });
});
