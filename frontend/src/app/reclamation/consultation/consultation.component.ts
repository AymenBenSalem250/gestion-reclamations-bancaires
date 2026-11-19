import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Reclamation } from '../../core/models';
import { ReclamationService } from '../../core/reclamation.service';
import { ReferenceService } from '../../core/reference.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-reclamation-consultation',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, PageHeaderComponent],
  templateUrl: './consultation.component.html',
  styleUrls: ['../../parametre/shared/parametre-page.css', './consultation.component.css']
})
export class ConsultationComponent implements OnInit {

  items: Reclamation[] = [];
  loading = false;
  error = '';
  searchText = '';
  selectedStatus = '';
  private searchSubject = new Subject<string>();
  currentPage = 1;
  readonly pageSize = 10;
  totalPages = 1;
  totalElements = 0;

  statutOptions: { code: string, label: string }[] = [
    { code: '', label: 'Tous les statuts' }
  ];

  constructor(
    private service: ReclamationService,
    private referenceService: ReferenceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(searchValue => {
      this.searchText = searchValue;
      this.currentPage = 1;
      this.loadReclamations();
    });

    this.loadStatuts();
    this.loadReclamations();
  }

  loadStatuts(): void {
    this.referenceService.getStatuts(0, 1000).subscribe({
      next: data => {
        const dynamicOptions = data.content.map(s => ({
          code: s.codSta,
          label: s.libSta
        }));
        this.statutOptions = [
          { code: '', label: 'Tous les statuts' },
          ...dynamicOptions
        ];
      }
    });
  }

  loadReclamations(): void {
    this.loading = true;
    this.error = '';

    const params: any = {
      page: this.currentPage - 1,
      size: this.pageSize
    };

    if (this.searchText && this.searchText.trim() !== '') {
      params.search = this.searchText.trim();
    } else {
      params.search = null;
    }

    if (this.selectedStatus && this.selectedStatus !== '') {
      params.codSta = this.selectedStatus;
    } else {
      params.codSta = null;
    }

    this.service.getAll(params).subscribe({
      next: res => {
        this.items = res.content;
        this.totalPages = Math.max(1, res.totalPages);
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement des réclamations.';
        this.loading = false;
      }
    });
  }

  getBadgeClass(statut?: string): string {

    if (!statut) return 'badge-default';

    const s = statut.toLowerCase();

    if (s.includes('cours')) return 'badge-encours';
    if (s.includes('favorable') || s.includes('trait')) return 'badge-traite';
    if (s.includes('rejet')) return 'badge-rejete';
    if (s.includes('compl')) return 'badge-complement';
    if (s.includes('clôt')) return 'badge-cloture';

    return 'badge-default';
  }

  openModification(item: Reclamation): void {
    this.router.navigate(['/reclamation/modification', item.refRec]);
  }

  openTraitement(item: Reclamation): void {
    this.router.navigate(['/reclamation/traitement', item.refRec]);
  }

  openMessages(item: Reclamation): void {
    this.router.navigate(['/reclamation/messages', item.refRec]);
  }
  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  onStatutFilterChange(): void {
    this.currentPage = 1;
    this.loadReclamations();
  }

get pageNumbers(): number[] {
  return Array.from({ length: this.totalPages }, (_, i) => i + 1);
}

goToPage(page: number): void {
  if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
    this.currentPage = page;
    this.loadReclamations();
  }
}
}