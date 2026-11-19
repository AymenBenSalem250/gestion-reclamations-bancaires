import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, delay, of } from 'rxjs';

export interface AnalyticsDTO {
  total: number;
  ouvertes: number;
  cloturees: number;
  favorables: number;
  rejets: number;
  repartitionNature: { [key: string]: number };
  repartitionCanal: { [key: string]: number };
  repartitionObjet: { [key: string]: number };
  evolutionMensuelle: { month: string; total: number }[];
  details?: any[];
}

export interface XmlHistoryDTO {
  id: number;
  dateGeneration: string;
  annee: number;
  nomFichier: string;
  statut: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReportingService {
  private apiUrl = 'http://localhost:8081/api/reporting';

  constructor(private http: HttpClient) {}

  /**
   * Récupère les données analytiques pour une année donnée.
   * @param year L'année pour laquelle récupérer les statistiques
   */
  getAnalytics(year: number): Observable<AnalyticsDTO> {
    return this.http.get<AnalyticsDTO>(`${this.apiUrl}/analytics/${year}`);
  }

  /**
   * Génère le fichier XML pour l'année donnée.
   * @param year L'année de génération
   */
  generateXml(year: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/xml/${year}`, { responseType: 'blob' });
  }

  /**
   * [Mock] Récupère l'historique des générations XML 
   * (à adapter selon le vrai backend s'il existe une route spécifique)
   */
  getXmlHistory(): Observable<XmlHistoryDTO[]> {
    return of([
      { id: 1, dateGeneration: new Date().toISOString(), annee: new Date().getFullYear(), nomFichier: `reclamations_${new Date().getFullYear()}.xml`, statut: 'SUCCESS' },
      { id: 2, dateGeneration: new Date(Date.now() - 86400000).toISOString(), annee: new Date().getFullYear() - 1, nomFichier: `reclamations_${new Date().getFullYear() - 1}.xml`, statut: 'SUCCESS' }
    ]).pipe(delay(500));
  }
}
