import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ReclamationService } from '../../core/reclamation.service';
import { ReferenceService } from '../../core/reference.service';
import { Reclamation, Statut } from '../../core/models';

import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-traitement',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent],
  templateUrl: './traitement.component.html',
  styleUrls: ['../saisie/saisie.component.css', './traitement.component.css']
})
export class TraitementComponent implements OnInit {

  // ── Navigation ──────────────────────────────────────────────────────────────
  stepIndex = 1;

  steps = [
    { index: 1, label: 'Réclamation' },
    { index: 2, label: 'Décision' },
    { index: 3, label: 'Confirmation' }
  ];

  // ── Data ───────────────────────────────────────────────────────────────────
  refRec = '';
  reclamation: Reclamation | null = null;
  statuts: Statut[] = [];

  loading = true;
  error = '';
  accessDenied = false;   // 403 — accès refusé

  // ── Step 2 form ──────────────────────────────────────────────────────────────────
  selectedStatutCode = '';
  commentaire = '';

  // ── Submission ──────────────────────────────────────────────────────────────────
  submitting = false;
  submitError = '';
  submitSuccess = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reclamationService: ReclamationService,
    private referenceService: ReferenceService
  ) {}

  ngOnInit(): void {
    this.refRec = this.route.snapshot.paramMap.get('refRec') || '';
    if (!this.refRec) {
      this.error = 'Référence de réclamation introuvable.';
      this.loading = false;
      return;
    }
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.accessDenied = false;
    this.error = '';

    // Charger les statuts disponibles (référentiel)
    this.referenceService.getStatuts().subscribe({
      next: data => this.statuts = data.content,
      error: () => this.statuts = []
    });

    this.reclamationService.getById(this.refRec).subscribe({
      next: data => {
        this.reclamation = data ?? null;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 403) {
          this.accessDenied = true;
        } else {
          this.error = 'Erreur lors du chargement de la réclamation.';
        }
      }
    });
  }

  // ── Getters décision ──────────────────────────────────────────────────────

  get selectedStatut(): Statut | undefined {
    return this.statuts.find(s => s.codSta === this.selectedStatutCode);
  }

  /**
   * Rejet détecté si :
   *   - code backend = '03' (valeur normalisée backend TSB)
   *   - OU libellé contient 'REJET' (fallback affichage)
   */
  get isRejet(): boolean {
    if (!this.selectedStatutCode) return false;
    const code = this.selectedStatutCode.toUpperCase();
    const lib  = (this.selectedStatut?.libSta || '').toUpperCase();
    return code === '03' || lib.includes('REJET');
  }

  get isFavorable(): boolean {
    if (!this.selectedStatutCode) return false;
    const code = this.selectedStatutCode.toUpperCase();
    const lib  = (this.selectedStatut?.libSta || '').toUpperCase();
    return code === '02' || lib.includes('TRAITE') || lib.includes('FAVORABLE');
  }

  get isComplement(): boolean {
    if (!this.selectedStatutCode) return false;
    const code = this.selectedStatutCode.toUpperCase();
    const lib  = (this.selectedStatut?.libSta || '').toUpperCase();
    return code === '04' || lib.includes('COMPLEMENT') || lib.includes('COMPLÉMENT');
  }

  get isStep2Valid(): boolean {
    if (!this.selectedStatutCode) return false;
    // Si rejet → commentaire (justification) obligatoire
    if (this.isRejet && !this.commentaire.trim()) return false;
    return true;
  }

  get decisionLabel(): string {
    if (this.isFavorable) return 'Décision : Traité favorablement';
    if (this.isRejet)     return 'Décision : Rejet de la réclamation';
    if (this.isComplement) return "Décision : Complément d'information requis";
    return this.selectedStatut?.libSta || 'Décision sélectionnée';
  }

  get decisionClass(): string {
    if (this.isFavorable)  return 'decision-favorable';
    if (this.isRejet)      return 'decision-rejet';
    if (this.isComplement) return 'decision-complement';
    return 'decision-default';
  }

  // ── Navigation ─────────────────────────────────────────────────────────────

  next(): void {
    if (this.stepIndex === 1) { this.stepIndex = 2; return; }
    if (this.stepIndex === 2) {
      if (!this.isStep2Valid) return;
      this.stepIndex = 3;
    }
  }

  prev(): void {
    if (this.stepIndex === 2) { this.stepIndex = 1; return; }
    if (this.stepIndex === 3) { this.stepIndex = 2; return; }
  }

  isStepActive(index: number): boolean    { return this.stepIndex === index; }
  isStepCompleted(index: number): boolean { return index < this.stepIndex; }

  // ── Soumission ─────────────────────────────────────────────────────────────

  submit(): void {
    if (!this.isStep2Valid) return;

    this.submitting = true;
    this.submitError = '';

    this.reclamationService.changeStatus(this.refRec, {
      codSta: this.selectedStatutCode,
      commentaire: this.commentaire.trim() || undefined
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.submitSuccess = true;
      },
      error: (err) => {
        this.submitting = false;
        if (err.status === 403) {
          this.submitError = 'Accès non autorisé à cette réclamation.';
        } else if (err.status === 400 && err.error?.message) {
          this.submitError = err.error.message;
        } else {
          // Ne jamais exposer l’erreur technique brute (ex : erreur SMTP)
          this.submitError =
            'Une erreur est survenue lors du traitement. ' +
            'La réclamation a peut-être été mise à jour. Veuillez vérifier la consultation.';
        }
        this.stepIndex = 2;
      }
    });
  }

  goToConsultation(): void {
    this.router.navigate(['/reclamation/consultation']);
  }

  getBadgeClass(libelle: string | undefined): string {
    if (!libelle) return 'badge-default';
    const lib = libelle.toLowerCase();
    if (lib.includes('sms')) return 'badge-sms';
    if (lib.includes('email')) return 'badge-email';
    if (lib.includes('aucun')) return 'badge-none';
    return 'badge-default';
  }
}
