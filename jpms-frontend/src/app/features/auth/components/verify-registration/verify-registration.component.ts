import {
    Component, OnInit, OnDestroy,
    ChangeDetectionStrategy, ChangeDetectorRef, NgZone
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { ToastService } from '../../../../core/services/toast.service';

const MAX_ATTEMPTS = 5;

@Component({
    standalone: false,
    selector: 'app-verify-registration',
    templateUrl: './verify-registration.component.html',
    styleUrls: ['./verify-registration.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerifyRegistrationComponent implements OnInit, OnDestroy {

    email = '';
    loading = false;
    resendLoading = false;
    timer = 30;
    otpComplete = false;

    attemptsLeft = MAX_ATTEMPTS;   // 5 tries before forced redirect
    errorMsg = '';                 // shown below the OTP boxes

    private timerInterval: any;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private authApi: AuthApiService,
        private toast: ToastService,
        private ngZone: NgZone,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.email = this.route.snapshot.queryParamMap.get('email') || '';
        if (!this.email) {
            this.toast.error('Email is missing. Please register again.');
            this.router.navigate(['/auth/register']);
            return;
        }
        this.startTimer();
    }

    ngOnDestroy(): void {
        this.clearTimer();
    }

    // ── Helpers ─────────────────────────────────────────────────────

    get maskedEmail(): string {
        const [local, domain] = this.email.split('@');
        if (!local || !domain) return this.email;
        return `${local.slice(0, 2)}${'*'.repeat(Math.max(0, local.length - 2))}@${domain}`;
    }

    private box(i: number): HTMLInputElement | null {
        return document.getElementById('otp-' + i) as HTMLInputElement | null;
    }

    private focus(i: number): void {
        if (i >= 0 && i <= 5) this.box(i)?.focus();
    }

    private readOtp(): string {
        let val = '';
        for (let i = 0; i < 6; i++) val += (this.box(i)?.value || '');
        return val;
    }

    private clearBoxes(): void {
        for (let i = 0; i < 6; i++) {
            const b = this.box(i);
            if (b) b.value = '';
        }
        this.otpComplete = false;
    }

    private refreshComplete(): void {
        const complete = this.readOtp().length === 6;
        if (complete !== this.otpComplete) {
            this.otpComplete = complete;
            this.cdr.markForCheck();
        }
    }

    // ── Keyboard handler ─────────────────────────────────────────────

    onKeydown(event: KeyboardEvent, index: number): void {
        const key = event.key;
        const input = event.target as HTMLInputElement;

        if (/^\d$/.test(key)) {
            event.preventDefault();
            input.value = key;
            this.refreshComplete();
            if (index < 5) this.focus(index + 1);
            return;
        }

        if (key === 'Backspace') {
            event.preventDefault();
            if (input.value) {
                input.value = '';
            } else if (index > 0) {
                const prev = this.box(index - 1);
                if (prev) prev.value = '';
                this.focus(index - 1);
            }
            this.refreshComplete();
            return;
        }

        if (key === 'ArrowLeft') { event.preventDefault(); this.focus(index - 1); return; }
        if (key === 'ArrowRight') { event.preventDefault(); this.focus(index + 1); return; }
        if (key === 'Tab') return;
        if (key.length === 1) event.preventDefault();
    }

    // ── Paste: fill all boxes + auto-submit ──────────────────────────

    onPaste(event: ClipboardEvent): void {
        event.preventDefault();
        const digits = (event.clipboardData?.getData('text') || '')
            .replace(/\D/g, '')
            .slice(0, 6);

        for (let i = 0; i < 6; i++) {
            const b = this.box(i);
            if (b) b.value = digits[i] || '';
        }

        const nextEmpty = digits.length < 6 ? digits.length : 5;
        this.focus(nextEmpty);
        this.refreshComplete();

        if (digits.length === 6) {
            setTimeout(() => this.onVerify(), 150);
        }
    }

    // ── Verify ───────────────────────────────────────────────────────

    onVerify(): void {
        const otp = this.readOtp();
        if (otp.length < 6 || this.loading) return;

        this.loading = true;
        this.errorMsg = '';
        this.cdr.markForCheck();

        this.authApi.verifyRegistrationOtp(this.email, otp).subscribe({
            next: () => {
                this.toast.success('Email verified! You can now sign in.');
                this.router.navigate(['/auth/login']);
            },
            error: (err: any) => {
                this.loading = false;
                this.attemptsLeft--;

                // Clear boxes so user can re-enter cleanly
                this.clearBoxes();
                this.focus(0);

                if (this.attemptsLeft <= 0) {
                    // All attempts exhausted — tell user and redirect after 3s
                    this.errorMsg = 'Too many incorrect attempts. Redirecting to registration...';
                    this.cdr.markForCheck();
                    setTimeout(() => {
                        this.router.navigate(['/auth/register']);
                    }, 3000);
                    return;
                }

                // Show specific server message or a generic one with attempts remaining
                const serverMsg: string = err?.error?.message || '';
                if (serverMsg === 'OTP has expired. Please request a new one.') {
                    this.errorMsg = 'OTP has expired. Please request a new code below.';
                } else {
                    this.errorMsg = `Incorrect code. ${this.attemptsLeft} attempt${this.attemptsLeft === 1 ? '' : 's'} remaining.`;
                }

                this.cdr.markForCheck();
            }
        });
    }

    // ── Resend ───────────────────────────────────────────────────────

    onResend(): void {
        if (this.timer > 0 || this.resendLoading) return;
        this.resendLoading = true;
        this.cdr.markForCheck();

        this.authApi.resendRegistrationOtp(this.email).subscribe({
            next: () => {
                this.resendLoading = false;
                this.clearBoxes();
                this.focus(0);
                // Reset attempts on a fresh OTP
                this.attemptsLeft = MAX_ATTEMPTS;
                this.errorMsg = '';
                this.toast.success('New OTP sent to your email!');
                this.timer = 30;
                this.cdr.markForCheck();
                this.startTimer();
            },
            error: () => {
                this.resendLoading = false;
                this.cdr.markForCheck();
                this.toast.error('Failed to resend OTP. Please try again.');
            }
        });
    }

    // ── Timer ────────────────────────────────────────────────────────

    private startTimer(): void {
        this.clearTimer();
        this.ngZone.runOutsideAngular(() => {
            this.timerInterval = setInterval(() => {
                if (this.timer <= 0) { this.clearTimer(); return; }
                this.timer--;
                this.ngZone.run(() => this.cdr.markForCheck());
            }, 1000);
        });
    }

    private clearTimer(): void {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
        }
    }
}
