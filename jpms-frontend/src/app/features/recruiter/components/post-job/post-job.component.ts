import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RecruiterJobsApiService } from '../../services/recruiter-jobs-api.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-post-job',
    templateUrl: './post-job.component.html',
    styleUrls: ['./post-job.component.css']
})
export class PostJobComponent {
    step = 1;
    loading = false;

    step1Form: FormGroup;
    step2Form: FormGroup;

    jobTypes = ['FULL_TIME', 'PART_TIME', 'CONTRACT'];
    jobTypeLabels: Record<string, string> = { FULL_TIME: 'Full Time', PART_TIME: 'Part Time', CONTRACT: 'Contract' };

    constructor(
        private fb: FormBuilder,
        private recruiterJobsApi: RecruiterJobsApiService,
        private router: Router,
        private toast: ToastService,
        private cdr: ChangeDetectorRef
    ) {
        this.step1Form = this.fb.group({
            title: ['', Validators.required],
            companyName: ['', Validators.required],
            location: ['', Validators.required],
            jobType: ['', Validators.required]
        });

        this.step2Form = this.fb.group({
            description: ['', [Validators.required, Validators.minLength(100)]],
            salaryMin: [null],
            salaryMax: [null],
            experienceYears: [0, [Validators.required, Validators.min(0)]],
            deadline: ['', Validators.required]
        });
    }

    get today(): string { return new Date().toISOString().split('T')[0]; }

    nextStep(): void {
        if (this.step === 1 && this.step1Form.invalid) { this.step1Form.markAllAsTouched(); return; }
        if (this.step === 2 && this.step2Form.invalid) { this.step2Form.markAllAsTouched(); return; }
        this.step++;
    }

    prevStep(): void { this.step--; }

    get previewData(): any {
        return { ...this.step1Form.value, ...this.step2Form.value };
    }

    submit(): void {
        this.loading = true;
        const data = { ...this.step1Form.value, ...this.step2Form.value };
        this.recruiterJobsApi.postJob(data).subscribe({
            next: () => {
                this.toast.success('Job posted successfully! 🚀');
                this.router.navigate(['/recruiter/my-jobs']);
                this.cdr.detectChanges();
            },
            error: () => { this.loading = false; this.toast.error('Failed to post job'); this.cdr.detectChanges(); }
        });
    }
}

