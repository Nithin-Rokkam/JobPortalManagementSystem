import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Application } from '../../../shared/models/application.model';

@Injectable({ providedIn: 'root' })
export class ApplicationsApiService {
    private base = `${environment.apiUrl}/api/applications`;

    constructor(private http: HttpClient) { }

    apply(data: { jobId: number; coverLetter?: string; useExistingResume: boolean; existingResumeUrl?: string; resume?: File }): Observable<Application> {
        const form = new FormData();
        form.append('jobId', data.jobId.toString());
        form.append('useExistingResume', data.useExistingResume.toString());
        if (data.coverLetter) form.append('coverLetter', data.coverLetter);
        if (data.existingResumeUrl) form.append('existingResumeUrl', data.existingResumeUrl);
        if (data.resume) form.append('resume', data.resume);
        return this.http.post<Application>(this.base, form);
    }

    getMyApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.base}/my-applications`);
    }

    getApplicationById(id: number): Observable<Application> {
        return this.http.get<Application>(`${this.base}/${id}`);
    }
}
