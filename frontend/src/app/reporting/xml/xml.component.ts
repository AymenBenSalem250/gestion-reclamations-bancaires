import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportingService, XmlHistoryDTO } from '../reporting.service';

import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-xml',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent],
  templateUrl: './xml.component.html',
  styleUrls: ['./xml.component.css']
})
export class XmlComponent implements OnInit {
  reportingService = inject(ReportingService);

  annee: number = new Date().getFullYear();
  annees: number[] = [];

  generating: boolean = false;
  loadingHistory: boolean = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  historique: XmlHistoryDTO[] = [];

  ngOnInit() {
    this.initAnnees();
    this.loadHistory();
  }

  initAnnees() {
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 5; i++) {
      this.annees.push(currentYear - i);
    }
  }

  loadHistory() {
    this.loadingHistory = true;
    this.reportingService.getXmlHistory().subscribe({
      next: (data) => {
        this.historique = data;
        this.loadingHistory = false;
      },
      error: (err) => {
        console.error('Erreur chargement historique XML', err);
        this.loadingHistory = false;
      }
    });
  }

  generateXml() {
    this.generating = true;
    this.successMessage = null;
    this.errorMessage = null;

    this.reportingService.generateXml(this.annee).subscribe({
      next: (blob) => {
        this.generating = false;
        this.successMessage = `Le fichier BCT_760_${this.annee}.xml a été généré avec succès.`;
        
        // Téléchargement automatique
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `BCT_760_${this.annee}.xml`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        // Rafraîchir l'historique
        this.loadHistory();
      },
      error: (err) => {
        this.generating = false;
        this.errorMessage = "Une erreur s'est produite lors de la génération du fichier XML.";
        console.error(err);
      }
    });
  }
}
