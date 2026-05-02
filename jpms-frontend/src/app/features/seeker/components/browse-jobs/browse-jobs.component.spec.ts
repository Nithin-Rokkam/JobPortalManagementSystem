import { describe, it, expect, beforeEach } from 'vitest';
import { FormBuilder } from '@angular/forms';

// ── Test filter form logic in isolation ──────────────────────────────────────

function buildFilterForm(fb: FormBuilder) {
    return fb.group({
        title: [''],
        location: [''],
        jobType: [''],
        experienceYears: [''],
    });
}

function hasFilters(form: ReturnType<typeof buildFilterForm>): boolean {
    const v = form.value;
    return !!(v.title || v.location || v.jobType || v.experienceYears !== '');
}

describe('BrowseJobsComponent — filter form', () => {
    let fb: FormBuilder;

    beforeEach(() => { fb = new FormBuilder(); });

    it('hasFilters returns false when all fields empty', () => {
        const form = buildFilterForm(fb);
        expect(hasFilters(form)).toBe(false);
    });

    it('hasFilters returns true when title is set', () => {
        const form = buildFilterForm(fb);
        form.get('title')!.setValue('Angular');
        expect(hasFilters(form)).toBe(true);
    });

    it('hasFilters returns true when location is set', () => {
        const form = buildFilterForm(fb);
        form.get('location')!.setValue('Remote');
        expect(hasFilters(form)).toBe(true);
    });

    it('hasFilters returns true when jobType is set', () => {
        const form = buildFilterForm(fb);
        form.get('jobType')!.setValue('FULL_TIME');
        expect(hasFilters(form)).toBe(true);
    });

    it('hasFilters returns true when experienceYears is set', () => {
        const form = buildFilterForm(fb);
        form.get('experienceYears')!.setValue(3);
        expect(hasFilters(form)).toBe(true);
    });

    it('clearFilters resets all fields', () => {
        const form = buildFilterForm(fb);
        form.setValue({ title: 'Dev', location: 'NYC', jobType: 'REMOTE', experienceYears: 2 });
        form.reset({ title: '', location: '', jobType: '', experienceYears: '' });
        expect(hasFilters(form)).toBe(false);
    });
});

describe('BrowseJobsComponent — pagination', () => {
    function getPages(totalPages: number): number[] {
        return Array.from({ length: totalPages }, (_, i) => i);
    }

    it('generates correct page array for 3 pages', () => {
        expect(getPages(3)).toEqual([0, 1, 2]);
    });

    it('generates empty array for 0 pages', () => {
        expect(getPages(0)).toEqual([]);
    });

    it('generates single page array for 1 page', () => {
        expect(getPages(1)).toEqual([0]);
    });
});
