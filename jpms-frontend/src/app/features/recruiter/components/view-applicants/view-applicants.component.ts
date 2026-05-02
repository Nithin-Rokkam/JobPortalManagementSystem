import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { RecruiterAppsApiService } from '../../services/recruiter-apps-api.service';
import { JobsApiService } from '../../../seeker/services/jobs-api.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-view-applicants',
    templateUrl: './view-applicants.component.html',
    styleUrls: ['./view-applicants.component.css']
})
export class ViewApplicantsComponent implements OnInit {
    applicants: any[] = [];
    jobTitle = '';
    loading = true;
    jobId!: number;
    activeTab: string = 'ALL';
    selectedApplicant: any = null;
    openDropdownId: number | null = null;

    tabs = ['ALL', 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'SELECTED', 'REJECTED'];

    constructor(
        private route: ActivatedRoute,
        private recruiterAppsApi: RecruiterAppsApiService,
        private jobsApi: JobsApiService,
        private toast: ToastService,
        private cdr: ChangeDetectorRef,
        private sanitizer: DomSanitizer
    ) { }

    ngOnInit(): void {
        this.jobId = Number(this.route.snapshot.paramMap.get('jobId'));
        this.jobsApi.getById(this.jobId).subscribe({ next: j => { this.jobTitle = j.title; this.cdr.detectChanges(); } });
        this.recruiterAppsApi.getApplicantsForJob(this.jobId).subscribe({
            next: apps => { this.applicants = apps; this.loading = false; this.cdr.detectChanges(); },
            error: () => { this.loading = false; this.cdr.detectChanges(); }
        });
    }

    get filtered(): any[] {
        if (this.activeTab === 'ALL') return this.applicants;
        return this.applicants.filter(a => a.status === this.activeTab);
    }

    updateStatus(app: any, newStatus: string): void {
        const prev = app.status;
        app.status = newStatus;
        this.recruiterAppsApi.updateApplicationStatus(app.id, newStatus).subscribe({
            next: () => { 
                let msg = 'Status updated successfully';
                if (newStatus === 'SHORTLISTED') msg = 'Candidate shortlisted and notified via email';
                if (newStatus === 'SELECTED') msg = 'Candidate selected and notified via email';
                if (newStatus === 'REJECTED') msg = 'Candidate rejected and notified via email';
                this.toast.success(msg); 
                this.cdr.detectChanges(); 
            },
            error: () => { app.status = prev; this.toast.error('Failed to update status'); this.cdr.detectChanges(); }
        });
    }

    removeApplication(app: any, event: Event): void {
        event.stopPropagation();
        if (confirm('Are you sure you want to remove this application permanently?')) {
            this.recruiterAppsApi.deleteApplication(app.id).subscribe({
                next: () => {
                    this.applicants = this.applicants.filter(a => a.id !== app.id);
                    if (this.selectedApplicant?.id === app.id) this.selectedApplicant = null;
                    this.toast.success('Application removed successfully');
                    this.cdr.detectChanges();
                },
                error: () => this.toast.error('Failed to remove application')
            });
        }
    }

    getInitials(name: string): string {
        if (!name || name === 'Candidate') return 'C';
        return name.split(' ').filter(w => w).map(w => w[0]).join('').toUpperCase().slice(0, 2);
    }

    selectApplicant(app: any): void {
        this.selectedApplicant = app;
        if (app.resumeUrl && !app.safeResumeUrl) {
            app.safeResumeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(app.resumeUrl);
        }
    }

    closePanel(): void {
        this.selectedApplicant = null;
    }

    toggleDropdown(appId: number, event: Event): void {
        event.stopPropagation();
        this.openDropdownId = this.openDropdownId === appId ? null : appId;
    }

    closeDropdowns(): void {
        this.openDropdownId = null;
    }

    onStatusChange(app: any, newStatus: string, event: Event): void {
        event.stopPropagation();
        this.openDropdownId = null;
        if (app.status === newStatus) return;
        this.updateStatus(app, newStatus);
    }
}

