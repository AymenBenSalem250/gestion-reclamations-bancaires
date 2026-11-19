import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Canal } from '../../core/models';
import { CanalReceptionService } from './canal-reception.service';
import { CanalReceptionDialogFormComponent } from './canal-reception-dialog-form.component';
import { ConfirmDialogComponent } from '../shared-dialog/confirm-dialog.component';
import { exportToExcel, exportToPdf, ExportColumn } from '../shared/export.util';

@Component({
  selector: 'app-canal-reception-page-list',
  standalone: true,
  imports: [CommonModule, FormsModule, CanalReceptionDialogFormComponent, ConfirmDialogComponent],
  templateUrl: './canal-reception-page-list.component.html',
  styleUrls: ['../shared/parametre-page.css']
})
export class CanalReceptionPageListComponent implements OnInit {

  items: Canal[] = [];
  loading = false;
  error = '';
  search = '';
  currentPage = 1;
  readonly pageSize = 10;
  totalPages = 1;
  totalElements = 0;

  formVisible = false;
  editingItem: Canal | null = null;
  saving = false;
  formError = '';
  deleteVisible = false;
  deletingItem: Canal | null = null;
  deleting = false;
  successMessage = '';

  private readonly exportColumns: ExportColumn[] = [
    { key: 'id', label: 'ID' },
    { key: 'libCan', label: 'Libellé' }
  ];

  constructor(private service: CanalReceptionService) {}

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
  openEdit(item: Canal): void { this.editingItem = { ...item }; this.formError = ''; this.formVisible = true; }
  closeForm(): void { this.formVisible = false; this.editingItem = null; this.formError = ''; }

  onSave(data: Omit<Canal, 'id'> & { id?: number }): void {
    this.saving = true;
    this.formError = '';
    const request$ = data.id ? this.service.update(data.id, data as Canal) : this.service.create(data);
    request$.subscribe({
      next: () => { this.saving = false; this.closeForm(); this.showSuccess(data.id ? 'Canal modifié avec succès.' : 'Canal créé avec succès.'); this.loadData(); },
      error: err => { this.saving = false; this.formError = err.error?.message ?? 'Erreur lors de l\'enregistrement.'; }
    });
  }

  openDelete(item: Canal): void {
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

  this.service.delete(this.deletingItem.id).subscribe({
    next: () => {
      this.deleting = false;
      this.error = '';       
      this.cancelDelete();
      this.showSuccess('Canal supprimé avec succès.');
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

  exportExcel(): void { exportToExcel(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'canal-reception'); }
  exportPdf(): void { exportToPdf(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'Canal de réception', 'canal-reception'); }
  goToPage(page: number): void { 
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
      this.currentPage = page; 
      this.loadData();
    }
  }
  private resetPageIfNeeded(): void { 
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
      this.loadData();
    }
  }
  private showSuccess(message: string): void { this.successMessage = message; setTimeout(() => this.successMessage = '', 3500); }
}
