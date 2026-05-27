import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Job, PagedResponse } from '../../../shared/models/job.model';

/**
 * Appends 'Z' to a date string that has no timezone suffix so the browser
 * treats it as UTC (matching the Docker container timezone) instead of
 * local time. Without this, a job posted "just now" in UTC would appear
 * as "5h ago" for a user in IST (UTC+5:30).
 */
function toUtc(dateStr: string | null | undefined): string {
    if (!dateStr) return dateStr as string;
    return dateStr.endsWith('Z') || dateStr.includes('+') ? dateStr : dateStr + 'Z';
}

function normalizeJob(job: Job): Job {
    return {
        ...job,
        createdAt: toUtc(job.createdAt),
        updatedAt: toUtc(job.updatedAt)
    };
}

@Injectable({ providedIn: 'root' })
export class JobsApiService {
    private base = `${environment.apiUrl}/api/jobs`;

    constructor(private http: HttpClient) { }

    getAll(page = 0, size = 10): Observable<PagedResponse<Job>> {
        return this.http.get<PagedResponse<Job>>(this.base, {
            params: new HttpParams().set('page', page).set('size', size)
        }).pipe(map(res => ({ ...res, content: res.content.map(normalizeJob) })));
    }

    getById(id: number): Observable<Job> {
        return this.http.get<Job>(`${this.base}/${id}`).pipe(map(normalizeJob));
    }

    search(filters: {
        title?: string; location?: string; jobType?: string;
        experienceYears?: number; page?: number; size?: number
    }): Observable<PagedResponse<Job>> {
        let params = new HttpParams();
        Object.entries(filters).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== '') params = params.set(k, v.toString());
        });
        return this.http.get<PagedResponse<Job>>(`${this.base}/search`, { params })
            .pipe(map(res => ({ ...res, content: res.content.map(normalizeJob) })));
    }
}
