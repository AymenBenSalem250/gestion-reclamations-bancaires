import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Statut } from '../../core/models';
import { SortReclamationService } from './sort-reclamation.service';
import { SortReclamationDialogFormComponent } from './sort-reclamation-dialog-form.component';
import { ConfirmDialogComponent } from '../shared-dialog/confirm-dialog.component';
import { exportToExcel, exportToPdf, ExportColumn } from '../shared/export.util';

@Component({
  selector: 'app-sort-reclamation-page-list',
  standalone: true,
  imports: [CommonModule, FormsModule, SortReclamationDialogFormComponent, ConfirmDialogComponent],
  templateUrl: './sort-reclamation-page-list.component.html',
  styleUrls: ['../shared/parametre-page.css']
})
export class SortReclamationPageListComponent implements OnInit {

  items: Statut[] = [];
  loading = false;
  error = '';
  search = '';
  currentPage = 1;
  readonly pageSize = 10;
  totalPages = 1;
  totalElements = 0;

  formVisible = false;
  editingItem: Statut | null = null;
  saving = false;
  formError = '';

  deleteVisible = false;
  deletingItem: Statut | null = null;
  deleting = false;
  successMessage = '';

  private readonly exportColumns: ExportColumn[] = [
    { key: 'codSta', label: 'Code' },
    { key: 'libSta', label: 'Libellé' },
    { key: 'msgSta', label: 'Message 1' },
    { key: 'msg2Sta', label: 'Message 2' }
  ];

  constructor(private service: SortReclamationService) {}

  ngOnInit(): void { this.loadData(); }

  loadData(): void {
    this.loading = true;
    this.error = '';
    this.service.getAll(this.currentPage - 1, this.pageSize, this.search.trim()).subscribe({
      next: res => { 
        this.items = res.content; 
        this.totalPages = Math.max(1, res.totalPages); 
        this.totalElements = res.totalElements;
        this.loading = false; 
        this.resetPageIfNeeded(); 
      },
      error: err => { this.error = err.error?.message ?? 'Erreur lors du chargement.'; this.loading = false; }
    });
  }

  get pageNumbers(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }

  onSearchChange(): void { this.currentPage = 1; this.loadData(); }

  openCreate(): void { this.editingItem = null; this.formError = ''; this.formVisible = true; }
  openEdit(item: Statut): void { this.editingItem = { ...item }; this.formError = ''; this.formVisible = true; }
  closeForm(): void { this.formVisible = false; this.editingItem = null; this.formError = ''; }

  onSave(data: any): void {
    this.saving = true;
    this.formError = '';
    const isEdit = !!this.editingItem;
    const request$ = isEdit ? this.service.update(data.codSta, data) : this.service.create(data);
    request$.subscribe({
      next: () => { this.saving = false; this.closeForm(); this.showSuccess(isEdit ? 'Statut modifié avec succès.' : 'Statut créé avec succès.'); this.loadData(); },
      error: err => { this.saving = false; this.formError = err.error?.message ?? 'Erreur lors de l\'enregistrement.'; }
    });
  }

  openDelete(item: Statut): void {
  this.error = '';            
  this.successMessage = '';
  this.deletingItem = item;
  this.deleteVisible = true;
}
  cancelDelete(): void { this.deleteVisible = false; this.deletingItem = null; }

  confirmDelete(): void {
  if (!this.deletingItem) return;

  this.deleting = true;
  this.error = '';            
  this.successMessage = '';   

  this.service.delete(this.deletingItem.codSta).subscribe({
    next: () => {
      this.deleting = false;
      this.error = '';        
      this.cancelDelete();
      this.showSuccess('Sort supprimé avec succès.');
      this.loadData();
    },
    error: err => {
      this.deleting = false;
      console.error(err);
      this.error = err.error?.message ?? 'Erreur lors de la suppression.';
      this.cancelDelete();
    }
  });
}

  exportExcel(): void { exportToExcel(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'sort-reclamation'); }
  exportPdf(): void { exportToPdf(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'Sort de réclamation', 'sort-reclamation'); }
  goToPage(page: number): void { if (page >= 1 && page <= this.totalPages && page !== this.currentPage) { this.currentPage = page; this.loadData(); } }
  private resetPageIfNeeded(): void { if (this.currentPage > this.totalPages && this.totalPages > 0) { this.currentPage = this.totalPages; this.loadData(); } }
  private showSuccess(message: string): void { this.successMessage = message; setTimeout(() => this.successMessage = '', 3500); }
}
