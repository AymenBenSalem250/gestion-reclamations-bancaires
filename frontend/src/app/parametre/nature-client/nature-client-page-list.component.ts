import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NatureReclamant } from '../../core/models';
import { NatureClientService } from './nature-client.service';
import { NatureClientDialogFormComponent } from './nature-client-dialog-form.component';
import { ConfirmDialogComponent } from '../shared-dialog/confirm-dialog.component';
import { exportToExcel, exportToPdf, ExportColumn } from '../shared/export.util';

@Component({
  selector: 'app-nature-client-page-list',
  standalone: true,
  imports: [CommonModule, FormsModule, NatureClientDialogFormComponent, ConfirmDialogComponent],
  templateUrl: './nature-client-page-list.component.html',
  styleUrls: ['../shared/parametre-page.css', './nature-client-page-list.component.css']
})
export class NatureClientPageListComponent implements OnInit {

  items: NatureReclamant[] = [];
  loading = false;
  error = '';
  search = '';
  currentPage = 1;
  readonly pageSize = 10;
  totalPages = 1;
  totalElements = 0;

  formVisible = false;
  editingItem: NatureReclamant | null = null;
  saving = false;
  formError = '';

  deleteVisible = false;
  deletingItem: NatureReclamant | null = null;
  deleting = false;

  successMessage = '';

  private readonly exportColumns: ExportColumn[] = [
    { key: 'id', label: 'ID' },
    { key: 'codNat', label: 'Code' },
    { key: 'libNat', label: 'Libellé' }
  ];

  constructor(private service: NatureClientService) {}

  ngOnInit(): void {
    this.loadData();
  }

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
      error: err => {
        this.error = err.error?.message ?? 'Erreur lors du chargement des données.';
        this.loading = false;
      }
    });
  }

  get pageNumbers(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }

  onSearchChange(): void { this.currentPage = 1; this.loadData(); }

  openCreate(): void { this.editingItem = null; this.formError = ''; this.formVisible = true; }
  openEdit(item: NatureReclamant): void { this.editingItem = { ...item }; this.formError = ''; this.formVisible = true; }
  closeForm(): void { this.formVisible = false; this.editingItem = null; this.formError = ''; }

  onSave(data: Omit<NatureReclamant, 'id'> & { id?: number }): void {
    this.saving = true;
    this.formError = '';
    const request$ = data.id ? this.service.update(data.id, data as NatureReclamant) : this.service.create(data);
    request$.subscribe({
      next: () => { this.saving = false; this.closeForm(); this.showSuccess(data.id ? 'Nature modifiée avec succès.' : 'Nature créée avec succès.'); this.loadData(); },
      error: err => { this.saving = false; this.formError = err.error?.message ?? 'Erreur lors de l\'enregistrement.'; }
    });
  }

  openDelete(item: NatureReclamant): void {
    this.error = '';
    this.deletingItem = item;
    this.deleteVisible = true;
  }

  cancelDelete(): void {
    this.deleteVisible = false;
    this.deletingItem = null;
  }

 confirmDelete(): void {
  if (!this.deletingItem) {
    return;
  }

  this.deleting = true;
  this.error = '';  // ✅ RESET ICI AVANT DELETE

  this.service.delete(this.deletingItem.id).subscribe({
    next: () => {
      this.deleting = false;
      this.error = '';   // ✅ RESET AUSSI ICI
      this.cancelDelete();
      this.showSuccess('Nature supprimée avec succès.');
      this.loadData();
    },
    error: err => {
      this.deleting = false;
      this.error = err.error?.message ?? 'Erreur lors de la suppression.';
      this.cancelDelete();
    }
  });
}

  exportExcel(): void {
    exportToExcel(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'nature-client');
  }

  exportPdf(): void {
    exportToPdf(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'Nature de client', 'nature-client');
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  private resetPageIfNeeded(): void {
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => this.successMessage = '', 3500);
  }
}
