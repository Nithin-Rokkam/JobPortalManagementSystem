import { describe, it, expect, beforeEach, vi } from 'vitest';
import { FormBuilder, Validators } from '@angular/forms';

// ── We test the form logic in isolation (no Angular TestBed needed) ──────────

function buildForm(fb: FormBuilder) {
    return fb.group({
        name: ['', [Validators.required, Validators.minLength(2)]],
        email: ['', [Validators.required, Validators.email]],
        phone: ['', [Validators.pattern(/^\d{10,15}$/)]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        role: ['JOB_SEEKER', Validators.required],
    });
}

describe('RegisterComponent — form validation', () => {
    let fb: FormBuilder;

    beforeEach(() => {
        fb = new FormBuilder();
    });

    it('form is invalid when empty', () => {
        const form = buildForm(fb);
        expect(form.valid).toBe(false);
    });

    it('name is required', () => {
        const form = buildForm(fb);
        form.get('name')!.setValue('');
        expect(form.get('name')!.hasError('required')).toBe(true);
    });

    it('name must be at least 2 characters', () => {
        const form = buildForm(fb);
        form.get('name')!.setValue('A');
        expect(form.get('name')!.hasError('minlength')).toBe(true);
    });

    it('email must be valid format', () => {
        const form = buildForm(fb);
        form.get('email')!.setValue('not-an-email');
        expect(form.get('email')!.hasError('email')).toBe(true);
    });

    it('password must be at least 8 characters', () => {
        const form = buildForm(fb);
        form.get('password')!.setValue('short');
        expect(form.get('password')!.hasError('minlength')).toBe(true);
    });

    it('phone pattern allows 10-digit number', () => {
        const form = buildForm(fb);
        form.get('phone')!.setValue('9876543210');
        expect(form.get('phone')!.valid).toBe(true);
    });

    it('phone pattern rejects letters', () => {
        const form = buildForm(fb);
        form.get('phone')!.setValue('abcdefghij');
        expect(form.get('phone')!.hasError('pattern')).toBe(true);
    });

    it('form is valid with correct values', () => {
        const form = buildForm(fb);
        form.setValue({
            name: 'John Doe',
            email: 'john@example.com',
            phone: '9876543210',
            password: 'password123',
            role: 'JOB_SEEKER',
        });
        expect(form.valid).toBe(true);
    });

    it('role defaults to JOB_SEEKER', () => {
        const form = buildForm(fb);
        expect(form.get('role')!.value).toBe('JOB_SEEKER');
    });

    it('role can be set to RECRUITER', () => {
        const form = buildForm(fb);
        form.get('role')!.setValue('RECRUITER');
        expect(form.get('role')!.value).toBe('RECRUITER');
    });
});
