import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AdminApiService } from '../../services/admin-api.service';
import { PlatformReport } from '../../../../shared/models/platform-report.model';

@Component({
    standalone: false,
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
    report: PlatformReport | null = null;
    recentUsers: any[] = [];
    recentLogs: any[] = [];
    loading = true;

    chartData: any = null;
    chartOptions: any = {
        responsive: true,
        plugins: {
            legend: { position: 'bottom', labels: { color: '#8B99B5', font: { size: 12 } } }
        }
    };

    constructor(private adminApi: AdminApiService, private cdr: ChangeDetectorRef) { }

    ngOnInit(): void {
        this.adminApi.getReport().subscribe({
            next: r => {
                this.report = r;
                const s = r.applicationsByStatus;
                this.chartData = {
                    labels: ['Applied', 'Under Review', 'Shortlisted', 'Rejected'],
                    datasets: [{
                        data: [s.APPLIED, s.UNDER_REVIEW, s.SHORTLISTED, s.REJECTED],
                        backgroundColor: ['#00C2FF', '#FFB800', '#00E5A0', '#FF4560'],
                        borderWidth: 0
                    }]
                };
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => { this.loading = false; this.cdr.detectChanges(); }
        });

        this.adminApi.getUsers().subscribe({ next: u => { this.recentUsers = u.slice(0, 5); this.cdr.detectChanges(); } });
        this.adminApi.getAuditLogs().subscribe({ next: l => { this.recentLogs = l.slice(0, 5); this.cdr.detectChanges(); } });
    }

    get shortlistRate(): string {
        if (!this.report || !this.report.totalApplications) return '0%';
        return ((this.report.applicationsByStatus.SHORTLISTED / this.report.totalApplications) * 100).toFixed(1) + '%';
    }

    getInitials(name: string): string {
        return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }
}

