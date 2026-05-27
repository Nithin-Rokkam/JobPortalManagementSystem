import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RecruiterJobsApiService } from '../../services/recruiter-jobs-api.service';
import { ToastService } from '../../../../core/services/toast.service';
import { Job } from '../../../../shared/models/job.model';

@Component({
    standalone: false,
    selector: 'app-my-jobs',
    templateUrl: './my-jobs.component.html',
    styleUrls: ['./my-jobs.component.css']
})
export class MyJobsComponent implements OnInit {
    jobs: Job[] = [];
    loading = true;
    totalPages = 0;
    currentPage = 0;
    confirmDeleteId: number | null = null;

    constructor(private recruiterJobsApi: RecruiterJobsApiService, private toast: ToastService, private cdr: ChangeDetectorRef) { }

    ngOnInit(): void { this.loadJobs(0); }

    loadJobs(page: number): void {
        this.loading = true;
        this.currentPage = page;
        this.recruiterJobsApi.getMyJobs(page, 10).subscribe({
            next: res => { this.jobs = res.content; this.totalPages = res.totalPages; this.loading = false; this.cdr.detectChanges(); },
            error: () => { this.loading = false; this.cdr.detectChanges(); }
        });
    }

    confirmDelete(id: number): void { this.confirmDeleteId = id; }

    deleteJob(): void {
        if (!this.confirmDeleteId) return;
        this.recruiterJobsApi.deleteJob(this.confirmDeleteId).subscribe({
            next: () => {
                this.jobs = this.jobs.filter(j => j.id !== this.confirmDeleteId);
                this.confirmDeleteId = null;
                this.toast.success('Job deleted');
                this.cdr.detectChanges();
            },
            error: () => { this.toast.error('Failed to delete job'); this.confirmDeleteId = null; this.cdr.detectChanges(); }
        });
    }

    get pages(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i); }
}

