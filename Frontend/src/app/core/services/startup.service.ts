import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Startup, PagedResponse, StartupUpdate, StartupDocument } from '../models/startup.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class StartupService {
  private baseUrl = `${environment.apiUrl}/startups`;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 10): Observable<PagedResponse<Startup>> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PagedResponse<Startup>>(this.baseUrl, { params });
  }

  search(filters: any, page = 0, size = 10): Observable<PagedResponse<Startup>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filters.category) params = params.set('category', filters.category);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.currentRound) params = params.set('currentRound', filters.currentRound);
    if (filters.stage) params = params.set('stage', filters.stage);
    return this.http.get<PagedResponse<Startup>>(`${this.baseUrl}/search`, { params });
  }

  getById(id: number): Observable<Startup> {
    return this.http.get<Startup>(`${this.baseUrl}/${id}`);
  }

  create(data: Partial<Startup>): Observable<Startup> {
    return this.http.post<Startup>(this.baseUrl, data);
  }

  update(id: number, data: Partial<Startup>): Observable<Startup> {
    return this.http.put<Startup>(`${this.baseUrl}/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
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

  postUpdate(id: number, update: Partial<StartupUpdate>): Observable<StartupUpdate> {
    return this.http.post<StartupUpdate>(`${this.baseUrl}/${id}/updates`, update);
  }

  getDocuments(id: number): Observable<StartupDocument[]> {
    return this.http.get<StartupDocument[]>(`${this.baseUrl}/${id}/documents`);
  }

  addDocument(id: number, doc: Partial<StartupDocument>): Observable<StartupDocument> {
    return this.http.post<StartupDocument>(`${this.baseUrl}/${id}/documents`, doc);
  }
}
