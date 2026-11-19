import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Statut, Service, Objet, Canal, NatureReclamant, NotificationType, Page } from './models';

@Injectable({ providedIn: 'root' })
export class ReferenceService {
  private readonly base = 'http://localhost:8081/api/reference';

  constructor(private http: HttpClient) {}

  // ── Statuts ────────────────────────────────────────────────────────────────
  getStatuts(page: number = 0, size: number = 1000): Observable<Page<Statut>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<Statut>>(`${this.base}/statuts`, { params });
  }
  createStatut(data: Statut): Observable<Statut>             { return this.http.post<Statut>(`${this.base}/statuts`, data); }
  updateStatut(cod: string, data: Statut): Observable<Statut>{ return this.http.put<Statut>(`${this.base}/statuts/${cod}`, data); }
  deleteStatut(cod: string): Observable<void>                { return this.http.delete<void>(`${this.base}/statuts/${cod}`); }

  // ── Services ───────────────────────────────────────────────────────────────
  getServices(page: number = 0, size: number = 1000): Observable<Page<Service>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<Service>>(`${this.base}/services`, { params });
  }
  createService(data: Service): Observable<Service>             { return this.http.post<Service>(`${this.base}/services`, data); }
  updateService(cod: string, data: Service): Observable<Service>{ return this.http.put<Service>(`${this.base}/services/${cod}`, data); }
  deleteService(cod: string): Observable<void>                  { return this.http.delete<void>(`${this.base}/services/${cod}`); }

  // ── Objets ─────────────────────────────────────────────────────────────────
  getObjets(page: number = 0, size: number = 1000): Observable<Page<Objet>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<Objet>>(`${this.base}/objets`, { params });
  }
  createObjet(data: Omit<Objet,'id'>): Observable<Objet>    { return this.http.post<Objet>(`${this.base}/objets`, data); }
  updateObjet(id: number, data: Objet): Observable<Objet>   { return this.http.put<Objet>(`${this.base}/objets/${id}`, data); }
  deleteObjet(id: number): Observable<void>                 { return this.http.delete<void>(`${this.base}/objets/${id}`); }

  // ── Canals ─────────────────────────────────────────────────────────────────
  getCanals(page: number = 0, size: number = 1000): Observable<Page<Canal>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<Canal>>(`${this.base}/canals`, { params });
  }
  createCanal(data: Omit<Canal,'id'>): Observable<Canal>    { return this.http.post<Canal>(`${this.base}/canals`, data); }
  updateCanal(id: number, data: Canal): Observable<Canal>   { return this.http.put<Canal>(`${this.base}/canals/${id}`, data); }
  deleteCanal(id: number): Observable<void>                 { return this.http.delete<void>(`${this.base}/canals/${id}`); }

  // ── NatureReclamants ───────────────────────────────────────────────────────
  getNatures(page: number = 0, size: number = 1000): Observable<Page<NatureReclamant>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<NatureReclamant>>(`${this.base}/nature-reclamants`, { params });
  }
  createNature(data: Omit<NatureReclamant,'id'>): Observable<NatureReclamant>{ return this.http.post<NatureReclamant>(`${this.base}/nature-reclamants`, data); }
  updateNature(id: number, data: NatureReclamant): Observable<NatureReclamant>{ return this.http.put<NatureReclamant>(`${this.base}/nature-reclamants/${id}`, data); }
  deleteNature(id: number): Observable<void>                               { return this.http.delete<void>(`${this.base}/nature-reclamants/${id}`); }

  // ── NotificationTypes ───────────────────────────────────────────────────────
  getNotificationTypes(page: number = 0, size: number = 1000): Observable<Page<NotificationType>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<NotificationType>>(`${this.base}/notification-types`, { params });
  }
  createNotificationType(data: Omit<NotificationType,'id'>): Observable<NotificationType>{ return this.http.post<NotificationType>(`${this.base}/notification-types`, data); }
  updateNotificationType(id: number, data: NotificationType): Observable<NotificationType>{ return this.http.put<NotificationType>(`${this.base}/notification-types/${id}`, data); }
  deleteNotificationType(id: number): Observable<void>                               { return this.http.delete<void>(`${this.base}/notification-types/${id}`); }
}
