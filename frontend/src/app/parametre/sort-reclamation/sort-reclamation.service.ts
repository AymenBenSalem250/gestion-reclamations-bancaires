import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Statut, Page } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class SortReclamationService {
  private readonly base = 'http://localhost:8081/api/reference/statuts';

  constructor(private http: HttpClient) {}

  getAll(page: number = 0, size: number = 10, search: string = ''): Observable<Page<Statut>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (search) params = params.set('search', search);
    return this.http.get<Page<Statut>>(this.base, { params });
  }

  create(data: Statut): Observable<Statut> {
    return this.http.post<Statut>(this.base, data);
  }

  update(codSta: string, data: Statut): Observable<Statut> {
    return this.http.put<Statut>(`${this.base}/${codSta}`, data);
  }

  delete(codSta: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${codSta}`);
  }
}
