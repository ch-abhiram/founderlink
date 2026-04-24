import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { TeamMember } from '../models/team.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private baseUrl = `${environment.apiUrl}/team`;

  constructor(private http: HttpClient) {}

  inviteMember(data: any): Observable<TeamMember> {
    return this.http.post<TeamMember>(`${this.baseUrl}/invite`, data);
  }

  updateInviteStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/invite/${id}/status`, { status });
  }

  getTeamForStartup(startupId: number): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.baseUrl}/startup/${startupId}`);
  }

  getMyTeamInvitesMap(): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.baseUrl}/me`);
  }

  removeMember(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
