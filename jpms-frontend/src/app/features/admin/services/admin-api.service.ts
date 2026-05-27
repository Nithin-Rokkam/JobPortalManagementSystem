import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PlatformReport, AuditLog } from '../../../shared/models/platform-report.model';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
    private base = `${environment.apiUrl}/api/admin`;

    constructor(private http: HttpClient) { }

    getUsers(): Observable<any[]> { return this.http.get<any[]>(`${this.base}/users`); }
    deleteUser(id: number): Observable<any> { return this.http.delete(`${this.base}/users/${id}`); }
    banUser(id: number): Observable<any> { return this.http.put(`${this.base}/users/${id}/ban`, {}); }
    unbanUser(id: number): Observable<any> { return this.http.put(`${this.base}/users/${id}/unban`, {}); }

    getJobs(): Observable<any[]> { return this.http.get<any[]>(`${this.base}/jobs`); }
    deleteJob(id: number): Observable<any> { return this.http.delete(`${this.base}/jobs/${id}`); }

    getReport(): Observable<PlatformReport> { return this.http.get<PlatformReport>(`${this.base}/reports`); }
    getAuditLogs(): Observable<AuditLog[]> { return this.http.get<AuditLog[]>(`${this.base}/audit-logs`); }
}
