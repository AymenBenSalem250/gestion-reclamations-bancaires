import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationType } from '../../core/models';
import { NotificationTypeService } from './notification-type.service';
import { NotificationTypeDialogFormComponent } from './notification-type-dialog-form.component';
import { ConfirmDialogComponent } from '../shared-dialog/confirm-dialog.component';
import { exportToExcel, exportToPdf, ExportColumn } from '../shared/export.util';

@Component({
  selector: 'app-notification-type-page-list',
  standalone: true,
  imports: [CommonModule, FormsModule, NotificationTypeDialogFormComponent, ConfirmDialogComponent],
  templateUrl: './notification-type-page-list.component.html',
  styleUrls: ['../shared/parametre-page.css']
})
export class NotificationTypePageListComponent implements OnInit {

  items: NotificationType[] = [];
  loading = false;
  error = '';
  search = '';
  currentPage = 1;
  readonly pageSize = 10;
  totalPages = 1;
  totalElements = 0;

  formVisible = false;
  editingItem: NotificationType | null = null;
  saving = false;
  formError = '';
  deleteVisible = false;
  deletingItem: NotificationType | null = null;
  deleting = false;
  successMessage = '';

  private readonly exportColumns: ExportColumn[] = [
    { key: 'id', label: 'ID' },
    { key: 'libelle', label: 'Libellé' }
  ];

  constructor(private service: NotificationTypeService) {}

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
  openEdit(item: NotificationType): void { this.editingItem = { ...item }; this.formError = ''; this.formVisible = true; }
  closeForm(): void { this.formVisible = false; this.editingItem = null; this.formError = ''; }

  onSave(data: Omit<NotificationType, 'id'> & { id?: number }): void {
    this.saving = true;
    this.formError = '';
    const request$ = data.id ? this.service.update(data.id, data as NotificationType) : this.service.create(data);
    request$.subscribe({
      next: () => { this.saving = false; this.closeForm(); this.showSuccess(data.id ? 'Type de notification modifié avec succès.' : 'Type de notification créé avec succès.'); this.loadData(); },
      error: err => { this.saving = false; this.formError = err.error?.message ?? 'Erreur lors de l\'enregistrement.'; }
    });
  }

  openDelete(item: NotificationType): void {
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
      this.showSuccess('Type de notification supprimé avec succès.');
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

  exportExcel(): void { exportToExcel(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'notification-types'); }
  exportPdf(): void { exportToPdf(this.items as unknown as Record<string, unknown>[], this.exportColumns, 'Type de notification', 'notification-types'); }
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

  getBadgeClass(libelle: string): string {
    const lib = (libelle || '').toLowerCase();
    if (lib.includes('sms')) return 'badge-sms';
    if (lib.includes('email')) return 'badge-email';
    if (lib.includes('aucun')) return 'badge-none';
    return 'badge-default';
  }
}
