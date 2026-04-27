import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Startup, StartupDocument, StartupUpdate } from '../models/startup.model';

@Injectable({ providedIn: 'root' })
export class StartupService {
  private baseUrl = `${environment.apiUrl}/startups`;

  constructor(private http: HttpClient) {}

  private toFrontendStartup(startup: any): Startup {
    return {
      ...startup,
      targetAmount: startup.targetAmount ?? startup.fundingGoal,
      raisedAmount: startup.raisedAmount ?? startup.currentFunding ?? 0,
    };
  }

  private toBackendPayload(data: any): any {
    const payload = { ...data };
    if (payload.targetAmount !== undefined) {
      payload.fundingGoal = payload.targetAmount;
      delete payload.targetAmount;
    }
    delete payload.raisedAmount;
    delete payload.equityOffered;
    delete payload.websiteUrl;
    delete payload.logoUrl;
    delete payload.linkedinUrl;
    delete payload.twitterUrl;
    delete payload.founderEmail;
    return payload;
  }

  search(filters: any = {}, page = 0, size = 12): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    Object.keys(filters).forEach(k => { if (filters[k]) params = params.set(k, filters[k]); });
    const hasServerFilters = ['category', 'status', 'currentRound', 'stage'].some(k => !!filters[k]);
    const url = hasServerFilters ? `${this.baseUrl}/search` : this.baseUrl;
    return this.http.get<any>(url, { params }).pipe(
      map(pageResult => ({
        ...pageResult,
        content: (pageResult.content || []).map((s: any) => this.toFrontendStartup(s)),
      }))
    );
  }

  getById(id: number): Observable<Startup> {
    return this.http.get<any>(`${this.baseUrl}/${id}`).pipe(map(s => this.toFrontendStartup(s)));
  }

  create(data: any): Observable<Startup> {
    return this.http.post<any>(this.baseUrl, this.toBackendPayload(data)).pipe(map(s => this.toFrontendStartup(s)));
  }

  update(id: number, data: any): Observable<Startup> {
    return this.http.put<any>(`${this.baseUrl}/${id}`, this.toBackendPayload(data)).pipe(map(s => this.toFrontendStartup(s)));
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  getPending(): Observable<Startup[]> {
    return this.search({ status: 'PENDING' }, 0, 100).pipe(map(r => r.content || []));
  }

  approve(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/approve`, {});
  }

  reject(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/reject`, {});
  }

  follow(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/follow`, {});
  }

  unfollow(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}/unfollow`);
  }

  getFollowers(id: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/${id}/followers`);
  }

  getUpdates(id: number): Observable<StartupUpdate[]> {
    return this.http.get<StartupUpdate[]>(`${this.baseUrl}/${id}/updates`);
  }

  getDocuments(id: number): Observable<StartupDocument[]> {
    return this.http.get<StartupDocument[]>(`${this.baseUrl}/${id}/documents`);
  }

  addDocument(id: number, data: any): Observable<StartupDocument> {
    return this.http.post<StartupDocument>(`${this.baseUrl}/${id}/documents`, data);
  }

  postUpdate(id: number, data: any): Observable<StartupUpdate> {
    return this.http.post<StartupUpdate>(`${this.baseUrl}/${id}/updates`, data);
  }
}
