import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private baseUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  private toFrontendNotification(notification: any): Notification {
    return {
      ...notification,
      read: notification.read ?? notification.status === 'READ',
    };
  }

  getNotifications(unreadOnly = false): Observable<Notification[]> {
    const url = unreadOnly ? `${this.baseUrl}?unreadOnly=true` : this.baseUrl;
    return this.http.get<any[]>(url).pipe(
      map(items => items.map(n => this.toFrontendNotification(n)))
    );
  }

  markAsRead(id: number): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}/read`, {}).pipe(map(n => this.toFrontendNotification(n)));
  }

  markAllRead(): Observable<any> {
    return this.http.put(`${this.baseUrl}/read-all`, {});
  }

  markAllAsRead(): Observable<any> {
    return this.markAllRead();
  }
}
