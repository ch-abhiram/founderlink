import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Conversation, Message } from '../models/messaging.model';

@Injectable({ providedIn: 'root' })
export class MessagingService {
  private baseUrl = `${environment.apiUrl}/messages`;

  constructor(private http: HttpClient) {}

  getMyConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.baseUrl}/my`);
  }

  getConversationMessages(conversationId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.baseUrl}/conversation/${conversationId}`);
  }

  sendMessage(data: { startupId: number | undefined, content: string, participantEmail?: string }): Observable<Message> {
    return this.http.post<Message>(this.baseUrl, data);
  }
}
