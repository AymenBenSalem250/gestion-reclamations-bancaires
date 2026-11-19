import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Objet, Service, Page } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class ObjetReclamationService {

  private readonly base = 'http://localhost:8081/api/reference';

  constructor(private http: HttpClient) {}

  getAll(page: number = 0, size: number = 10, search: string = ''): Observable<Page<Objet>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (search) params = params.set('search', search);

    return this.http.get<Page<Objet>>(`${this.base}/objets`, { params });
  }

  getServices(): Observable<Service[]> {
    return this.http
      .get<Page<Service>>(`${this.base}/services?page=0&size=1000`)
      .pipe(map(res => res.content));
  }

  create(data: Omit<Objet, 'id'>): Observable<Objet> {
    return this.http.post<Objet>(`${this.base}/objets`, data);
  }

  update(id: number, data: Objet): Observable<Objet> {
    return this.http.put<Objet>(`${this.base}/objets/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/objets/${id}`);
  }
}