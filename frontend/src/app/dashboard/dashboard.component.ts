import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ViewChild,
  ElementRef,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Subject, takeUntil, catchError, of } from 'rxjs';
import { Chart, registerables } from 'chart.js';

import { AuthService } from '../core/auth.service';
import {
  SlaDashboardService,
  SlaDashboardDTO,
  SlaUrgenceDTO
} from '../core/sla-dashboard.service';

import { PageHeaderComponent } from '../shared/page-header/page-header.component';

Chart.register(...registerables);

// ─────────────────────────────────────────────────────────────────────────────
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, HttpClientModule, PageHeaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit, OnDestroy, AfterViewInit {

  // ── Services (inject pattern Angular 17)
  private auth = inject(AuthService);
  private slaService = inject(SlaDashboardService);
  private cdr = inject(ChangeDetectorRef);

  // ── Canvas refs pour Chart.js
  @ViewChild('gaugeCanvas') gaugeCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('donutCanvas') donutCanvas!: ElementRef<HTMLCanvasElement>;

  // ── State
  isAdmin     = false;
  isLoading   = true;
  hasError    = false;
  data?: SlaDashboardDTO;

  private destroy$ = new Subject<void>();
  private gaugeChart: Chart | null = null;
  private donutChart: Chart | null = null;

  // ─────────────────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.isAdmin = this.auth.hasRole('ROLE_ADMIN');
    this.loadDashboard();
  }

  ngAfterViewInit(): void {
    // Les graphiques seront initialisés après que les données soient chargées
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.gaugeChart?.destroy();
    this.donutChart?.destroy();
  }

  // ─────────────────────────────────────────────────────────────────────────
  loadDashboard(): void {
    this.isLoading = true;
    this.hasError  = false;

    // Tenter l'API réelle, sinon fallback sur le mock
    this.slaService.getDashboard().pipe(
      takeUntil(this.destroy$),
      catchError(() => this.slaService.getMockDashboard())
    ).subscribe({
      next: (dto) => {
        this.data      = dto;
        this.isLoading = false;
        this.cdr.detectChanges();
        // Initialiser les charts après le rendu DOM
        setTimeout(() => this.initCharts(), 50);
      },
      error: () => {
        this.hasError  = true;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ─────────────────────────────────────────────────────────────────────────
  private initCharts(): void {
    this.buildGaugeChart();
    this.buildDonutChart();
  }

  // ── Gauge circulaire (demi-cercle)
  private buildGaugeChart(): void {
    if (!this.gaugeCanvas?.nativeElement || !this.data) return;
    this.gaugeChart?.destroy();

    const value  = this.data?.respectPercentage ?? 0;
    const remain = Math.max(0, 100 - value);
    const color  = this.getSlaColor(value);

    const isDark = document.body.classList.contains('dark-mode');
    const gaugeBg = isDark ? '#334155' : '#f1f5f9';

    this.gaugeChart = new Chart(this.gaugeCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        datasets: [{
          data: [value, remain],
          backgroundColor: [color, gaugeBg],
          borderWidth: 0,
          circumference: 180,
          rotation: 270,
        }]
      },
      options: {
        cutout: '78%',
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend:  { display: false },
          tooltip: { enabled: false },
        },
        animation: {
          animateRotate: true,
          duration: 1200,
          easing: 'easeInOutQuart',
        }
      }
    });
  }

  // ── Donut chart
  private buildDonutChart(): void {
    if (!this.donutCanvas?.nativeElement || !this.data) return;
    this.donutChart?.destroy();

    const respectCount = this.data?.respectSlaCount ?? 0;
    const exceededCount = this.data?.exceededCount ?? 0;

    const labels = ['SLA Respecté', 'SLA Dépassé'];
    const values = [respectCount, exceededCount];
    const colors = ['#16a34a', '#dc2626']; // Vert et Rouge

    const isDark = document.body.classList.contains('dark-mode');
    const donutBorder = isDark ? '#1e293b' : '#ffffff';
    const legendColor = isDark ? '#cbd5e1' : '#64748b';
    const tooltipBg = isDark ? '#0f172a' : '#1F2A3C';

    this.donutChart = new Chart(this.donutCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: values,
          backgroundColor: colors,
          borderColor: donutBorder,
          borderWidth: 3,
          hoverBorderWidth: 4,
          hoverOffset: 8,
        }]
      },
      options: {
        cutout: '68%',
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            position: 'bottom',
            labels: {
              font: { size: 12, family: "'Inter', sans-serif", weight: 'bold' },
              color: legendColor,
              padding: 16,
              usePointStyle: true,
              pointStyleWidth: 10,
            }
          },
          tooltip: {
            backgroundColor: tooltipBg,
            titleColor: '#ffffff',
            bodyColor: 'rgba(255,255,255,0.8)',
            padding: 12,
            cornerRadius: 10,
            callbacks: {
              label: (ctx) => ` ${ctx.label} : ${ctx.parsed} réclamations`
            }
          },
        },
        animation: {
          animateRotate: true,
          duration: 1000,
          easing: 'easeInOutQuart',
        }
      }
    });
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Helpers & Formatter
  // ─────────────────────────────────────────────────────────────────────────

  get safeTotal(): number {
    return this.data?.total ?? 0;
  }

  get safeEnCours(): number {
    return this.data?.enCours ?? 0;
  }

  get safeRespectPercentage(): number {
    return this.data?.respectPercentage ?? 0;
  }

  get safeRisqueMoyenGlobal(): number {
    return this.data?.risqueMoyenGlobal ?? 0;
  }

  get safeUrgences(): SlaUrgenceDTO[] {
    return this.data?.urgences ?? [];
  }

  // Permet d'itérer sur un objet map via *ngFor
  get serviceRankingsAsArray(): { service: string; score: number }[] {
    const performances = this.data?.performanceParService ?? {};
    return Object.entries(performances)
      .map(([service, score]) => ({ service, score }))
      .sort((a, b) => b.score - a.score); // Trier par performance décroissante
  }

  getSlaColor(value: number): string {
    if (value >= 80) return '#16a34a'; // GREEN
    if (value >= 60) return '#f59e0b'; // ORANGE
    return '#dc2626'; // RED
  }

  getRiskColor(score: number): string {
    if (score < 40)  return '#16a34a'; // Faible risque = GREEN
    if (score < 70)  return '#f59e0b'; // Risque modéré = ORANGE
    return '#dc2626'; // Haut risque = RED
  }

  getStatusBadgeClass(status: 'GREEN' | 'ORANGE' | 'RED' | string): string {
    if (status === 'GREEN') return 'badge-green';
    if (status === 'ORANGE') return 'badge-orange';
    if (status === 'RED') return 'badge-red';
    return 'badge-default';
  }

  getSlaStatusClass(status: 'GREEN' | 'ORANGE' | 'RED' | string): string {
    if (status === 'GREEN') return 'sla-green';
    if (status === 'ORANGE') return 'sla-orange';
    if (status === 'RED') return 'sla-red';
    return 'sla-default';
  }

  getScoreBarWidth(score: number): string {
    return `${Math.max(0, Math.min(100, score))}%`;
  }

  getScoreBarColor(score: number): string {
    return this.getSlaColor(score);
  }

  getDaysLabel(days: number): string {
    if (days <= 0)  return 'Expiré';
    if (days === 1) return '1 jour restant';
    return `${days} jours restants`;
  }

  getUrgencyLabel(status: string): string {
    if (status === 'RED') return 'CRITIQUE';
    if (status === 'ORANGE') return 'ATTENTION';
    return 'NORMAL';
  }

  trackByRef(_: number, item: SlaUrgenceDTO): string {
    return item.refRec;
  }

  trackByService(_: number, item: { service: string; score: number }): string {
    return item.service;
  }

  // KPI Cards configuration dynamique (sans trends car backend ne les a pas)
  get kpiCards() {
    return [
      {
        id: 'total',
        label: 'Total Réclamations',
        value: this.safeTotal.toLocaleString('fr-FR'),
        icon: 'clipboard',
        colorClass: 'blue',
        sublabel: 'réclamations gérées'
      },
      {
        id: 'encours',
        label: 'En Cours',
        value: this.safeEnCours.toLocaleString('fr-FR'),
        icon: 'clock',
        colorClass: 'orange',
        sublabel: 'réclamations actives'
      },
      {
        id: 'sla',
        label: 'Respect SLA',
        value: this.safeRespectPercentage + '%',
        icon: 'shield',
        colorClass: this.safeRespectPercentage >= 80 ? 'green' : this.safeRespectPercentage >= 60 ? 'orange' : 'red',
        sublabel: 'conformité délais'
      },
      {
        id: 'risk',
        label: 'Risque Global IA',
        value: this.safeRisqueMoyenGlobal + '/100',
        icon: 'brain',
        colorClass: this.safeRisqueMoyenGlobal < 40 ? 'green' : this.safeRisqueMoyenGlobal < 70 ? 'orange' : 'red',
        sublabel: 'score IA prédictif'
      },
    ];
  }
}
