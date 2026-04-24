import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private baseUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  getNotifications(unreadOnly: boolean = false): Observable<Notification[]> {
    let params = new HttpParams().set('unreadOnly', unreadOnly);
    return this.http.get<Notification[]>(this.baseUrl, { params });
  }

  markAsRead(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<any> {
    return this.http.put(`${this.baseUrl}/read-all`, {});
  }
}
