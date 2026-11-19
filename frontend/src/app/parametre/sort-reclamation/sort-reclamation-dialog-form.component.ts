import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Statut } from '../../core/models';

@Component({
  selector: 'app-sort-reclamation-dialog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './sort-reclamation-dialog-form.component.html',
  styleUrls: ['../shared/form-dialog.css']
})
export class SortReclamationDialogFormComponent implements OnChanges {

  @Input() visible = false;
  @Input() item: Statut | null = null;
  @Input() saving = false;
  @Input() errorMessage = '';

  @Output() saved = new EventEmitter<Statut>();
  @Output() closed = new EventEmitter<void>();

  form!: FormGroup;
  isEdit = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      codSta: ['', Validators.required],
      libSta: ['', Validators.required],
      msgSta: ['', Validators.required],
      msg2Sta: ['', Validators.required]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue || changes['item']) {
      this.isEdit = !!this.item;
      if (this.item) {
        this.form.patchValue({
          codSta: this.item.codSta,
          libSta: this.item.libSta,
          msgSta: this.item.msgSta ?? '',
          msg2Sta : this.item.msg2Sta  ?? ''
        });
        this.form.get('codSta')?.disable();
      } else {
        this.form.reset({ codSta: '', libSta: '', msgSta: '', msg2Sta : '' });
        this.form.get('codSta')?.enable();
      }
    }
  }

  get title(): string {
    return this.isEdit ? 'Modifier le sort de réclamation' : 'Ajouter un sort de réclamation';
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
    this.saved.emit(this.form.getRawValue() as Statut);
  }

  onClose(): void {
    if (!this.saving) {
      this.closed.emit();
    }
  }
}
