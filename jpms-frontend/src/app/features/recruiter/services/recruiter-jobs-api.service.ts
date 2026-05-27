import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Job, PagedResponse } from '../../../shared/models/job.model';

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
export class RecruiterJobsApiService {
    private base = `${environment.apiUrl}/api/jobs`;

    constructor(private http: HttpClient) { }

    getMyJobs(page = 0, size = 10): Observable<PagedResponse<Job>> {
        return this.http.get<PagedResponse<Job>>(`${this.base}/my-jobs`, {
            params: new HttpParams().set('page', page).set('size', size)
        }).pipe(map(res => ({ ...res, content: res.content.map(normalizeJob) })));
    }

    postJob(data: any): Observable<Job> {
        return this.http.post<Job>(this.base, data).pipe(map(normalizeJob));
    }

    updateJob(id: number, data: Partial<Job>): Observable<Job> {
        return this.http.put<Job>(`${this.base}/${id}`, data).pipe(map(normalizeJob));
    }

    deleteJob(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/${id}`);
    }
}
