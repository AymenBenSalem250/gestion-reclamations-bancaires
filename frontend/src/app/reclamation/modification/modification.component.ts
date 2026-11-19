import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ReclamationService } from '../../core/reclamation.service';
import { ReferenceService } from '../../core/reference.service';
import {
  Reclamation,
  Canal,
  Objet,
  NatureReclamant,
  NotificationType
} from '../../core/models';

import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

const STATUT_EN_COURS = '01';

@Component({
  selector: 'app-reclamation-modification',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent],
  templateUrl: './modification.component.html',
  styleUrls: ['../saisie/saisie.component.css', './modification.component.css']
})
export class ModificationComponent implements OnInit {

  refRec: string = '';
  reclamation: Reclamation | null = null;

  canals: Canal[] = [];
  objets: Objet[] = [];
  natures: NatureReclamant[] = [];
  notificationTypes: NotificationType[] = [];

  loading = true;
  error = '';
  accessDenied = false;
  submitting = false;
  submitError = '';
  submitSuccess = false;

  isAdmin = false;

  formData = {
    description: '',
    reponse: '',
    canalId: null as number | null,
    objetId: null as number | null,
    natRecId: null as number | null,
    notificationTypeIds: [] as number[],
    telephone: '',
    email: ''
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reclamationService: ReclamationService,
    private referenceService: ReferenceService
  ) {}

  /* =======================================================
     INIT
  ======================================================= */

  ngOnInit(): void {

    const rolesData = localStorage.getItem('roles');

    if (rolesData) {
      try {
        const roles = JSON.parse(rolesData);
        this.isAdmin = Array.isArray(roles)
          ? roles.includes('ROLE_ADMIN')
          : roles === 'ROLE_ADMIN';
      } catch {
        this.isAdmin = rolesData.includes('ROLE_ADMIN');
      }
    }

    this.refRec = this.route.snapshot.paramMap.get('refRec') || '';

    if (!this.refRec) {
      this.error = 'Référence de réclamation introuvable.';
      this.loading = false;
      return;
    }

    this.loadReferences();
    this.loadData();
  }

  /* =======================================================
     REFERENCES
  ======================================================= */

  loadReferences(): void {

    this.referenceService.getCanals(0, 1000)
      .subscribe(res => this.canals = res.content);

    this.referenceService.getObjets(0, 1000)
      .subscribe(res => this.objets = res.content);

    this.referenceService.getNatures(0, 1000)
      .subscribe(res => this.natures = res.content);

    this.referenceService.getNotificationTypes(0, 1000)
      .subscribe(res => {
        this.notificationTypes =
          res.content.filter(nt => nt.active === true);
      });
  }

  /* =======================================================
     LOAD DATA
  ======================================================= */

  loadData(): void {

    this.loading = true;
    this.accessDenied = false;
    this.error = '';
    

    this.reclamationService.getById(this.refRec).subscribe({
      next: data => {

        this.reclamation = data;

        this.formData = {
          description: data.descRec || '',
          reponse: data.compRec || '',
          canalId: data.canalId ?? null,
          objetId: data.objetId ?? null,
          natRecId: data.natRecId ?? null,
          notificationTypeIds: [...(data.notificationTypeIds ?? [])],
          telephone: data.telephone || '',
          email: data.emailRec || ''
        };

        this.loading = false;
      },
      error: (err) => {
        this.loading = false;

        if (err.status === 403) {
          this.accessDenied = true;
        } else {
          this.error = 'Impossible de charger cette réclamation.';
        }
      }
      
    });
  }

  /* =======================================================
     MULTI NOTIFICATION
  ======================================================= */

  toggleNotification(id: number): void {

    if (!this.canModifyNotificationType) return;

    if (this.formData.notificationTypeIds.includes(id)) {
      this.formData.notificationTypeIds =
        this.formData.notificationTypeIds.filter(x => x !== id);
    } else {
      this.formData.notificationTypeIds = [
        ...this.formData.notificationTypeIds,
        id
      ];
    }
  }

  isNotificationSelected(id: number): boolean {
    return this.formData.notificationTypeIds.includes(id);
  }

  normalize(value: string): string {
    return value?.trim().toUpperCase() ?? '';
  }

  /* =======================================================
     VALIDATION
  ======================================================= */

  get canModify(): boolean {

    if (!this.reclamation) return false;

    const statut = (this.reclamation as any).statutCode;

    if (this.isAdmin) return true;

    return statut === STATUT_EN_COURS;
  }

  get canModifyNotificationType(): boolean {

    if (!this.reclamation) return false;

    const statut = (this.reclamation as any).statutCode;

    if (this.isAdmin) return true;

    return statut === STATUT_EN_COURS;
  }

  get isValid(): boolean {
    if (!this.canModify) return false;
    return this.formData.description.trim().length >= 10;
  }

  get descriptionTooShort(): boolean {
    return this.formData.description.trim().length > 0 &&
           this.formData.description.trim().length < 10;
  }

  /* =======================================================
     SUBMIT
  ======================================================= */

  submit(): void {

    if (!this.isValid || !this.canModify) return;

    this.submitting = true;
    this.submitError = '';

    const payload = {
      descRec: this.formData.description.trim(),
      compRec: this.formData.reponse.trim(),
      canalId: this.formData.canalId,
      objetId: this.formData.objetId,
      natRecId: this.formData.natRecId,
      notificationTypeIds: this.formData.notificationTypeIds,
      telephone: this.formData.telephone,
      emailRec: this.formData.email
    };

    this.reclamationService.update(this.refRec, payload).subscribe({
      next: () => {
        this.submitting = false;
        this.submitSuccess = true;
      },
      error: (err) => {
        this.submitting = false;
        this.submitError =
          err.error?.message ??
          'Erreur lors de la modification.';
      }
    });
  }

  /* =======================================================
     NAVIGATION
  ======================================================= */

  goToConsultation(): void {
    this.router.navigate(['/reclamation/consultation']);
  }

  goToTraitement(): void {
    this.router.navigate(['/reclamation/traitement', this.refRec]);
  }
}