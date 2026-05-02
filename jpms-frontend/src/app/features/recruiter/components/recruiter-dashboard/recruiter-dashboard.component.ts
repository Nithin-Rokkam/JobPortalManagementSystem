import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { AuthApiService } from '../../../auth/services/auth-api.service';
import { RecruiterJobsApiService } from '../../services/recruiter-jobs-api.service';
import { Job } from '../../../../shared/models/job.model';

@Component({
    standalone: false,
    selector: 'app-recruiter-dashboard',
    templateUrl: './recruiter-dashboard.component.html',
    styleUrls: ['./recruiter-dashboard.component.css']
})
export class RecruiterDashboardComponent implements OnInit {
    user: any;
    profile: any;
    jobs: Job[] = [];
    loading = true;
    counts = { total: 0, active: 0, closed: 0, draft: 0 };

    constructor(
        private auth: AuthService,
        private authApi: AuthApiService,
        private recruiterJobsApi: RecruiterJobsApiService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.auth.currentUser$.subscribe(user => {
            this.user = user;
            this.cdr.detectChanges();
        });

        // Fetch full profile to get companyName + sync profilePictureUrl into AuthService
        // so the navbar avatar always reflects the latest picture
        this.authApi.getProfile().subscribe({
            next: p => {
                this.profile = p;
                // Merge fresh profilePictureUrl into the stored user so the header updates
                const current = this.auth.getUser();
                if (current && p.profilePictureUrl !== current.profilePictureUrl) {
                    this.auth.saveUser({ ...current, profilePictureUrl: p.profilePictureUrl });
                }
                this.cdr.detectChanges();
            },
            error: () => { /* non-critical */ }
        });

        this.recruiterJobsApi.getMyJobs(0, 100).subscribe({
            next: res => {
                this.jobs = res.content;
                this.counts.total = res.totalElements;
                this.counts.active = res.content.filter(j => j.status === 'ACTIVE').length;
                this.counts.closed = res.content.filter(j => j.status === 'CLOSED').length;
                this.counts.draft = res.content.filter(j => j.status === 'DRAFT').length;
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    get recentJobs(): Job[] { return this.jobs.slice(0, 4); }

    getInitials(name: string): string {
        return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }
}

