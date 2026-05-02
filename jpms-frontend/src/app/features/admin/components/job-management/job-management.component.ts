import { Component, OnInit } from '@angular/core';
import { AdminApiService } from '../../services/admin-api.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-job-management',
    templateUrl: './job-management.component.html',
    styleUrls: ['./job-management.component.css']
})
export class JobManagementComponent implements OnInit {
    jobs: any[] = [];
    loading = true;
    search = '';
    statusFilter = '';
    confirmDeleteId: number | null = null;

    constructor(private adminApi: AdminApiService, private toast: ToastService) { }

    ngOnInit(): void {
        this.adminApi.getJobs().subscribe({
            next: j => { this.jobs = j; this.loading = false; },
            error: () => { this.loading = false; }
        });
    }

    get filtered(): any[] {
        return this.jobs.filter(j => {
            const matchSearch = !this.search || j.title?.toLowerCase().includes(this.search.toLowerCase()) || j.companyName?.toLowerCase().includes(this.search.toLowerCase());
            const matchStatus = !this.statusFilter || j.status === this.statusFilter;
            return matchSearch && matchStatus;
        });
    }

    deleteJob(): void {
        if (!this.confirmDeleteId) return;
        this.adminApi.deleteJob(this.confirmDeleteId).subscribe({
            next: () => { this.jobs = this.jobs.filter(j => j.id !== this.confirmDeleteId); this.confirmDeleteId = null; this.toast.success('Job deleted'); },
            error: () => { this.toast.error('Failed to delete job'); this.confirmDeleteId = null; }
        });
    }
}

