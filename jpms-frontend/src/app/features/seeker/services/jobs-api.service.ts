import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Job, PagedResponse } from '../../../shared/models/job.model';

@Injectable({ providedIn: 'root' })
export class JobsApiService {
    private base = `${environment.apiUrl}/api/jobs`;

    constructor(private http: HttpClient) { }

    getAll(page = 0, size = 10): Observable<PagedResponse<Job>> {
        return this.http.get<PagedResponse<Job>>(this.base, {
            params: new HttpParams().set('page', page).set('size', size)
        });
    }

    getById(id: number): Observable<Job> {
        return this.http.get<Job>(`${this.base}/${id}`);
    }

    search(filters: { title?: string; location?: string; jobType?: string; experienceYears?: number; page?: number; size?: number }): Observable<PagedResponse<Job>> {
        let params = new HttpParams();
        Object.entries(filters).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== '') params = params.set(k, v.toString());
        });
        return this.http.get<PagedResponse<Job>>(`${this.base}/search`, { params });
    }
}
