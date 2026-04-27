import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoginResponse, RegisterResponse } from '../models/auth.model';
import { BehaviorSubject, Observable, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = `${environment.apiUrl}/auth`;
  private _isAuthenticated$ = new BehaviorSubject<boolean>(this.hasToken());
  private _role$ = new BehaviorSubject<string | null>(this.getRole());

  isAuthenticated$ = this._isAuthenticated$.asObservable();
  role$ = this._role$.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  private hasToken(): boolean { return !!localStorage.getItem('founderlink_access_token'); }

  register(data: any): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, data);
  }

  login(data: any): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, data).pipe(
      tap(res => this.setTokens(res))
    );
  }

  verifyOtp(email: string, otp: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/verify-otp`, { email, otp });
  }

  resendOtp(email: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/resend-otp`, { email });
  }

  refresh(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) { this.clearTokens(); return throwError(() => new Error('No refresh token')); }
    return this.http.post<LoginResponse>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      tap({ next: res => this.setTokens(res), error: () => this.clearTokens() })
    );
  }

  changePassword(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/change-password`, data);
  }

  logout(): void {
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe({ next: () => this.clearTokens(), error: () => this.clearTokens() });
  }

  private setTokens(res: LoginResponse): void {
    localStorage.setItem('founderlink_access_token', res.accessToken);
    localStorage.setItem('founderlink_refresh_token', res.refreshToken);
    localStorage.setItem('founderlink_email', res.email);
    localStorage.setItem('founderlink_role', res.role);
    this._isAuthenticated$.next(true);
    this._role$.next(res.role);
  }

  clearTokens(): void {
    ['founderlink_access_token','founderlink_refresh_token','founderlink_email','founderlink_role']
      .forEach(k => localStorage.removeItem(k));
    this._isAuthenticated$.next(false);
    this._role$.next(null);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null  { return localStorage.getItem('founderlink_access_token'); }
  getRefreshToken(): string | null { return localStorage.getItem('founderlink_refresh_token'); }
  getEmail(): string | null        { return localStorage.getItem('founderlink_email'); }
  getRole(): string | null         { return localStorage.getItem('founderlink_role'); }
  isAuthenticated(): boolean       { return this.hasToken(); }
}
