import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private baseUrl = `${environment.apiUrl}/team`;

  constructor(private http: HttpClient) {}

  private toFrontendMember(member: any): any {
    return {
      ...member,
      memberEmail: member.memberEmail ?? member.userEmail,
      equityShare: member.equityShare ?? member.equityPercentage,
    };
  }

  getTeamForStartup(startupId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/startup/${startupId}`).pipe(
      map(members => members.map(member => this.toFrontendMember(member)))
    );
  }

  inviteMember(data: any): Observable<any> {
    const normalizedRole = (data.role ?? '').toUpperCase();
    return this.http.post<any>(`${this.baseUrl}/invite`, {
      startupId: data.startupId,
      userEmail: data.userEmail ?? data.memberEmail,
      role: data.role,
      equityPercentage: data.equityPercentage ?? data.equityShare,
      permissionLevel: data.permissionLevel ?? (normalizedRole === 'COFOUNDER' ? 'ADMIN' : 'MEMBER'),
    }).pipe(map(member => this.toFrontendMember(member)));
  }

  removeMember(memberId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${memberId}`);
  }

  updateInviteStatus(id: number, status: string): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/invite/${id}/status`, { status }).pipe(
      map(member => this.toFrontendMember(member))
    );
  }

  getMyInvites(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/me`).pipe(
      map(members => members.map(member => this.toFrontendMember(member)))
    );
  }
}
