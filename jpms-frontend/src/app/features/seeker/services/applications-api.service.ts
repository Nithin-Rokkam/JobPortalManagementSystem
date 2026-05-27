import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Application } from '../../../shared/models/application.model';

function toUtc(dateStr: string | null | undefined): string {
    if (!dateStr) return dateStr as string;
    return dateStr.endsWith('Z') || dateStr.includes('+') ? dateStr : dateStr + 'Z';
}

function normalizeApp(app: Application): Application {
    return {
        ...app,
        appliedAt: toUtc(app.appliedAt),
        updatedAt: toUtc(app.updatedAt)
    };
}

@Injectable({ providedIn: 'root' })
export class ApplicationsApiService {
    private base = `${environment.apiUrl}/api/applications`;

    constructor(private http: HttpClient) { }

    apply(data: {
        jobId: number; coverLetter?: string; useExistingResume: boolean;
        existingResumeUrl?: string; resume?: File
    }): Observable<Application> {
        const form = new FormData();
        form.append('jobId', data.jobId.toString());
        form.append('useExistingResume', data.useExistingResume.toString());
        if (data.coverLetter) form.append('coverLetter', data.coverLetter);
        if (data.existingResumeUrl) form.append('existingResumeUrl', data.existingResumeUrl);
        if (data.resume) form.append('resume', data.resume);
        return this.http.post<Application>(this.base, form).pipe(map(normalizeApp));
    }

    getMyApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.base}/my-applications`)
            .pipe(map(apps => apps.map(normalizeApp)));
    }

    getApplicationById(id: number): Observable<Application> {
        return this.http.get<Application>(`${this.base}/${id}`).pipe(map(normalizeApp));
    }
}
