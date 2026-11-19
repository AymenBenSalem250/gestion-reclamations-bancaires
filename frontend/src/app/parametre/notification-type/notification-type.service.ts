import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationType, Page } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class NotificationTypeService {
  private readonly base = 'http://localhost:8081/api/reference/notification-types';

  constructor(private http: HttpClient) {}

  getAll(page: number = 0, size: number = 10, search: string = ''): Observable<Page<NotificationType>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (search) params = params.set('search', search);
    return this.http.get<Page<NotificationType>>(this.base, { params });
  }
  create(data: Omit<NotificationType, 'id'>): Observable<NotificationType> { return this.http.post<NotificationType>(this.base, data); }
  update(id: number, data: NotificationType): Observable<NotificationType> { return this.http.put<NotificationType>(`${this.base}/${id}`, data); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.base}/${id}`); }
}
