import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';

import { ReferenceService } from '../../core/reference.service';
import { ReclamationService } from '../../core/reclamation.service';
import { Canal, Objet, NatureReclamant, NotificationType } from '../../core/models';

interface ClientResult {
  codeClt: string;
  ugClt: string;
  libCpt: string;
  numCpt: string;
  nomClt: string;
  prenClt: string;
  mobPer: string | null;
  emailPer: string | null;
}

import { PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-saisie',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent],
  templateUrl: './saisie.component.html',
  styleUrl: './saisie.component.css'
})
export class SaisieComponent implements OnInit {

  // ✅ Wizard configuration
  stepIndex = 1;

  steps = [
    { index: 1, label: 'Choix du type client' },
    { index: 2, label: 'Recherche du client' },
    { index: 3, label: "Collecte d'informations" },
    { index: 4, label: 'Traitement de la réclamation' },
    { index: 5, label: 'Vérification' }
  ];

  // ✅ Data
  natures: NatureReclamant[] = [];
  canals: Canal[] = [];
  objets: Objet[] = [];
  notificationTypes: NotificationType[] = [];

  clientResults: ClientResult[] = [];
  searching = false;
  searchError = '';

  submitting = false;
  submitError = '';
  submitSuccess = false;
  generatedRef = '';

  formData: any = {
    clientType: '',
    natureId: null,
    searchQuery: '',
    searchType: 'compte',
    selectedClient: null,

    codeClient: '',
    agence: '',
    libelleClient: '',
    numeroCompte: '',

    telephone: '',
    email: '',
    gsm: '',

    nom: '',
    prenom: '',
    raisonSociale: '',
    cinOuMatricule: '',

    canalId: null,
    objetId: null,
    description: ''
  };

  private readonly clientApiBase = 'http://localhost:8081/api/clients';

  constructor(
    private referenceService: ReferenceService,
    private reclamationService: ReclamationService,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.referenceService.getNatures(0, 1000)
      .subscribe(res => this.natures = res.content);

    this.referenceService.getCanals(0, 1000)
      .subscribe(res => this.canals = res.content);

    this.referenceService.getObjets(0, 1000)
      .subscribe(res => this.objets = res.content);

    this.referenceService.getNotificationTypes(0, 1000)
      .subscribe(res => {
        this.notificationTypes = res.content.filter(nt => nt.active === true);
      });
  }

  // ✅ Multi-selection notification
  selectedNotificationIds: number[] = [];

  toggleNotification(id: number): void {
    if (this.selectedNotificationIds.includes(id)) {
      this.selectedNotificationIds = this.selectedNotificationIds.filter(x => x !== id);
    } else {
      this.selectedNotificationIds = [...this.selectedNotificationIds, id];
    }
  }

  isNotificationSelected(id: number): boolean {
    return this.selectedNotificationIds.includes(id);
  }

  // ✅ Step state
  isStepActive(index: number): boolean {
    return this.stepIndex === index;
  }

  isStepCompleted(index: number): boolean {
    return index < this.stepIndex;
  }

  get selectedCanal(): string { return this.canals.find(c => c.id === this.formData.canalId)?.libCan || '-'; }
  get selectedObjet(): string { return this.objets.find(o => o.id === this.formData.objetId)?.libObj || '-'; }

  get selectedNotificationTypeLabels(): string {
    if (this.selectedNotificationIds.length === 0) return 'Aucun';
    return this.notificationTypes
      .filter(nt => this.selectedNotificationIds.includes(nt.id))
      .map(nt => nt.libelle)
      .join(', ');
  }

  // ✅ Type checks
  get isTSB(): boolean {
    return this.formData.clientType === 'TSB';
  }

  get isNonTSB(): boolean {
    return this.formData.clientType === 'NON_TSB';
  }

  onClientTypeChange() {
    if (this.formData.clientType === 'TSB') {
      this.formData.natureId = null;
    }
  }

  get isEntreprise(): boolean {
    const nature = this.natures.find(n => n.id === this.formData.natureId);
    if (!nature) return false;
    const lib = nature.libNat.toLowerCase();
    return lib.includes('entreprise') || lib.includes('association');
  }

  // ✅ Validation
  get isStep1Valid(): boolean {
    if (this.isTSB) return !!this.formData.clientType;
    return !!this.formData.clientType && this.formData.natureId !== null;
  }

  get isStep2Valid(): boolean {
    return !!this.formData.selectedClient;
  }

  get isStep3Valid(): boolean {
    if (this.isTSB) return true;
    return !!this.formData.nom &&
           this.isValidPhone(this.formData.telephone) &&
           this.isValidEmail(this.formData.email);
  }

  get isStep4Valid(): boolean {
    return this.formData.canalId !== null &&
           this.formData.objetId !== null &&
           this.formData.description.trim().length >= 10;
  }

  get descriptionTooShort(): boolean {
    return this.formData.description.trim().length > 0 &&
           this.formData.description.trim().length < 10;
  }

  get cinError(): string {
    if (!this.formData.cinOuMatricule) return '';
    return this.isValidCin(this.formData.cinOuMatricule)
      ? ''
      : 'Le CIN doit comporter exactement 8 chiffres.';
  }

  get phoneError(): string {
    if (!this.formData.telephone) return '';
    return this.isValidPhone(this.formData.telephone)
      ? ''
      : 'Numéro invalide : 8 chiffres commençant par 2, 5, 7 ou 9.';
  }

  get emailError(): string {
    if (!this.formData.email) return '';
    return this.isValidEmail(this.formData.email)
      ? ''
      : 'Adresse e-mail invalide.';
  }

  private isValidCin(cin: string): boolean {
    return /^\d{8}$/.test(cin.trim());
  }

  private isValidPhone(phone: string): boolean {
    const cleaned = phone.trim();
    return /^[2579]\d{7}$/.test(cleaned);
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }

  // ✅ Navigation
  next(): void {
    if (this.stepIndex === 1) {
      this.stepIndex = this.isTSB ? 2 : 3;
      return;
    }
    this.stepIndex++;
  }

  prev(): void {
    if (this.stepIndex === 3 && this.isNonTSB) {
      this.stepIndex = 1;
      return;
    }
    this.stepIndex--;
  }

  // ✅ Recherche client
  searchClient(): void {
    const query = this.formData.searchQuery.trim();
    if (!query) return;

    this.searching = true;
    this.searchError = '';
    this.clientResults = [];

    let params = new HttpParams();
    params = params.set(
      this.formData.searchType === 'compte' ? 'numCpt' : 'codeClt',
      query
    );

    this.http.get<ClientResult>(
      `${this.clientApiBase}/search`,
      { params }
    ).subscribe({
      next: (data) => {
        if (data) this.clientResults = [data];
        else this.searchError = 'Aucun client trouvé.';
        this.searching = false;
      },
      error: () => {
        this.searchError = 'Erreur lors de la recherche client.';
        this.searching = false;
      }
    });
  }

  chooseClient(client: ClientResult): void {
    this.formData.selectedClient = client;
    this.formData.codeClient = client.codeClt;
    this.formData.agence = client.ugClt;
    this.formData.libelleClient = client.libCpt;
    this.formData.numeroCompte = client.numCpt;
    this.formData.telephone = client.mobPer ?? '';
    this.formData.gsm = client.mobPer ?? '';
    this.formData.email = client.emailPer ?? '';
  }

  submit(): void {

  this.submitting = true;
  this.submitError = '';

  const payload: any = {

    typeRec: this.isTSB ? 'CLIENT' : 'NON_CLIENT',

    // ✅ Champs communs
    descRec: this.formData.description,
    canalId: Number(this.formData.canalId),
    objetId: Number(this.formData.objetId),
    notificationTypeIds: [...this.selectedNotificationIds],
    telephone: this.formData.telephone,
    emailRec: this.formData.email,

    // Initialisation optionnelle
    codeClt: null,
    numCpt: null,
    nomRec: null,
    prenRec: null,
    cin: null,
    matriculeFiscal: null,
    natRecId: null
  };

  if (this.isTSB) {

    // ✅ Client bancaire
    payload.codeClt = this.formData.codeClient;
    payload.numCpt = this.formData.numeroCompte;

  } else {

    // ✅ Non client
    payload.natRecId = this.formData.natureId;
    payload.nomRec = this.formData.nom;
    payload.prenRec = this.formData.prenom;

    // ✅ CIN ou matricule fiscal
    if (this.isEntreprise) {
      payload.matriculeFiscal = this.formData.cinOuMatricule;
    } else {
      payload.cin = this.formData.cinOuMatricule;
    }
  }

  this.reclamationService.create(payload).subscribe({
    next: (rec) => {
      this.submitSuccess = true;
      this.generatedRef = rec.refRec;
      this.submitting = false;
    },
    error: (err) => {
      console.error(err);
      this.submitError = 'Erreur lors de l’enregistrement.';
      this.submitting = false;
    }
  });
}

  goToConsultation(): void {
    this.router.navigate(['/reclamation/consultation']);
  }

  resetForm(): void {
    this.stepIndex = 1;
    this.submitSuccess = false;
    this.generatedRef = '';
  }
}