import { Component, OnInit } from '@angular/core';
import { AdminApiService } from '../../services/admin-api.service';
import { PlatformReport } from '../../../../shared/models/platform-report.model';

@Component({
    standalone: false,
    selector: 'app-platform-report',
    templateUrl: './platform-report.component.html',
    styleUrls: ['./platform-report.component.css']
})
export class PlatformReportComponent implements OnInit {
    report: PlatformReport | null = null;
    loading = true;

    barData: any = null;
    barOptions: any = {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
            x: { ticks: { color: '#8B99B5' }, grid: { color: 'rgba(255,255,255,0.04)' } },
            y: { ticks: { color: '#8B99B5' }, grid: { color: 'rgba(255,255,255,0.04)' } }
        }
    };

    constructor(private adminApi: AdminApiService) { }

    ngOnInit(): void {
        this.adminApi.getReport().subscribe({
            next: r => {
                this.report = r;
                const s = r.applicationsByStatus;
                this.barData = {
                    labels: ['Applied', 'Under Review', 'Shortlisted', 'Rejected'],
                    datasets: [{
                        label: 'Applications',
                        data: [s.APPLIED, s.UNDER_REVIEW, s.SHORTLISTED, s.REJECTED],
                        backgroundColor: ['#00C2FF', '#FFB800', '#00E5A0', '#FF4560'],
                        borderRadius: 6
                    }]
                };
                this.loading = false;
            },
            error: () => { this.loading = false; }
        });
    }

    get shortlistRate(): string {
        if (!this.report?.totalApplications) return '0%';
        return ((this.report.applicationsByStatus.SHORTLISTED / this.report.totalApplications) * 100).toFixed(1) + '%';
    }

    get reviewConversion(): string {
        const ur = this.report?.applicationsByStatus.UNDER_REVIEW;
        const sl = this.report?.applicationsByStatus.SHORTLISTED;
        if (!ur || !sl) return '0%';
        return ((sl / ur) * 100).toFixed(1) + '%';
    }

    print(): void { window.print(); }
}

