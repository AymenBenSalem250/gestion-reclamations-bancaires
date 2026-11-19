import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NatureReclamant } from '../../core/models';

@Component({
  selector: 'app-nature-client-dialog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './nature-client-dialog-form.component.html',
  styleUrls: ['../shared/form-dialog.css', './nature-client-dialog-form.component.css']
})
export class NatureClientDialogFormComponent implements OnChanges {

  @Input() visible = false;
  @Input() item: NatureReclamant | null = null;
  @Input() saving = false;
  @Input() errorMessage = '';

  @Output() saved = new EventEmitter<Omit<NatureReclamant, 'id'> & { id?: number }>();
  @Output() closed = new EventEmitter<void>();

  form!: FormGroup;
  isEdit = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      codNat: ['', Validators.required],
      libNat: ['', Validators.required]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue || changes['item']) {
      this.isEdit = !!this.item;
      if (this.item) {
        this.form.patchValue({
          codNat: this.item.codNat ?? '',
          libNat: this.item.libNat ?? ''
        });
      } else {
        this.form.reset({ codNat: '', libNat: '' });
      }
    }
  }

  get title(): string {
    return this.isEdit ? 'Modifier la nature de client' : 'Ajouter une nature de client';
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && control.touched);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.value;
    if (this.isEdit && this.item) {
      this.saved.emit({ ...this.item, ...value });
    } else {
      this.saved.emit(value);
    }
  }

  onClose(): void {
    if (!this.saving) {
      this.closed.emit();
    }
  }
}
