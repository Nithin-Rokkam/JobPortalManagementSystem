import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { JobsApiService } from '../../services/jobs-api.service';
import { ApplicationsApiService } from '../../services/applications-api.service';
import { Job } from '../../../../shared/models/job.model';
import { Application } from '../../../../shared/models/application.model';

@Component({
    standalone: false,
    selector: 'app-seeker-dashboard',
    templateUrl: './seeker-dashboard.component.html',
    styleUrls: ['./seeker-dashboard.component.css']
})
export class SeekerDashboardComponent implements OnInit {
    user: any;
    recentJobs: Job[] = [];
    applications: Application[] = [];
    loadingJobs = true;
    loadingApps = true;

    counts = { total: 0, shortlisted: 0, underReview: 0, rejected: 0 };

    get greeting(): string {
        const h = new Date().getHours();
        if (h < 12) return 'Good morning';
        if (h < 17) return 'Good afternoon';
        return 'Good evening';
    }

    get recentApps(): Application[] {
        return [...this.applications].sort((a, b) => new Date(b.appliedAt).getTime() - new Date(a.appliedAt).getTime()).slice(0, 3);
    }

    constructor(
        private auth: AuthService,
        private jobsApi: JobsApiService,
        private appsApi: ApplicationsApiService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.user = this.auth.getUser();

        this.jobsApi.getAll(0, 4).subscribe({
            next: res => { this.recentJobs = res.content; this.loadingJobs = false; this.cdr.detectChanges(); },
            error: () => { this.loadingJobs = false; this.cdr.detectChanges(); }
        });

        this.appsApi.getMyApplications().subscribe({
            next: apps => {
                this.applications = apps;
                this.counts.total = apps.length;
                this.counts.shortlisted = apps.filter(a => a.status === 'SHORTLISTED').length;
                this.counts.underReview = apps.filter(a => a.status === 'UNDER_REVIEW').length;
                this.counts.rejected = apps.filter(a => a.status === 'REJECTED').length;
                this.loadingApps = false;
                this.cdr.detectChanges();
            },
            error: () => { this.loadingApps = false; this.cdr.detectChanges(); }
        });
    }
}

