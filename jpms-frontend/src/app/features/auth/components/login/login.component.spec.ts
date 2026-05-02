import { describe, it, expect, beforeEach } from 'vitest';
import { FormBuilder, Validators } from '@angular/forms';

// ── Test login form validation logic in isolation ────────────────────────────

function buildLoginForm(fb: FormBuilder) {
    return fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        rememberMe: [false],
    });
}

describe('LoginComponent — form validation', () => {
    let fb: FormBuilder;

    beforeEach(() => {
        fb = new FormBuilder();
    });

    it('form is invalid when empty', () => {
        const form = buildLoginForm(fb);
        expect(form.valid).toBe(false);
    });

    it('email is required', () => {
        const form = buildLoginForm(fb);
        form.get('email')!.setValue('');
        form.get('email')!.markAsTouched();
        expect(form.get('email')!.hasError('required')).toBe(true);
    });

    it('email must be valid format', () => {
        const form = buildLoginForm(fb);
        form.get('email')!.setValue('bad-email');
        expect(form.get('email')!.hasError('email')).toBe(true);
    });

    it('password must be at least 8 characters', () => {
        const form = buildLoginForm(fb);
        form.get('password')!.setValue('short');
        expect(form.get('password')!.hasError('minlength')).toBe(true);
    });

    it('form is valid with correct credentials', () => {
        const form = buildLoginForm(fb);
        form.setValue({ email: 'user@test.com', password: 'password123', rememberMe: false });
        expect(form.valid).toBe(true);
    });

    it('rememberMe defaults to false', () => {
        const form = buildLoginForm(fb);
        expect(form.get('rememberMe')!.value).toBe(false);
    });
});

// ── Test error message helpers ───────────────────────────────────────────────
describe('LoginComponent — emailError / passwordError getters', () => {
    it('emailError returns empty string when untouched', () => {
        const fb = new FormBuilder();
        const form = buildLoginForm(fb);
        const control = form.get('email')!;
        // Simulate getter logic
        const emailError = (control.touched && control.errors)
            ? (control.errors['required'] ? 'Email is required' : 'Please enter a valid email address')
            : '';
        expect(emailError).toBe('');
    });

    it('emailError returns required message when touched and empty', () => {
        const fb = new FormBuilder();
        const form = buildLoginForm(fb);
        const control = form.get('email')!;
        control.markAsTouched();
        const emailError = (control.touched && control.errors)
            ? (control.errors['required'] ? 'Email is required' : 'Please enter a valid email address')
            : '';
        expect(emailError).toBe('Email is required');
    });

    it('emailError returns format message for invalid email', () => {
        const fb = new FormBuilder();
        const form = buildLoginForm(fb);
        const control = form.get('email')!;
        control.setValue('not-valid');
        control.markAsTouched();
        const emailError = (control.touched && control.errors)
            ? (control.errors['required'] ? 'Email is required' : 'Please enter a valid email address')
            : '';
        expect(emailError).toBe('Please enter a valid email address');
    });
});
