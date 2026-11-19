import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Canal, Page } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class CanalReceptionService {
  private readonly base = 'http://localhost:8081/api/reference/canals';

  constructor(private http: HttpClient) {}

  getAll(page: number = 0, size: number = 10, search: string = ''): Observable<Page<Canal>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (search) params = params.set('search', search);
    return this.http.get<Page<Canal>>(this.base, { params });
  }
  create(data: Omit<Canal, 'id'>): Observable<Canal> { return this.http.post<Canal>(this.base, data); }
  update(id: number, data: Canal): Observable<Canal> { return this.http.put<Canal>(`${this.base}/${id}`, data); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.base}/${id}`); }
}
