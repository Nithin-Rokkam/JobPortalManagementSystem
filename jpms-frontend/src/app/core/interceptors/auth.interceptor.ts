import { Injectable } from '@angular/core';
import {
    HttpInterceptor, HttpRequest, HttpHandler,
    HttpEvent, HttpErrorResponse, HttpClient
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    constructor(
        private authService: AuthService,
        private http: HttpClient
    ) { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = this.authService.getAccessToken();
        const authReq = token
            ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
            : req;

        return next.handle(authReq).pipe(
            catchError((error: HttpErrorResponse) => {
                if (error.status === 401 && !req.url.includes('/refresh')) {
                    const refreshToken = this.authService.getRefreshToken();
                    if (refreshToken) {
                        // Call refresh endpoint
                        return this.http.post<any>(`${environment.apiUrl}/api/auth/refresh`, { refreshToken }).pipe(
                            switchMap((res: any) => {
                                this.authService.saveTokens(res.accessToken, res.refreshToken);
                                const retryReq = req.clone({
                                    setHeaders: { Authorization: `Bearer ${res.accessToken}` }
                                });
                                return next.handle(retryReq);
                            }),
                            catchError(() => {
                                this.authService.logout();
                                return throwError(() => error);
                            })
                        );
                    }
                    this.authService.logout();
                }
                return throwError(() => error);
            })
        );
    }
}
