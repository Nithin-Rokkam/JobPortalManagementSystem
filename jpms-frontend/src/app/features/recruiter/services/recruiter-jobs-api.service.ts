import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Job, PagedResponse } from '../../../shared/models/job.model';

@Injectable({ providedIn: 'root' })
export class RecruiterJobsApiService {
    private base = `${environment.apiUrl}/api/jobs`;

    constructor(private http: HttpClient) { }

    getMyJobs(page = 0, size = 10): Observable<PagedResponse<Job>> {
        return this.http.get<PagedResponse<Job>>(`${this.base}/my-jobs`, {
            params: new HttpParams().set('page', page).set('size', size)
        });
    }

    postJob(data: any): Observable<Job> {
        return this.http.post<Job>(this.base, data);
    }

    updateJob(id: number, data: Partial<Job>): Observable<Job> {
        return this.http.put<Job>(`${this.base}/${id}`, data);
    }

    deleteJob(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/${id}`);
    }
}
