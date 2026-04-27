import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Investment } from '../models/investment.model';

@Injectable({ providedIn: 'root' })
export class InvestmentService {
  private baseUrl = `${environment.apiUrl}/investments`;

  constructor(private http: HttpClient) {}

  createInvestment(data: any): Observable<Investment> {
    return this.http.post<Investment>(this.baseUrl, data);
  }

  getMyInvestments(): Observable<Investment[]> {
    return this.http.get<Investment[]>(`${this.baseUrl}/me`);
  }

  getAllInvestments(): Observable<Investment[]> {
    return this.http.get<Investment[]>(this.baseUrl);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/status`, { status });
  }

  getInvestmentsForStartup(startupId: number): Observable<Investment[]> {
    return this.http.get<Investment[]>(`${this.baseUrl}/startup/${startupId}`);
  }
}
