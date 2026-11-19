import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartModule } from 'primeng/chart';
import { ReportingService, AnalyticsDTO } from '../reporting.service';

import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-etat',
  standalone: true,
  imports: [CommonModule, FormsModule, ChartModule, PageHeaderComponent],
  templateUrl: './etat.component.html',
  styleUrls: ['./etat.component.css']
})
export class EtatComponent implements OnInit {
  reportingService = inject(ReportingService);

  annee: number = new Date().getFullYear();
  annees: number[] = [];

  loading: boolean = false;
  error: string | null = null;
  analytics: AnalyticsDTO | null = null;

  // Chart data
  natureChartData: any;
  canalChartData: any;
  objetChartData: any;
  evolutionChartData: any;

  chartOptions: any;
  barChartOptions: any;

  ngOnInit() {
    this.initAnnees();
    this.initChartOptions();
    this.loadData();
  }

  initAnnees() {
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 5; i++) {
      this.annees.push(currentYear - i);
    }
  }

  initChartOptions() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-primary').trim() || '#1F2A3C';
    const textColorSecondary = documentStyle.getPropertyValue('--text-secondary').trim() || '#64748B';
    const surfaceBorder = documentStyle.getPropertyValue('--border-color').trim() || '#E2E8F0';

    this.chartOptions = {
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: { color: textColor, usePointStyle: true }
        }
      }
    };

    this.barChartOptions = {
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          ticks: { color: textColorSecondary },
          grid: { color: surfaceBorder, drawBorder: false }
        },
        y: {
          beginAtZero: true,
          ticks: { color: textColorSecondary, stepSize: 1 },
          grid: { color: surfaceBorder, drawBorder: false }
        }
      }
    };
  }

  loadData() {
    this.loading = true;
    this.error = null;
    
    this.reportingService.getAnalytics(this.annee).subscribe({
      next: (data) => {
        this.analytics = data;
        this.prepareCharts(data);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des statistiques.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  prepareCharts(data: AnalyticsDTO) {

  const documentStyle = getComputedStyle(document.documentElement);
  
  const primary = documentStyle.getPropertyValue('--primary-color').trim() || '#2563EB';
  const accent = documentStyle.getPropertyValue('--accent-color').trim() || '#E63946';
  const success = documentStyle.getPropertyValue('--success-color').trim() || '#21A366';
  const colors = [primary, accent, success, '#F59E0B', '#8B5CF6', '#EC4899', '#14B8A6'];

  // ✅ Repartition Nature
  if (data.repartitionNature) {
    const entries = Object.entries(data.repartitionNature);
    this.natureChartData = {
      labels: entries.map(([key]) => key),
      datasets: [{
        data: entries.map(([_, value]) => value),
        backgroundColor: colors,
        hoverOffset: 4
      }]
    };
  }

  // ✅ Repartition Canal
  if (data.repartitionCanal) {
    const entries = Object.entries(data.repartitionCanal);
    this.canalChartData = {
      labels: entries.map(([key]) => key),
      datasets: [{
        data: entries.map(([_, value]) => value),
        backgroundColor: colors,
        hoverOffset: 4
      }]
    };
  }

  // ✅ Repartition Objet
  if (data.repartitionObjet) {
    const entries = Object.entries(data.repartitionObjet);
    this.objetChartData = {
      labels: entries.map(([key]) => key),
      datasets: [{
        data: entries.map(([_, value]) => value),
        backgroundColor: colors,
        hoverOffset: 4
      }]
    };
  }

  // ✅ Evolution Mensuelle (si déjà tableau → on garde)
  if (Array.isArray(data.evolutionMensuelle)) {
    this.evolutionChartData = {
      labels: data.evolutionMensuelle.map(d => d.month),
      datasets: [{
        label: 'Total Réclamations',
        data: data.evolutionMensuelle.map(d => d.total),
        backgroundColor: 'rgba(37, 99, 235, 0.1)',
        borderColor: primary,
        fill: true,
        tension: 0.4
      }]
    };
   }
  }

  onAnneeChange() {
    this.loadData();
  }
  
  refresh() {
    this.loadData();
  }
}
