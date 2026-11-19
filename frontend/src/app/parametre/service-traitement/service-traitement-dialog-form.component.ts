import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Service } from '../../core/models';

@Component({
  selector: 'app-service-traitement-dialog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './service-traitement-dialog-form.component.html',
  styleUrls: ['../shared/form-dialog.css']
})
export class ServiceTraitementDialogFormComponent implements OnChanges {

  @Input() visible = false;
  @Input() item: Service | null = null;
  @Input() saving = false;
  @Input() errorMessage = '';
  @Output() saved = new EventEmitter<Service>();
  @Output() closed = new EventEmitter<void>();

  form!: FormGroup;
  isEdit = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      codSer: ['', Validators.required],
      libSer: ['', Validators.required]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue || changes['item']) {
      this.isEdit = !!this.item;
      if (this.item) {
        this.form.patchValue({ codSer: this.item.codSer, libSer: this.item.libSer });
        this.form.get('codSer')?.disable();
      } else {
        this.form.reset({ codSer: '', libSer: '' });
        this.form.get('codSer')?.enable();
      }
    }
  }

  get title(): string { return this.isEdit ? 'Modifier le service' : 'Ajouter un service'; }
  isInvalid(field: string): boolean { const c = this.form.get(field); return !!(c && c.invalid && c.touched); }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saved.emit(this.form.getRawValue() as Service);
  }

  onClose(): void { if (!this.saving) this.closed.emit(); }
}
