import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NatureReclamant, Page } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class NatureClientService {
  private readonly base = 'http://localhost:8081/api/reference/nature-reclamants';

  constructor(private http: HttpClient) {}

  getAll(page: number = 0, size: number = 10, search: string = ''): Observable<Page<NatureReclamant>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (search) params = params.set('search', search);
    return this.http.get<Page<NatureReclamant>>(this.base, { params });
  }

  create(data: Omit<NatureReclamant, 'id'>): Observable<NatureReclamant> {
    return this.http.post<NatureReclamant>(this.base, data);
  }

  update(id: number, data: NatureReclamant): Observable<NatureReclamant> {
    return this.http.put<NatureReclamant>(`${this.base}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
