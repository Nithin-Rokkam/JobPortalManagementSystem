import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthApiService } from '../../services/auth-api.service';
import { ToastService } from '../../../../core/services/toast.service';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-forgot-password',
  template: `
    <div class="centered-container animate-fade-in-up">
      <div class="celestial-card inner-glow" style="max-width: 448px; width: 100%; padding: 2rem;">
        
        <div style="display: flex; justify-content: center; margin-bottom: 1.5rem;">
          <div style="background: var(--bg-surface); border: 1px solid var(--glass-border); padding: 0.75rem; border-radius: 12px;">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--royal-blue)" stroke-width="2">
              <path d="m21 2-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3"></path>
            </svg>
          </div>
        </div>

        <div style="text-align: center; margin-bottom: 2rem;">
          <h1 class="heading-celestial" style="font-size: 1.5rem; margin-bottom: 0.5rem;">Forgot Password</h1>
          <p style="color: var(--text-secondary); font-size: 0.875rem;">Enter your email to receive a reset code.</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label class="label-celestial">Email Address</label>
            <input 
              type="email" 
              formControlName="email" 
              placeholder="you@example.com"
              class="form-input"
            />
            <span class="error-text" *ngIf="form.get('email')?.touched && form.get('email')?.invalid">Valid email is required</span>
          </div>

          <button 
            type="submit" 
            class="btn-celestial" 
            style="width: 100%; margin-top: 1rem;"
            [disabled]="loading"
          >
            <span *ngIf="!loading">Send Reset Code</span>
            <span *ngIf="loading">Sending...</span>
          </button>
        </form>

        <div style="margin-top: 2rem; text-align: center; font-size: 0.875rem;">
          <a routerLink="/auth/login" style="color: var(--royal-blue); text-decoration: none; font-weight: 700;">Back to Login</a>
        </div>

      </div>
    </div>
  `
})
export class ForgotPasswordComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private toast: ToastService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    
    this.authApi.forgotPassword(this.form.value.email).subscribe({
      next: () => {
        this.toast.success('Reset code sent to your email!');
        this.router.navigate(['/auth/reset-password'], { queryParams: { email: this.form.value.email } });
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
        this.toast.error('Failed to send reset code. Please try again.');
      }
    });
  }
}
