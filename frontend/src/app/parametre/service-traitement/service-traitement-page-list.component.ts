import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Service } from '../../core/models';
import { ServiceTraitementService } from './service-traitement.service';
import { ServiceTraitementDialogFormComponent } from './service-traitement-dialog-form.component';
import { ConfirmDialogComponent } from '../shared-dialog/confirm-dialog.component';
import { exportToExcel, exportToPdf, ExportColumn } from '../shared/export.util';

@Component({
  selector: 'app-service-traitement-page-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ServiceTraitementDialogFormComponent, ConfirmDialogComponent],
  templateUrl: './service-traitement-page-list.component.html',
  styleUrls: ['../shared/parametre-page.css']
})
export class ServiceTraitementPageListComponent implements OnInit {

  items: Service[] = [];
  loading = false;
  error = '';
  search = '';
  currentPage = 1;
  readonly pageSize = 10;
  totalPages = 1;
  totalElements = 0;

  formVisible = false;
  editingItem: Service | null = null;
  saving = false;
  formError = '';
  deleteVisible = false;
  deletingItem: Service | null = null;
  deleting = false;
  successMessage = '';

  private readonly exportColumns: ExportColumn[] = [
    { key: 'codSer', label: 'Code' },
    { key: 'libSer', label: 'Libellé' }
  ];

  constructor(private service: ServiceTraitementService) {}

  ngOnInit(): void { this.loadData(); }

  loadData(): void {
    this.loading = true;
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
  openEdit(item: Service): void { this.editingItem = { ...item }; this.formError = ''; this.formVisible = true; }
  closeForm(): void { this.formVisible = false; this.editingItem = null; this.formError = ''; }

  onSave(data: any): void {
    this.saving = true;
    this.formError = '';
    const isEdit = !!this.editingItem;
    const request$ = isEdit ? this.service.update(data.codSer, data) : this.service.create(data);
    request$.subscribe({
      next: () => { this.saving = false; this.closeForm(); this.showSuccess(isEdit ? 'Service modifié avec succès.' : 'Service créé avec succès.'); this.loadData(); },
      error: err => { this.saving = false; this.formError = err.error?.message ?? 'Erreur lors de l\'enregistrement.'; }
    });
  }

 openDelete(item: Service): void {
  this.error = '';    // ✅ IMPORTANT
  this.deletingItem = item;
  this.deleteVisible = true;
  }
  cancelDelete(): void { this.deleteVisible = false; this.deletingItem = null; }
  confirmDelete(): void {
  if (!this.deletingItem) return;

  this.deleting = true;
  this.error = '';           // ✅ RESET ICI

  this.service.delete(this.deletingItem.codSer).subscribe({
    next: () => {
      this.deleting = false;
      this.error = '';       // ✅ RESET AUSSI ICI
      this.cancelDelete();
      this.showSuccess('Service supprimé avec succès.');
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

  exportExcel(): void { exportToExcel(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'service-traitement'); }
  exportPdf(): void { exportToPdf(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'Service de traitement', 'service-traitement'); }
  goToPage(page: number): void { if (page >= 1 && page <= this.totalPages && page !== this.currentPage) { this.currentPage = page; this.loadData(); } }
  private resetPageIfNeeded(): void { if (this.currentPage > this.totalPages && this.totalPages > 0) { this.currentPage = this.totalPages; this.loadData(); } }
  private showSuccess(message: string): void { this.successMessage = message; setTimeout(() => this.successMessage = '', 3500); }
}
