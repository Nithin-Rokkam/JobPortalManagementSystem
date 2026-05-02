import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { JobsApiService } from '../../../seeker/services/jobs-api.service';
import { RecruiterJobsApiService } from '../../services/recruiter-jobs-api.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-edit-job',
    templateUrl: './edit-job.component.html',
    styleUrls: ['./edit-job.component.css']
})
export class EditJobComponent implements OnInit {
    form: FormGroup;
    loading = false;
    jobId!: number;
    jobTypes = ['FULL_TIME', 'PART_TIME', 'REMOTE', 'CONTRACT'];
    jobTypeLabels: Record<string, string> = { FULL_TIME: 'Full Time', PART_TIME: 'Part Time', REMOTE: 'Remote', CONTRACT: 'Contract' };

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private jobsApi: JobsApiService,
        private recruiterJobsApi: RecruiterJobsApiService,
        private toast: ToastService,
        private cdr: ChangeDetectorRef
    ) {
        this.form = this.fb.group({
            title: ['', Validators.required],
            companyName: ['', Validators.required],
            location: ['', Validators.required],
            jobType: ['', Validators.required],
            description: ['', [Validators.required, Validators.minLength(100)]],
            salaryMin: [null],
            salaryMax: [null],
            experienceYears: [0],
            deadline: ['', Validators.required]
        });
    }

    ngOnInit(): void {
        this.jobId = Number(this.route.snapshot.paramMap.get('id'));
        this.jobsApi.getById(this.jobId).subscribe({
            next: job => {
                this.form.patchValue({
                    ...job,
                    deadline: job.deadline?.split('T')[0] || job.deadline
                });
                this.cdr.detectChanges();
            },
            error: () => { this.toast.error('Job not found'); this.router.navigate(['/recruiter/my-jobs']); this.cdr.detectChanges(); }
        });
    }

    get today(): string { return new Date().toISOString().split('T')[0]; }

    onSubmit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.loading = true;
        this.recruiterJobsApi.updateJob(this.jobId, this.form.value).subscribe({
            next: () => {
                this.toast.success('Job updated successfully');
                this.router.navigate(['/recruiter/my-jobs']);
                this.cdr.detectChanges();
            },
            error: () => { this.loading = false; this.toast.error('Failed to update job'); this.cdr.detectChanges(); }
        });
    }
}

