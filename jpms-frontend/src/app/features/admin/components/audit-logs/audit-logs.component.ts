import { Component, OnInit } from '@angular/core';
import { AdminApiService } from '../../services/admin-api.service';
import { AuditLog } from '../../../../shared/models/platform-report.model';

@Component({
    standalone: false,
    selector: 'app-audit-logs',
    templateUrl: './audit-logs.component.html',
    styleUrls: ['./audit-logs.component.css']
})
export class AuditLogsComponent implements OnInit {
    logs: AuditLog[] = [];
    loading = true;
    search = '';

    constructor(private adminApi: AdminApiService) { }

    ngOnInit(): void {
        this.adminApi.getAuditLogs().subscribe({
            next: l => {
                this.logs = l.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
                this.loading = false;
            },
            error: () => { this.loading = false; }
        });
    }

    get filtered(): AuditLog[] {
        if (!this.search) return this.logs;
        return this.logs.filter(l => l.action?.toLowerCase().includes(this.search.toLowerCase()) || l.details?.toLowerCase().includes(this.search.toLowerCase()));
    }
}

