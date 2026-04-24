import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Conversation, Message } from '../models/messaging.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class MessagingService {
  private baseUrl = `${environment.apiUrl}/messages`;

  constructor(private http: HttpClient) {}

  sendMessage(data: any): Observable<Message> {
    return this.http.post<Message>(this.baseUrl, data);
  }

  getConversationMessages(id: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.baseUrl}/conversation/${id}`);
  }

  getStartupConversations(startupId: number): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.baseUrl}/startup/${startupId}`);
  }

  getMyConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.baseUrl}/my`);
  }
}
