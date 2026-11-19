import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Reclamation,
  ReclamationStats,
  StatusChangeRequest,
  Page
} from './models';

@Injectable({
  providedIn: 'root'
})
export class ReclamationService {

  private readonly baseUrl = 'http://localhost:8081/api/reclamations';

  constructor(private http: HttpClient) {}

  // ✅ GET ALL (avec filtres optionnels)
  getAll(params?: { page?: number; size?: number; codSta?: string | null; search?: string | null }): Observable<Page<Reclamation>> {
    let httpParams = new HttpParams();

    if (params?.page !== undefined && params.page !== null) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined && params.size !== null) httpParams = httpParams.set('size', params.size.toString());
    if (params?.codSta && params.codSta !== 'ALL') httpParams = httpParams.set('codSta', params.codSta);
    if (params?.search) httpParams = httpParams.set('search', params.search);

    return this.http.get<Page<Reclamation>>(this.baseUrl, { params: httpParams });
  }

  // ✅ GET BY ID
  getById(refRec: string): Observable<Reclamation> {
    return this.http.get<Reclamation>(`${this.baseUrl}/${refRec}`);
  }

  // ✅ GET BY STATUS
  getByStatus(codSta: string): Observable<Reclamation[]> {
    return this.http.get<Reclamation[]>(`${this.baseUrl}/status/${codSta}`);
  }

  // ✅ GET STATISTICS
  getStatistics(): Observable<ReclamationStats> {
    return this.http.get<ReclamationStats>(`${this.baseUrl}/statistics`);
  }

  // ✅ CREATE
  create(data: any): Observable<Reclamation> {
    return this.http.post<Reclamation>(this.baseUrl, data);
  }

  // ✅ UPDATE
  update(refRec: string, data: any): Observable<Reclamation> {
    return this.http.put<Reclamation>(`${this.baseUrl}/${refRec}`, data);
  }

  // ✅ CHANGE STATUS
  changeStatus(refRec: string, data: StatusChangeRequest): Observable<Reclamation> {
    return this.http.put<Reclamation>(`${this.baseUrl}/${refRec}/status`, data);
  }

  // ✅ ADD RESPONSE
  addResponse(refRec: string, repRec: string): Observable<Reclamation> {
    return this.http.put<Reclamation>(
      `${this.baseUrl}/${refRec}/response`,
      { repRec }
    );
  }

  // ✅ DELETE
  delete(refRec: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${refRec}`);
  }
}