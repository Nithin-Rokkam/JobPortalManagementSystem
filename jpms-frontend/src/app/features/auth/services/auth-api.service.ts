import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
    private base = `${environment.apiUrl}/api/auth`;

    constructor(private http: HttpClient) { }

    register(data: { name: string; email: string; password: string; phone?: string; role: string }): Observable<any> {
        return this.http.post(`${this.base}/register`, data);
    }

    login(data: { email: string; password: string }): Observable<any> {
        return this.http.post(`${this.base}/login`, data);
    }

    refresh(refreshToken: string): Observable<any> {
        return this.http.post(`${this.base}/refresh`, { refreshToken });
    }

    logout(refreshToken: string): Observable<any> {
        return this.http.post(`${this.base}/logout`, { refreshToken });
    }

    getProfile(): Observable<any> {
        return this.http.get(`${this.base}/profile`);
    }

    uploadProfilePicture(file: File): Observable<any> {
        const form = new FormData();
        form.append('picture', file);
        return this.http.put(`${this.base}/profile/picture`, form);
    }

    uploadResume(file: File): Observable<any> {
        const form = new FormData();
        form.append('resume', file);
        return this.http.put(`${this.base}/profile/resume`, form);
    }

    forgotPassword(email: string): Observable<any> {
        return this.http.post(`${this.base}/forgot-password`, { email });
    }

    resetPassword(email: string, otp: string, newPassword: string): Observable<any> {
        return this.http.post(`${this.base}/reset-password`, { email, otp, newPassword });
    }

    verifyRegistrationOtp(email: string, otp: string): Observable<any> {
        return this.http.post(`${this.base}/verify-registration`, { email, otp });
    }

    resendRegistrationOtp(email: string): Observable<any> {
        return this.http.post(`${this.base}/resend-registration-otp`, { email });
    }

    updateCompanyName(companyName: string): Observable<any> {
        return this.http.put(`${this.base}/profile/company`, { companyName });
    }
}
