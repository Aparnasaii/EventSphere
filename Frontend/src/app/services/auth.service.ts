import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, switchMap, map, catchError } from 'rxjs/operators';
import {
  LoginRequest, AuthResponse,
  RegisterRequest, User,
  ForgotPasswordRequest, ResetPasswordRequest
} from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl      = 'http://localhost:8081/users/api';
  private currentUser$ = new BehaviorSubject<User | null>(this.getStoredUser());

  constructor(private http: HttpClient, private router: Router) {}

  // ── Login ─────────────────────────────────────────────────────
  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, req).pipe(
      // Step 1: store token & parse partial user from JWT payload
      tap(res => {
        sessionStorage.setItem('token', res.token);
        const partial = this.parseUserFromToken(res.token);
        sessionStorage.setItem('user', JSON.stringify(partial));
        this.currentUser$.next(partial);
      }),
      // Step 2: fetch full profile so fullName / contactNumber are available
      switchMap(res => {
        const partial = this.parseUserFromToken(res.token);
        if (!partial?.userId) return of(res);

        return this.http.get<any>(`${this.baseUrl}/users/${partial.userId}`).pipe(
          tap(profile => {
            const full: User = {
              userId:        partial.userId,
              email:         partial.email,
              role:          partial.role,
              fullName:      profile.fullName      ?? '',
              contactNumber: profile.contactNumber ?? '',
              status:        profile.status        ?? 'ACTIVE'
            };
            sessionStorage.setItem('user', JSON.stringify(full));
            this.currentUser$.next(full);
          }),
          map(() => res),
          catchError(() => of(res))  // profile fetch failure must not block login
        );
      })
    );
  }

  // ── Register ──────────────────────────────────────────────────
  register(req: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users/register`, req);
  }

  // ── Forgot Password ───────────────────────────────────────────
  forgotPassword(req: ForgotPasswordRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/forgot-password`, req,
      { responseType: 'text' });
  }

  // ── Reset Password ────────────────────────────────────────────
  resetPassword(req: ResetPasswordRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/auth/reset-password`, req,
      { responseType: 'text' });
  }

  // ── Logout ────────────────────────────────────────────────────
  logout(): void {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
    this.currentUser$.next(null);
    this.router.navigate(['/auth/login']);
  }

  // ── Helpers ───────────────────────────────────────────────────
  getToken(): string | null              { return sessionStorage.getItem('token'); }
  isLoggedIn(): boolean                  { return !!this.getToken(); }
  getCurrentUser(): User | null          { return this.currentUser$.value; }
  currentUser(): Observable<User | null> { return this.currentUser$.asObservable(); }

  getRole(): string {
    return this.getCurrentUser()?.role ?? '';
  }

  isAdmin():     boolean { return this.getRole() === 'ROLE_ADMIN'; }
  isOrganizer(): boolean { return this.getRole() === 'ROLE_ORGANIZER'; }
  isAttendee():  boolean { return this.getRole() === 'ROLE_ATTENDEE'; }

  private getStoredUser(): User | null {
    try {
      const u = sessionStorage.getItem('user');
      return u ? JSON.parse(u) : null;
    } catch { return null; }
  }

  /** Parse the lightweight partial user from JWT claims (no fullName in token). */
  private parseUserFromToken(token: string): any {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        userId: payload.userId,
        email:  payload.sub,
        role:   payload.role,
        fullName:      '',
        contactNumber: '',
        status:        'ACTIVE'
      };
    } catch { return null; }
  }
}
