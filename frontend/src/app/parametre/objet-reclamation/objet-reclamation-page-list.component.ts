import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Objet, Service } from '../../core/models';
import { ObjetReclamationService } from './objet-reclamation.service';
import { ObjetReclamationDialogFormComponent } from './objet-reclamation-dialog-form.component';
import { ConfirmDialogComponent } from '../shared-dialog/confirm-dialog.component';
import { exportToExcel, exportToPdf, ExportColumn } from '../shared/export.util';

@Component({
  selector: 'app-objet-reclamation-page-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ObjetReclamationDialogFormComponent, ConfirmDialogComponent],
  templateUrl: './objet-reclamation-page-list.component.html',
  styleUrls: ['../shared/parametre-page.css']
})
export class ObjetReclamationPageListComponent implements OnInit {

  items: Objet[] = [];
  services: Service[] = [];
  loading = false;
  error = '';
  search = '';
  currentPage = 1;
  readonly pageSize = 10;
  totalPages = 1;
  totalElements = 0;

  formVisible = false;
  editingItem: Objet | null = null;
  saving = false;
  formError = '';
  deleteVisible = false;
  deletingItem: Objet | null = null;
  deleting = false;
  successMessage = '';

  private readonly exportColumns: ExportColumn[] = [
    { key: 'id', label: 'ID' },
    { key: 'codObj', label: 'Code Objet' },
    { key: 'libObj', label: 'Libellé' },
    { key: 'serviceLabel', label: 'Service traitant', format: (_, row) => String(row['serviceLabel'] ?? '') }
  ];

  constructor(private service: ObjetReclamationService) {}

  ngOnInit(): void {
    this.loadServices();
    this.loadData();
  }

  loadServices(): void {
    this.service.getServices().subscribe({
      next: data => this.services = data,
      error: () => this.services = []
    });
  }

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

  serviceLabel(item: Objet): string {
    return item.service?.libSer || '';
  }

  openCreate(): void { this.editingItem = null; this.formError = ''; this.formVisible = true; }
  openEdit(item: Objet): void { this.editingItem = { ...item }; this.formError = ''; this.formVisible = true; }
  closeForm(): void { this.formVisible = false; this.editingItem = null; this.formError = ''; }

  onSave(data: Omit<Objet, 'id'> & { id?: number }): void {
    this.saving = true;
    this.formError = '';
    const request$ = data.id ? this.service.update(data.id, data as Objet) : this.service.create(data);
    request$.subscribe({
      next: () => { this.saving = false; this.closeForm(); this.showSuccess(data.id ? 'Objet modifié avec succès.' : 'Objet créé avec succès.'); this.loadData(); },
      error: err => { this.saving = false; this.formError = err.error?.message ?? 'Erreur lors de l\'enregistrement.'; }
    });
  }

  openDelete(item: Objet): void {
  this.error = '';           // ✅ IMPORTANT
  this.successMessage = '';
  this.deletingItem = item;
  this.deleteVisible = true;
  }
  cancelDelete(): void { this.deleteVisible = false; this.deletingItem = null; }
  confirmDelete(): void {
  if (!this.deletingItem) return;

  this.deleting = true;
  this.error = '';           // ✅ RESET ICI
  this.successMessage = '';  // ✅ RESET SUCCESS

  this.service.delete(this.deletingItem.id).subscribe({
    next: () => {
      this.deleting = false;
      this.error = '';       // ✅ RESET AUSSI ICI
      this.cancelDelete();
      this.showSuccess('Objet supprimé avec succès.');
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
  

  private exportRows(): Record<string, unknown>[] {
    return this.items.map(item => ({
      ...item,
      serviceLabel: item.service?.libSer || ''
    })) as unknown as Record<string, unknown>[];
  }

  exportExcel(): void { exportToExcel(this.exportRows(), this.exportColumns, 'objet-reclamation'); }
  exportPdf(): void { exportToPdf(this.exportRows(), this.exportColumns, 'Objet de réclamation', 'objet-reclamation'); }
  goToPage(page: number): void { if (page >= 1 && page <= this.totalPages && page !== this.currentPage) { this.currentPage = page; this.loadData(); } }
  private resetPageIfNeeded(): void { if (this.currentPage > this.totalPages && this.totalPages > 0) { this.currentPage = this.totalPages; this.loadData(); } }
  private showSuccess(message: string): void { this.successMessage = message; setTimeout(() => this.successMessage = '', 3500); }
}
