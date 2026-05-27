import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
import { JobsApiService } from '../../services/jobs-api.service';
import { ApplicationsApiService } from '../../services/applications-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { Job } from '../../../../shared/models/job.model';

@Component({
    standalone: false,
    selector: 'app-job-detail',
    templateUrl: './job-detail.component.html',
    styleUrls: ['./job-detail.component.css']
})
export class JobDetailComponent implements OnInit {
    job: Job | null = null;
    loading = true;
    alreadyApplied = false;
    showModal = false;
    applyLoading = false;
    useExistingResume = true;
    selectedFile: File | null = null;
    coverLetter = '';
    user: any;

    applyForm: FormGroup;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private jobsApi: JobsApiService,
        private appsApi: ApplicationsApiService,
        private auth: AuthService,
        private toast: ToastService,
        private cdr: ChangeDetectorRef
    ) {
        this.applyForm = this.fb.group({ coverLetter: [''] });
    }

    ngOnInit(): void {
        this.user = this.auth.getUser();
        if (!this.user?.resumeUrl) {
            this.useExistingResume = false;
        }
        const id = Number(this.route.snapshot.paramMap.get('id'));

        this.jobsApi.getById(id).subscribe({
            next: job => { this.job = job; this.loading = false; this.cdr.detectChanges(); },
            error: () => { this.loading = false; this.toast.error('Job not found'); this.router.navigate(['/seeker/browse']); this.cdr.detectChanges(); }
        });

        this.appsApi.getMyApplications().subscribe({
            next: apps => { this.alreadyApplied = apps.some(a => a.jobId === id); this.cdr.detectChanges(); }
        });
    }

    get isDeadlinePassed(): boolean {
        if (!this.job?.deadline) return false;
        return new Date(this.job.deadline) < new Date();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files?.length) this.selectedFile = input.files[0];
    }

    apply(): void {
        this.showModal = true;
    }

    submitApplication(): void {
        if (!this.job) return;
        this.applyLoading = true;

        this.appsApi.apply({
            jobId: this.job.id,
            coverLetter: this.applyForm.get('coverLetter')?.value || undefined,
            useExistingResume: this.useExistingResume,
            existingResumeUrl: this.useExistingResume ? this.user?.resumeUrl : undefined,
            resume: !this.useExistingResume ? this.selectedFile || undefined : undefined
        }).subscribe({
            next: () => {
                this.applyLoading = false;
                this.showModal = false;
                this.alreadyApplied = true;
                this.toast.success('Application submitted successfully! 🎉');
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                this.applyLoading = false;
                if (err.error && err.error.message) {
                    this.toast.error(err.error.message);
                } else {
                    this.toast.error('Failed to submit application');
                }
                this.cdr.detectChanges();
            }
        });
    }
}

