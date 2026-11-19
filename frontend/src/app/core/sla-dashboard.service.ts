import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';

// ─────────────────────────────────────────────────────────────────────────────
// Models — SLA Dashboard (Aligné Backend Spring Boot)
// ─────────────────────────────────────────────────────────────────────────────

export interface SlaUrgenceDTO {
  refRec: string;
  datRec: string | Date;
  datLimite: string | Date;
  joursRestants: number;
  slaStatus: 'GREEN' | 'ORANGE' | 'RED';
  riskScore: number;
  serviceLib: string;
}

export interface SlaDashboardDTO {
  total: number;
  enCours: number;
  cloturees: number;
  respectSlaCount: number;
  exceededCount: number;
  respectPercentage: number;
  risqueMoyenGlobal: number;
  slaDays: number;
  performanceParService: { [key: string]: number };
  urgences: SlaUrgenceDTO[];
}

// ─────────────────────────────────────────────────────────────────────────────
// Mock data — utilisé en fallback si l'API n'est pas disponible
// ─────────────────────────────────────────────────────────────────────────────
const MOCK_DATA: SlaDashboardDTO = {
  total: 1248,
  enCours: 187,
  cloturees: 800,
  respectSlaCount: 1023,
  exceededCount: 225,
  respectPercentage: 82,
  risqueMoyenGlobal: 34,
  slaDays: 7,
  performanceParService: {
    'Back Office': 94,
    'Service Client': 87,
    'Monétique': 74,
    'Crédit': 61,
    'Conformité': 43,
    'IT & Systèmes': 31
  },
  urgences: [
    { refRec: 'REC-2026-00412', datRec: '2026-06-20', datLimite: '2026-06-21', joursRestants: 1,  riskScore: 92, slaStatus: 'RED',    serviceLib: 'Monétique' },
    { refRec: 'REC-2026-00387', datRec: '2026-06-22', datLimite: '2026-06-24', joursRestants: 2,  riskScore: 87, slaStatus: 'RED',    serviceLib: 'Crédit' },
    { refRec: 'REC-2026-00355', datRec: '2026-06-25', datLimite: '2026-06-30', joursRestants: 5,  riskScore: 64, slaStatus: 'ORANGE', serviceLib: 'Service Client' },
    { refRec: 'REC-2026-00371', datRec: '2026-06-28', datLimite: '2026-07-07', joursRestants: 9,  riskScore: 42, slaStatus: 'GREEN',  serviceLib: 'IT & Systèmes' },
  ]
};

@Injectable({ providedIn: 'root' })
export class SlaDashboardService {
  private apiUrl = 'http://localhost:8081/api/reporting';

  constructor(private http: HttpClient) {}

  /**
   * Récupère les données du dashboard SLA.
   * En cas d'erreur réseau, renvoie les données mock.
   */
  getDashboard(): Observable<SlaDashboardDTO> {
    return this.http.get<SlaDashboardDTO>(`${this.apiUrl}/sla-dashboard`);
  }

  /** Mock fallback / dev */
  getMockDashboard(): Observable<SlaDashboardDTO> {
    return of(MOCK_DATA).pipe(delay(400));
  }
}
