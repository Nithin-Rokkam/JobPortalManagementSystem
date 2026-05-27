import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  standalone: false,
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {
  form: FormGroup;
  loading = false;

  get isRecruiter(): boolean {
    return this.form.get('role')?.value === 'RECRUITER';
  }

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private router: Router,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.pattern(/^\d{10,15}$/)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      role: ['JOB_SEEKER', Validators.required],
      companyName: ['']
    });
  }

  ngOnInit(): void {
    // Trigger a clean detection cycle after view init
    this.cdr.detectChanges();
  }

  selectRole(role: string): void {
    this.form.get('role')?.setValue(role);
    const companyCtrl = this.form.get('companyName');
    if (role === 'RECRUITER') {
      companyCtrl?.setValidators([Validators.required, Validators.minLength(2)]);
    } else {
      companyCtrl?.clearValidators();
    }
    companyCtrl?.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.cdr.detectChanges();

    this.authApi.register(this.form.value).subscribe({
      next: () => {
        this.toast.success('Account created! Please check your email for the verification code.');
        this.router.navigate(['/auth/verify-registration'], {
          queryParams: { email: this.form.value.email }
        });
      },
      error: (err: any) => {
        this.loading = false;
        this.cdr.detectChanges();
        let errorMsg = 'Something went wrong. Please try again.';
        if (err.status === 409) {
          errorMsg = 'This email is already registered.';
        }
        this.toast.error(errorMsg);
      }
    });
  }
}
