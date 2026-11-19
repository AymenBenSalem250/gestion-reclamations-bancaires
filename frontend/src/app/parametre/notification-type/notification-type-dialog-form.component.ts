import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NotificationType } from '../../core/models';

@Component({
  selector: 'app-notification-type-dialog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './notification-type-dialog-form.component.html',
  styleUrls: ['../shared/form-dialog.css']
})
export class NotificationTypeDialogFormComponent implements OnChanges {

  @Input() visible = false;
  @Input() item: NotificationType | null = null;
  @Input() saving = false;
  @Input() errorMessage = '';
  @Output() saved = new EventEmitter<Omit<NotificationType, 'id'> & { id?: number }>();
  @Output() closed = new EventEmitter<void>();

  form!: FormGroup;
  isEdit = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      libelle: ['', Validators.required],
      active: [true]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue || changes['item']) {
      this.isEdit = !!this.item;
      if (this.item) {
        this.form.patchValue({
          libelle: this.item.libelle ?? '',
          active: this.item.active ?? true
        });
      } else {
        this.form.reset({ libelle: '', active: true });
      }
    }
  }

  get title(): string { return this.isEdit ? 'Modifier le type de notification' : 'Ajouter un type de notification'; }
  isInvalid(field: string): boolean { const c = this.form.get(field); return !!(c && c.invalid && c.touched); }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.value;
    this.saved.emit(this.isEdit && this.item ? { ...this.item, ...value } : value);
  }

  onClose(): void { if (!this.saving) this.closed.emit(); }
}
