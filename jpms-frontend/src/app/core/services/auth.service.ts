import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly TOKEN_KEY = 'joblix_access_token';
    private readonly REFRESH_KEY = 'joblix_refresh_token';
    private readonly USER_KEY = 'joblix_user';

    private currentUserSubject: BehaviorSubject<any>;
    public currentUser$: Observable<any>;

    constructor(private router: Router) {
        const savedUser = localStorage.getItem(this.USER_KEY);
        this.currentUserSubject = new BehaviorSubject<any>(savedUser ? JSON.parse(savedUser) : null);
        this.currentUser$ = this.currentUserSubject.asObservable();
    }

    saveTokens(accessToken: string, refreshToken: string): void {
        localStorage.setItem(this.TOKEN_KEY, accessToken);
        localStorage.setItem(this.REFRESH_KEY, refreshToken);
    }

    getAccessToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    getRefreshToken(): string | null {
        return localStorage.getItem(this.REFRESH_KEY);
    }

    saveUser(user: any): void {
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        this.currentUserSubject.next(user);
    }

    getUser(): any {
        return this.currentUserSubject.value;
    }

    getRole(): string | null {
        return this.getUser()?.role ?? null;
    }

    isLoggedIn(): boolean {
        return !!this.getAccessToken();
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.REFRESH_KEY);
        localStorage.removeItem(this.USER_KEY);
        this.currentUserSubject.next(null);
        this.router.navigate(['/auth/login']);
    }

    redirectByRole(): void {
        const role = this.getRole();
        if (role === 'JOB_SEEKER') this.router.navigate(['/seeker/dashboard']);
        else if (role === 'RECRUITER') this.router.navigate(['/recruiter/dashboard']);
        else if (role === 'ADMIN') this.router.navigate(['/admin/dashboard']);
        else this.router.navigate(['/auth/login']);
    }
}
