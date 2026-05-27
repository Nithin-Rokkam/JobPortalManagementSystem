import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RecruiterAppsApiService {
    private base = `${environment.apiUrl}/api/applications`;

    constructor(private http: HttpClient) { }

    getApplicantsForJob(jobId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.base}/job/${jobId}`);
    }

    updateApplicationStatus(applicationId: number, newStatus: string): Observable<any> {
        return this.http.patch(`${this.base}/${applicationId}/status`, { newStatus });
    }

    deleteApplication(applicationId: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/${applicationId}`);
    }
}
