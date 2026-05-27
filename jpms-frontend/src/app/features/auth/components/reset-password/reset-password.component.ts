import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  standalone: false,
  selector: 'app-reset-password',
  template: `
    <div class="centered-container animate-fade-in-up">
      <div class="celestial-card inner-glow" style="max-width: 448px; width: 100%; padding: 2rem;">
        
        <div style="display: flex; justify-content: center; margin-bottom: 1.5rem;">
          <div style="background: var(--bg-surface); border: 1px solid var(--glass-border); padding: 0.75rem; border-radius: 12px;">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--royal-blue)" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
            </svg>
          </div>
        </div>

        <div style="text-align: center; margin-bottom: 2rem;">
          <h1 class="heading-celestial" style="font-size: 1.5rem; margin-bottom: 0.5rem;">Reset Password</h1>
          <p style="color: var(--text-secondary); font-size: 0.875rem;">Enter the code sent to your email and your new password.</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          
          <div class="form-group">
            <label class="label-celestial">Reset Code (OTP)</label>
            <input 
              type="text" 
              formControlName="otp" 
              placeholder="123456"
              class="form-input"
              maxlength="6"
              autocomplete="off"
            />
            <span class="error-text" *ngIf="form.get('otp')?.touched && form.get('otp')?.invalid">6-digit code is required</span>
          </div>

          <div class="form-group">
            <label class="label-celestial">New Password</label>
            <input 
              type="password" 
              formControlName="newPassword" 
              placeholder="••••••••"
              class="form-input"
              autocomplete="new-password"
            />
            <span class="error-text" *ngIf="form.get('newPassword')?.touched && form.get('newPassword')?.invalid">Min 8 characters required</span>
          </div>

          <button 
            type="submit" 
            class="btn-celestial" 
            style="width: 100%; margin-top: 1rem;"
            [disabled]="loading"
          >
            <span *ngIf="!loading">Reset Password</span>
            <span *ngIf="loading">Resetting...</span>
          </button>
        </form>

        <div style="margin-top: 2rem; text-align: center; font-size: 0.875rem;">
          <a routerLink="/auth/login" style="color: var(--royal-blue); text-decoration: none; font-weight: 700;">Back to Login</a>
        </div>

      </div>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  loading = false;
  email: string = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authApi: AuthApiService,
    private toast: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') || '';
    if (!this.email) {
      this.toast.error('Email is missing. Please restart the process.');
      this.router.navigate(['/auth/forgot-password']);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;

    const { otp, newPassword } = this.form.value;
    const cleanOtp = otp ? otp.trim() : '';
    
    this.authApi.resetPassword(this.email.trim(), cleanOtp, newPassword).subscribe({
      next: () => {
        this.toast.success('Password reset successful! Please sign in.');
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
        this.toast.error('Invalid or expired code. Please try again.');
      }
    });
  }
}
