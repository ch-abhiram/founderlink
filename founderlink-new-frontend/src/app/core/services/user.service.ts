import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {
  private baseUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  private getCurrentEmail(): string {
    const email = localStorage.getItem('founderlink_email');
    if (!email) throw new Error('No authenticated user email found');
    return email;
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.baseUrl}/${encodeURIComponent(this.getCurrentEmail())}`);
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${encodeURIComponent(this.getCurrentEmail())}`, data);
  }

  getPreferences(): Observable<any> {
    return this.http.get(`${this.baseUrl}/${encodeURIComponent(this.getCurrentEmail())}/preferences`);
  }

  updatePreferences(data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${encodeURIComponent(this.getCurrentEmail())}/preferences`, data);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  changeRole(email: string, role: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${encodeURIComponent(email)}/role`, { role });
  }
}
