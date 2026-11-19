import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Canal } from '../../core/models';

@Component({
  selector: 'app-canal-reception-dialog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './canal-reception-dialog-form.component.html',
  styleUrls: ['../shared/form-dialog.css']
})
export class CanalReceptionDialogFormComponent implements OnChanges {

  @Input() visible = false;
  @Input() item: Canal | null = null;
  @Input() saving = false;
  @Input() errorMessage = '';
  @Output() saved = new EventEmitter<Omit<Canal, 'id'> & { id?: number }>();
  @Output() closed = new EventEmitter<void>();

  form!: FormGroup;
  isEdit = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({ libCan: ['', Validators.required] });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue || changes['item']) {
      this.isEdit = !!this.item;
      this.form.patchValue({ libCan: this.item?.libCan ?? '' });
      if (!this.item) this.form.reset({ libCan: '' });
    }
  }

  get title(): string { return this.isEdit ? 'Modifier le canal' : 'Ajouter un canal'; }
  isInvalid(field: string): boolean { const c = this.form.get(field); return !!(c && c.invalid && c.touched); }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.value;
    this.saved.emit(this.isEdit && this.item ? { ...this.item, ...value } : value);
  }

  onClose(): void { if (!this.saving) this.closed.emit(); }
}
