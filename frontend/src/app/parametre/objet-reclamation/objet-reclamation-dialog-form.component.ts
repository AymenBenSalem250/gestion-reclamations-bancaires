import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Objet, Service } from '../../core/models';

@Component({
  selector: 'app-objet-reclamation-dialog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './objet-reclamation-dialog-form.component.html',
  styleUrls: ['../shared/form-dialog.css']
})
export class ObjetReclamationDialogFormComponent implements OnChanges {

  @Input() visible = false;
  @Input() item: Objet | null = null;
  @Input() services: Service[] = [];
  @Input() saving = false;
  @Input() errorMessage = '';

  @Output() saved = new EventEmitter<Omit<Objet, 'id'> & { id?: number }>();
  @Output() closed = new EventEmitter<void>();

  form!: FormGroup;
  isEdit = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      codObj: ['', Validators.required],
      libObj: ['', Validators.required],
      codSer: ['', Validators.required]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue || changes['item']) {
      this.isEdit = !!this.item;
      if (this.item) {
        this.form.patchValue({
          codObj: this.item.codObj ?? '',
          libObj: this.item.libObj ?? '',
          codSer: this.item.service?.codSer ?? ''
        });
      } else {
        this.form.reset({ codObj: '', libObj: '', codSer: '' });
      }
    }
  }

  get title(): string {
    return this.isEdit ? 'Modifier l\'objet de réclamation' : 'Ajouter un objet de réclamation';
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

    const { codObj, libObj, codSer } = this.form.value;
    const service = this.services.find(s => s.codSer === codSer);

    const payload: Omit<Objet, 'id'> & { id?: number } = {
      codObj,
      libObj,
      service
    };

    if (this.isEdit && this.item) {
      payload.id = this.item.id;
    }

    this.saved.emit(payload);
  }

  onClose(): void {
    if (!this.saving) {
      this.closed.emit();
    }
  }
}
