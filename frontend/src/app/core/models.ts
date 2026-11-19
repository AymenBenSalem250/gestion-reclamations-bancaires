// ─────────────────────────────────────────────────────────
// ✅ MODELS CORRIGÉS ALIGNE BACKEND SPRING BOOT
// ─────────────────────────────────────────────────────────

// 🔷 Page Response (Pagination Backend)
export interface Page<T> {
  content: T[];
  pageable: any;
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: any;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// 🔷 Statut
export interface Statut {
  codSta: string;
  libSta: string;
  msgSta?: string;   
  msg2Sta?: string;   

}

// 🔷 Service
export interface Service {
  codSer: string;
  libSer: string;
}

// 🔷 Objet
export interface Objet {
  id: number;
  codObj?: string;
  libObj: string;
  service?: Service;
}

// 🔷 Canal
export interface Canal {
  id: number;
  libCan: string;   // ✅ Correction ici
}

// 🔷 NatureReclamant
export interface NatureReclamant {
  id: number;
  codNat?: string;
  libNat: string;   // ✅ Correction ici
}

// 🔷 NotificationType
export interface NotificationType {
  id: number;
  libelle: string;
  active: boolean;
}

// ─────────────────────────────────────────────────────────
// ✅ RECLAMATION DTO (Aligné avec Backend DTO)
// ─────────────────────────────────────────────────────────

export interface Reclamation {
  refRec: string;
  typeRec: string;

  clientDisplay: string;
  natureDisplay: string;
  canalDisplay: string;
  objetDisplay: string;
  statutDisplay: string;
  notificationTypeDisplay?: string;

  descRec: string;
  compRec?: string;
  datRec: string;
  
  // ✅ IDs nécessaires pour modification
  canalId?: number;
  objetId?: number;
  natRecId?: number;
  notificationTypeIds?: number[];
  emailRec?: string;
  telephone?: string;
}

// ─────────────────────────────────────────────────────────
// ✅ STATISTICS MODEL CORRIGÉ
// ─────────────────────────────────────────────────────────

export interface ReclamationStats {
  total: number;
  nouveau: number;   // ✅ Correction
  encours: number;   // ✅ Correction
  traite: number;    // ✅ Correction
  cloture: number;   // ✅ Correction
}

// ─────────────────────────────────────────────────────────
// ✅ REQUEST TYPES
// ─────────────────────────────────────────────────────────

export interface StatusChangeRequest {
  codSta: string;
  commentaire?: string;
}

export interface ResponseRequest {
  repRec: string;
}