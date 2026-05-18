import { Injectable } from '@angular/core';
import {
  HttpRequest, HttpHandler, HttpEvent,
  HttpInterceptor, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private router: Router, private auth: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {

        // Never auto-logout or auto-redirect for auth endpoints.
        // Login/register calls can legitimately return 401/403 and those
        // errors must reach the component's own error handler.
        const isAuthEndpoint = req.url.includes('/api/auth/');
        if (isAuthEndpoint) {
          return throwError(() => err);
        }

        if (err.status === 401) {
          // Token expired or invalid on a protected route — clear session
          this.auth.logout();
        }

        // Re-throw so individual components can also handle the error
        return throwError(() => err);
      })
    );
  }
}
