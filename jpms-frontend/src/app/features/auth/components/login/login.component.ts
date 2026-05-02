import { Component, HostListener, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    form: FormGroup;
    loading = false;
    errorMsg = '';
    showPassword = false;
    capsLockOn = false;
    magicLinkMode = false;

    constructor(
        private fb: FormBuilder,
        private authApi: AuthApiService,
        private auth: AuthService,
        private toast: ToastService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) {
        this.form = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(8)]],
            rememberMe: [false]
        });
    }

    ngOnInit(): void {
        // Redirect after view is initialized so CD is stable
        if (this.auth.isLoggedIn()) {
            this.auth.redirectByRole();
        }
    }

    @HostListener('window:keydown', ['$event'])
    onKeyDown(event: KeyboardEvent): void {
        this.capsLockOn = event.getModifierState && event.getModifierState('CapsLock');
    }

    @HostListener('window:keyup', ['$event'])
    onKeyUp(event: KeyboardEvent): void {
        this.capsLockOn = event.getModifierState && event.getModifierState('CapsLock');
    }

    get isFormValid(): boolean {
        if (this.magicLinkMode) {
            return this.form.get('email')?.valid || false;
        }
        return this.form.valid;
    }

    onSubmit(): void {
        if (this.magicLinkMode) {
            this.sendMagicLink();
            return;
        }

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading = true;
        this.errorMsg = '';
        this.cdr.detectChanges();

        this.authApi.login(this.form.value).subscribe({
            next: (res: any) => {
                this.auth.saveTokens(res.accessToken, res.refreshToken);
                this.auth.saveUser({
                    id: res.userId,
                    name: res.name,
                    email: res.email,
                    role: res.role,
                    profilePictureUrl: res.profilePictureUrl,
                    resumeUrl: res.resumeUrl
                });

                this.toast.success('Welcome back!');
                this.auth.redirectByRole();
            },
            error: (err: any) => {
                this.loading = false;
                this.cdr.detectChanges();
                const serverMsg: string = err?.error?.message || '';

                // Unverified account — redirect to OTP page
                if (serverMsg === 'EMAIL_NOT_VERIFIED') {
                    this.toast.info('Please verify your email first.');
                    this.router.navigate(['/auth/verify-registration'], {
                        queryParams: { email: this.form.value.email }
                    });
                    return;
                }

                if (serverMsg) {
                    this.errorMsg = serverMsg;
                } else if (err.status === 401) {
                    this.errorMsg = 'Invalid email or password. Please try again.';
                } else if (err.status === 403) {
                    this.errorMsg = 'Your account has been suspended. Contact support.';
                } else if (err.status === 0) {
                    this.errorMsg = 'Unable to connect to server. Please check your connection.';
                } else {
                    this.errorMsg = 'Something went wrong. Please try again later.';
                }
                this.toast.error(this.errorMsg);
            }
        });
    }

    onForgotPassword(): void {
        this.toast.info('Password reset feature coming soon!');
    }

    onSocialLogin(provider: 'google' | 'linkedin'): void {
        const providerName = provider === 'google' ? 'Google' : 'LinkedIn';
        this.toast.info(`${providerName} login integration coming soon!`);
    }

    onMagicLinkModeChange(): void {
        const passwordControl = this.form.get('password');
        if (!passwordControl) return;

        if (this.magicLinkMode) {
            passwordControl.clearValidators();
        } else {
            passwordControl.setValidators([Validators.required, Validators.minLength(8)]);
        }
        passwordControl.updateValueAndValidity();
    }

    sendMagicLink(): void {
        const emailControl = this.form.get('email');
        if (!emailControl?.valid) {
            emailControl?.markAsTouched();
            this.toast.error('Please enter a valid email address');
            return;
        }
        const email = emailControl.value;

        this.loading = true;
        setTimeout(() => {
            this.loading = false;
            this.toast.success(`Magic link sent to ${email}! Check your inbox.`);
        }, 1500);
    }

    get emailError(): string {
        const control = this.form.get('email');
        if (control?.touched && control?.errors) {
            if (control.errors['required']) return 'Email is required';
            if (control.errors['email']) return 'Please enter a valid email address';
        }
        return '';
    }

    get passwordError(): string {
        const control = this.form.get('password');
        if (control?.touched && control?.errors) {
            if (control.errors['required']) return 'Password is required';
            if (control.errors['minlength']) return 'Password must be at least 8 characters';
        }
        return '';
    }
}
