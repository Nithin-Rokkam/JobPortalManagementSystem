import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ApplicationsApiService } from '../../services/applications-api.service';
import { Application, ApplicationStatus } from '../../../../shared/models/application.model';

@Component({
    standalone: false,
    selector: 'app-my-applications',
    templateUrl: './my-applications.component.html',
    styleUrls: ['./my-applications.component.css']
})
export class MyApplicationsComponent implements OnInit {
    applications: Application[] = [];
    loading = true;
    activeTab: ApplicationStatus | 'ALL' = 'ALL';
    expandedId: number | null = null;

    tabs: { label: string; value: ApplicationStatus | 'ALL' }[] = [
        { label: 'All', value: 'ALL' },
        { label: 'Applied', value: 'APPLIED' },
        { label: 'Under Review', value: 'UNDER_REVIEW' },
        { label: 'Shortlisted', value: 'SHORTLISTED' },
        { label: 'Rejected', value: 'REJECTED' }
    ];

    constructor(private appsApi: ApplicationsApiService, private cdr: ChangeDetectorRef) { }

    ngOnInit(): void {
        this.appsApi.getMyApplications().subscribe({
            next: apps => { this.applications = apps; this.loading = false; this.cdr.detectChanges(); },
            error: () => { this.loading = false; this.cdr.detectChanges(); }
        });
    }

    get filtered(): Application[] {
        if (this.activeTab === 'ALL') return this.applications;
        return this.applications.filter(a => a.status === this.activeTab);
    }

    toggle(id: number): void {
        this.expandedId = this.expandedId === id ? null : id;
    }
}

