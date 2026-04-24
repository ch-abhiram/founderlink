import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { User, UserPreference } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private baseUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getUserProfile(email: string): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${email}`);
  }

  getPreferences(email: string): Observable<UserPreference> {
    return this.http.get<UserPreference>(`${this.baseUrl}/${email}/preferences`);
  }

  updateProfile(email: string, data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${email}`, data);
  }

  updatePreferences(email: string, data: Partial<UserPreference>): Observable<UserPreference> {
    return this.http.put<UserPreference>(`${this.baseUrl}/${email}/preferences`, data);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.baseUrl);
  }
}
