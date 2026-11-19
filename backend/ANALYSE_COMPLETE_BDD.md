# 📊 Analyse Complète du Modèle de Données - Application Gestion des Réclamations Bancaires (TSB)

**Date d'analyse:** 18 juin 2026  
**Version Spring Boot:** 4.1.0  
**Base de données:** MySQL  
**ORM:** Hibernate JPA 7.x

---

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Inventaire des Entités](#inventaire-des-entités)
3. [Description Détaillée des Tables](#description-détaillée-des-tables)
4. [Relations JPA](#relations-jpa)
5. [Flux Métier Complet](#flux-métier-complet)
6. [Analyse Critique](#analyse-critique)
7. [Propositions d'Amélioration](#propositions-damélioration)
8. [Diagramme UML](#diagramme-uml)

---

## 🎯 Vue d'ensemble

### Contexte Métier
Application de gestion des réclamations bancaires pour TSB (Tunisian Saudi Bank), permettant:
- L'enregistrement et le suivi des réclamations clients
- La gestion des utilisateurs et des droits d'accès
- Le workflow de traitement des réclamations
- La génération de rapports et exports XML réglementaires
- La notification des clients (Email/SMS)

### Architecture Technique
- **21 entités JPA** réparties en 4 domaines fonctionnels
- **14 repositories** pour l'accès aux données
- **4 services métier** avec 50+ méthodes
- **5 contrôleurs REST** exposant 40+ endpoints
- **Sécurité JWT** avec RBAC (Role-Based Access Control)

---

## 📦 Inventaire des Entités

### Classification des 21 Entités

#### 🔐 Domaine Habilitation (Sécurité & Droits) - Préfixe `hab_`
1. **Users** (`hab_users`) - Utilisateurs de l'application
2. **Role** (`hab_role`) - Rôles (ADMIN, AGENT, RESPONSABLE, CLIENT)
3. **Privilege** (`hab_privilege`) - Privilèges d'accès aux menus
4. **Domaine** (`hab_domaine`) - Domaines applicatifs
5. **Module** (`hab_module`) - Modules fonctionnels
6. **Menu** (`hab_menu`) - Menus de navigation

#### 📝 Domaine Réclamation - Préfixe `rec_`
7. **Reclamation** (`rec_reclamation`) - ❤️ **ENTITÉ CENTRALE** - Réclamations
8. **ReclamationGenerated** (`rec_reclamation_gen`) - Traçabilité des exports XML
9. **Statut** (`rec_statut`) - États du workflow (NOUVEAU, EN_COURS, TRAITE, CLOTURE)
10. **Objet** (`rec_objet`) - Objets/motifs des réclamations
11. **Service** (`rec_service`) - Services bancaires concernés
12. **Canal** (`rec_canal`) - Canaux de réception (EMAIL, TELEPHONE, GUICHET)
13. **NatureReclamant** (`rec_nature_client`) - Type de client (Particulier, Entreprise)
14. **ParamGen** (`rec_param_gen`) - Paramètres généraux de configuration
15. **RubriqueXML** (`rec_rubrique_xml`) - Rubriques pour exports XML réglementaires


#### 🔧 Domaine Technique
16. **Path** (`sed_cci_path`) - Chemins d'accès utilisateur/module
17. **ReclamationDt** (⚠️ **NON PERSISTÉE** - POJO XML) - DTO pour exports statistiques

#### ❌ Entités Inutilisées/Problématiques
18. **Person** ⚠️ - POJO non persisté, sans utilité
19. **MapAdapter** ⚠️ - Classe utilitaire JAXB (pas une entité)
20. **NestedMapAdapter** ⚠️ - Classe utilitaire JAXB (pas une entité)

**Entités JPA réelles:** 16 tables en base de données  
**Classes utilitaires/DTO:** 5 classes non persistées

---

## 📋 Description Détaillée des Tables

### 1. 🔐 **USERS** (`hab_users`)

**Rôle Métier:** Gestion des comptes utilisateurs de l'application (employés de la banque, administrateurs)

**Description:** Stocke les informations d'authentification et de profil des utilisateurs pouvant accéder au système.

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | String | Identifiant unique | PK |
| email | String | Email de connexion | UNIQUE, NOT NULL |
| password | String | Mot de passe BCrypt | NOT NULL |
| Apassword | String | Ancien mot de passe (historique) | |
| firstName | String | Prénom | |
| lastName | String | Nom | |
| disabled | Boolean | Compte désactivé | |
| dpwd | Boolean | Mot de passe par défaut | |
| nbcnx | int | Nombre de tentatives de connexion | Max 3 |


**Clés Étrangères:** Aucune (table racine)

**Relations:**
- **ManyToMany** → Role (via `hab_users_roles`)

**Utilité:** Authentification JWT, gestion des droits, traçabilité des actions

---

### 2. 📜 **ROLE** (`hab_role`)

**Rôle Métier:** Définition des rôles applicatifs avec leurs privilèges

**Description:** Table de référence des rôles prédéfinis dans l'application

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | Long | Identifiant technique | PK, SEQUENCE |
| cod_role | String | Code du rôle (ADMIN, AGENT...) | NOT NULL |
| lib_role | String | Libellé du rôle | |

**Clés Étrangères:** Aucune

**Relations:**
- **ManyToMany** ← Users (via `hab_users_roles`)
- **OneToMany** → Privilege

**Valeurs Métier:**
- `ADMIN` - Administrateur système
- `AGENT` - Agent de traitement des réclamations
- `RESPONSABLE` - Manager/Chef de service
- `CLIENT` - Client externe (accès limité)
- `USER` - Utilisateur de base

**Utilité:** Contrôle d'accès RBAC, autorisation des actions

---

### 3. 🔑 **PRIVILEGE** (`hab_privilege`)

**Rôle Métier:** Gestion fine des droits d'accès aux menus par rôle

**Description:** Table de liaison permettant d'assigner des menus spécifiques à chaque rôle

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | Long | Identifiant technique | PK, AUTO |
| cod_role | Long | ID du rôle | FK → Role.id |
| cod_menu | String | Code du menu | FK → Menu.cod_menu |

**Clés Étrangères:**
- `cod_role` → `hab_role.id`
- `cod_menu` → `hab_menu.cod_menu`

**Relations:**
- **ManyToOne** → Role
- **ManyToOne** → Menu

**Utilité:** Affichage dynamique des menus selon le rôle, sécurité de navigation

---

### 4. ❤️ **RECLAMATION** (`rec_reclamation`) - ENTITÉ CENTRALE

**Rôle Métier:** Enregistrement et suivi complet d'une réclamation client

**Description:** Table principale du système contenant toutes les informations d'une réclamation bancaire

**Attributs Principaux:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| ref_rec | String | Référence unique (RECxxxxxxxxxx) | PK |
| typ_rec | String | Type de réclamation | |
| nat_rec | Long | Nature du réclamant | FK → NatureReclamant |
| nom_rec | String | Nom du réclamant | |
| pren_rec | String | Prénom du réclamant | |

| raisoc_rec | String | Raison sociale (si entreprise) | |
| fonct_rec | String | Fonction du réclamant | |
| datnais_rec | Date | Date de naissance | |
| genre_rec | String | Genre (M/F) | |
| typPid_rec | String | Type de pièce d'identité | |
| numpid_rec | String | Numéro pièce d'identité | |
| numcpt_rec | String | Numéro de compte bancaire | |
| lib_cpt_rec | String | Libellé du compte | |
| email_rec | String | Email du réclamant | |
| gsm_rec | Long | Numéro de téléphone | |
| dat_rec | Date | Date de la réclamation | NOT NULL |
| id_objRec | Long | Objet de la réclamation | FK → Objet |
| id_canal | Long | Canal de réception | FK → Canal |
| sta_rec | String | Statut de la réclamation | FK → Statut |
| desc_rec | String | Description détaillée | |
| obs_rec | String | Observations | |
| comp_rec | String | Complément/Réponse | |
| dat_rep | Date | Date de réponse | |
| notified_email | Boolean | Client notifié par email | Default: false |
| notified_sms | Boolean | Client notifié par SMS | Default: false |
| xml_gen | Boolean | XML généré pour BCT | Default: false |
| dat_gen_xml | Date | Date génération XML | |
| user_gen_xml | String | Utilisateur ayant généré XML | |
| user_cre | Long | Utilisateur créateur | |
| user_mod | Long | Utilisateur modificateur | |
| dat_cre | Date | Date de création | |
| dat_mod | Date | Date de modification | |

**Clés Étrangères:**
- `nat_rec` → `rec_nature_client.id`
- `id_objRec` → `rec_objet.id`
- `id_canal` → `rec_canal.id`
- `sta_rec` → `rec_statut.cod_sta`


**Relations:**
- **ManyToOne** → NatureReclamant
- **ManyToOne** → Objet
- **ManyToOne** → Canal
- **ManyToOne** → Statut

**Utilité:** Cœur du système, contient toutes les données client et le workflow complet

---

### 5. 📊 **STATUT** (`rec_statut`)

**Rôle Métier:** Table de référence des états du workflow de traitement

**Description:** États possibles d'une réclamation dans son cycle de vie

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| cod_sta | String | Code du statut | PK |
| lib_sta | String | Libellé du statut | |
| msg_sta | String | Message de notification | |
| msg2_sta | String | Message secondaire | |

**Valeurs Métier:**
- `NOUVEAU` - Réclamation créée, en attente d'affectation
- `EN_COURS` - Réclamation affectée à un agent, en traitement
- `TRAITE` - Traitement terminé, réponse fournie
- `CLOTURE` - Réclamation clôturée définitivement
- `ANNULE` - Réclamation annulée (doublon, erreur)

**Utilité:** Suivi du workflow, filtrage, statistiques

---

### 6. 🏢 **SERVICE** (`rec_service`)

**Rôle Métier:** Services/Départements de la banque

**Description:** Référence des services bancaires responsables du traitement

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| cod_ser | String | Code du service | PK |
| lib_ser | String | Libellé du service | |
| email1_ser | String | Email principal du service | |
| email2_ser | String | Email secondaire | |

**Exemples:**
- `IT` - Service informatique
- `RH` - Ressources humaines
- `FINANCE` - Service financier
- `CREDIT` - Service des crédits
- `COMPTE` - Service des comptes

**Utilité:** Routage des réclamations, notification des services concernés

---

### 7. 📌 **OBJET** (`rec_objet`)

**Rôle Métier:** Motifs/objets des réclamations

**Description:** Catégorisation des types de réclamations par service

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | Long | Identifiant technique | PK, SEQUENCE |
| cod_obj | String | Code de l'objet | |
| lib_obj | String | Libellé de l'objet | |
| id_cod_ser | String | Service responsable | FK → Service |


**Clés Étrangères:**
- `id_cod_ser` → `rec_service.cod_ser`

**Relations:**
- **ManyToOne** → Service
- **OneToMany** ← Reclamation

**Exemples:**
- `OBJ001` - Problème de carte bancaire
- `OBJ002` - Erreur de facturation
- `OBJ003` - Retard de virement

**Utilité:** Classification des réclamations, routage automatique vers les services

---

### 8. 📞 **CANAL** (`rec_canal`)

**Rôle Métier:** Canaux de réception des réclamations

**Description:** Moyens par lesquels les clients soumettent leurs réclamations

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | Long | Identifiant technique | PK, SEQUENCE |
| lib_can | String | Libellé du canal | |

**Valeurs Métier:**
- `EMAIL` - Réclamation par email
- `TELEPHONE` - Réclamation par téléphone/call center
- `GUICHET` - Réclamation en agence
- `COURRIER` - Réclamation par courrier postal
- `APPLICATION` - Via l'application mobile

**Utilité:** Statistiques par canal, amélioration du service client

---

### 9. 👤 **NATURE_RECLAMANT** (`rec_nature_client`)

**Rôle Métier:** Type de client réclamant

**Description:** Classification du type de réclamant pour traitement différencié

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | Long | Identifiant technique | PK, SEQUENCE |
| cod_nat | String | Code de la nature | |
| lib_nat | String | Libellé | |

**Valeurs Métier:**
- `PART` - Particulier (personne physique)
- `ENTR` - Entreprise (personne morale)
- `ASSO` - Association
- `COLL` - Collectivité

**Utilité:** Traitement adapté selon le type de client, statistiques

---

### 10. 📄 **RECLAMATION_GENERATED** (`rec_reclamation_gen`)

**Rôle Métier:** Traçabilité des exports XML réglementaires

**Description:** Journal des réclamations exportées vers la Banque Centrale de Tunisie (BCT)

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| ref_rec | String | Référence de la réclamation | PK, FK → Reclamation |
| dat_gen_xml | Date | Date de génération du XML | |

**Clés Étrangères:**
- `ref_rec` → `rec_reclamation.ref_rec`

**Utilité:** Conformité réglementaire, éviter les doublons d'export, audit

---

### 11. ⚙️ **PARAM_GEN** (`rec_param_gen`)

**Rôle Métier:** Paramètres de configuration globaux

**Description:** Table de paramétrage de l'application (clé-valeur)

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | Long | Identifiant technique | PK, SEQUENCE |
| lib_param | String | Nom du paramètre | |
| val_param | String | Valeur du paramètre | |

**Exemples:**
- `AGE_JEUNE` = `30` (limite d'âge pour statistiques)
- `AGE_MIDDLE` = `50`
- `DELAI_REPONSE` = `15` (jours)
- `EMAIL_EXPEDITEUR` = `reclamations@tsb.tn`

**Utilité:** Configuration dynamique sans redéploiement

---

### 12. 📋 **RUBRIQUE_XML** (`rec_rubrique_xml`)

**Rôle Métier:** Métadonnées pour export XML réglementaire BCT

**Description:** Définition des rubriques/sections du fichier XML à envoyer à la BCT

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| cod_rub | String | Code de la rubrique | PK |
| lib_rub | String | Libellé de la rubrique | |
| abrv_rub | String | Abréviation | |

**Utilité:** Structure des exports XML, conformité format BCT

---

### 13. 🌐 **DOMAINE** (`hab_domaine`)

**Rôle Métier:** Domaines applicatifs de haut niveau

**Description:** Regroupement logique de modules (ex: Habilitation, Réclamation, Reporting)

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| cod_dom | String | Code du domaine | PK |
| lib_mod | String | Libellé du domaine | |

**Exemples:**
- `HAB` - Habilitation (gestion des droits)
- `REC` - Réclamation (cœur métier)
- `RPT` - Reporting (statistiques)

**Utilité:** Organisation de l'architecture applicative

---

### 14. 📦 **MODULE** (`hab_module`)

**Rôle Métier:** Modules fonctionnels de l'application

**Description:** Groupes de fonctionnalités appartenant à un domaine

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| cod_mod | String | Code du module | PK |
| lib_mod | String | Libellé du module | |
| sta_mod | String | Statut (actif/inactif) | |
| cod_dom | String | Domaine parent | FK → Domaine |

**Clés Étrangères:**
- `cod_dom` → `hab_domaine.cod_dom`

**Relations:**
- **ManyToOne** → Domaine
- **OneToMany** → Menu

**Exemples:**
- `HAB-USR` - Gestion utilisateurs
- `REC-TRT` - Traitement réclamations
- `RPT-STAT` - Statistiques

**Utilité:** Structuration de l'application, gestion des menus

---

### 15. 🗂️ **MENU** (`hab_menu`)

**Rôle Métier:** Menus de navigation de l'interface

**Description:** Éléments du menu applicatif accessibles selon les privilèges

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| cod_menu | String | Code du menu | PK |
| lib_menu | String | Libellé du menu | |
| codmod | String | Module parent | FK → Module |

**Clés Étrangères:**
- `codmod` → `hab_module.cod_mod`

**Relations:**
- **ManyToOne** → Module
- **OneToMany** ← Privilege

**Exemples:**
- `MENU-REC-LIST` - Liste des réclamations
- `MENU-USR-GEST` - Gestion utilisateurs
- `MENU-RPT-STAT` - Statistiques

**Utilité:** Affichage dynamique des menus selon les droits utilisateur

---

### 16. 🛤️ **PATH** (`sed_cci_path`)

**Rôle Métier:** Chemins d'accès personnalisés par utilisateur/module

**Description:** Stockage des chemins de fichiers ou URLs personnalisés

**Attributs:**
| Colonne | Type | Description | Contrainte |
|---------|------|-------------|------------|
| id | Long | Identifiant technique | PK, SEQUENCE |
| path | String | Chemin (URL/file path) | |
| typ_path | String | Type de chemin | |
| id_user | String | Utilisateur | FK → Users |
| cod_mod | String | Module | FK → Module |


**Clés Étrangères:**
- `id_user` → `hab_users.id`
- `cod_mod` → `hab_module.cod_mod`

**Relations:**
- **ManyToOne** → Users
- **ManyToOne** → Module

**Utilité:** Personnalisation des chemins d'export/import par utilisateur

---

## 🔗 Relations JPA

### Relations ManyToMany (2)

#### 1. Users ↔ Role (`hab_users_roles`)
```
Users (1) ←→ (*) hab_users_roles (*) ←→ (1) Role
```
**Type:** Bidirectionnelle  
**Table de liaison:** `hab_users_roles`  
**Colonnes:**
- `user_id` → `hab_users.id`
- `role_id` → `hab_role.id`

**Justification:** Un utilisateur peut avoir plusieurs rôles, un rôle est attribué à plusieurs utilisateurs

---

### Relations ManyToOne (10)

#### 1. Reclamation → NatureReclamant
```
Reclamation (*) → (1) NatureReclamant
```
**Colonne:** `nat_rec` → `rec_nature_client.id`  
**Obligatoire:** OUI (`optional = false`)

#### 2. Reclamation → Objet
```
Reclamation (*) → (1) Objet
```
**Colonne:** `id_objRec` → `rec_objet.id`  
**Obligatoire:** OUI


#### 3. Reclamation → Canal
```
Reclamation (*) → (1) Canal
```
**Colonne:** `id_canal` → `rec_canal.id`  
**Obligatoire:** OUI

#### 4. Reclamation → Statut
```
Reclamation (*) → (1) Statut
```
**Colonne:** `sta_rec` → `rec_statut.cod_sta`  
**Obligatoire:** OUI

#### 5. Objet → Service
```
Objet (*) → (1) Service
```
**Colonne:** `id_cod_ser` → `rec_service.cod_ser`  
**Obligatoire:** OUI

#### 6. Privilege → Role
```
Privilege (*) → (1) Role
```
**Colonne:** `cod_role` → `hab_role.id`  
**Obligatoire:** OUI

#### 7. Privilege → Menu
```
Privilege (*) → (1) Menu
```
**Colonne:** `cod_menu` → `hab_menu.cod_menu`  
**Obligatoire:** OUI

#### 8. Module → Domaine
```
Module (*) → (1) Domaine
```
**Colonne:** `cod_dom` → `hab_domaine.cod_dom`  
**Obligatoire:** OUI

#### 9. Menu → Module
```
Menu (*) → (1) Module
```
**Colonne:** `codmod` → `hab_module.cod_mod`  
**Obligatoire:** OUI

#### 10. Path → Users & Module
```
Path (*) → (1) Users
Path (*) → (1) Module
```
**Colonnes:** `id_user` → `hab_users.id`, `cod_mod` → `hab_module.cod_mod`  
**Obligatoire:** OUI pour les deux


---

### Relations OneToMany (3)

#### 1. Role → Privilege
```
Role (1) → (*) Privilege
```
**Mapping:** `@OneToMany(mappedBy = "role")`  
**Fetch:** EAGER  
**Cascade:** ALL

#### 2. Module → Menu
```
Module (1) → (*) Menu
```
**Mapping:** `@OneToMany(mappedBy = "module")`  
**Fetch:** LAZY

#### 3. Service → Objet (implicite)
```
Service (1) → (*) Objet
```
Relation implicite via ManyToOne côté Objet

---

## 📊 Tableau de Synthèse

| # | Table | Préfixe | Rôle Métier | Relations | Utilité | Statut |
|---|-------|---------|-------------|-----------|---------|--------|
| 1 | **Users** | `hab_` | Comptes utilisateurs | ManyToMany → Role | Authentification JWT | ✅ Essentielle |
| 2 | **Role** | `hab_` | Rôles applicatifs | ManyToMany ← Users, OneToMany → Privilege | RBAC | ✅ Essentielle |
| 3 | **Privilege** | `hab_` | Droits d'accès menus | ManyToOne → Role, Menu | Sécurité navigation | ✅ Importante |
| 4 | **Reclamation** | `rec_` | ❤️ Réclamations clients | 4x ManyToOne | Cœur métier | ✅ CENTRALE |
| 5 | **Statut** | `rec_` | États workflow | OneToMany ← Reclamation | Suivi traitement | ✅ Essentielle |
| 6 | **Service** | `rec_` | Départements banque | OneToMany → Objet | Routage | ✅ Importante |

| 7 | **Objet** | `rec_` | Motifs réclamations | ManyToOne → Service | Classification | ✅ Essentielle |
| 8 | **Canal** | `rec_` | Canaux réception | OneToMany ← Reclamation | Statistiques | ✅ Importante |
| 9 | **NatureReclamant** | `rec_` | Type de client | OneToMany ← Reclamation | Traitement adapté | ✅ Importante |
| 10 | **ReclamationGenerated** | `rec_` | Journal exports XML | Lien → Reclamation | Conformité BCT | ✅ Importante |
| 11 | **ParamGen** | `rec_` | Paramètres config | Aucune | Configuration | ✅ Utile |
| 12 | **RubriqueXML** | `rec_` | Structure XML BCT | Aucune | Export réglementaire | ✅ Utile |
| 13 | **Domaine** | `hab_` | Domaines applicatifs | OneToMany → Module | Architecture | ⚠️ Peu utilisée |
| 14 | **Module** | `hab_` | Modules fonctionnels | ManyToOne → Domaine, OneToMany → Menu | Organisation | ✅ Importante |
| 15 | **Menu** | `hab_` | Menus navigation | ManyToOne → Module, OneToMany ← Privilege | Interface | ✅ Importante |
| 16 | **Path** | `sed_` | Chemins personnalisés | ManyToOne → Users, Module | Personnalisation | ⚠️ Utilité limitée |
| 17 | **ReclamationDt** | - | DTO statistiques | Aucune (POJO) | Export XML stats | ✅ Utile (non JPA) |
| 18 | **Person** | - | - | Aucune | - | ❌ INUTILE |
| 19 | **MapAdapter** | - | Utilitaire JAXB | Aucune | Sérialisation XML | ✅ Technique |
| 20 | **NestedMapAdapter** | - | Utilitaire JAXB | Aucune | Sérialisation XML | ✅ Technique |

**Total:** 16 tables JPA + 4 classes utilitaires/DTO

---

## 🔄 Flux Métier Complet

### 1️⃣ Authentification des Utilisateurs

**Tables Impliquées:** Users, Role, Privilege, Menu

**Processus:**
1. Utilisateur soumet email + password via `/api/auth/signin`
2. `UserDetailsServiceImpl` charge User depuis `hab_users`
3. Vérification BCrypt du password
4. Génération JWT contenant email + roles (via `JwtUtils`)
5. Token renvoyé au client (validité: 24h)
6. Client inclut token dans `Authorization: Bearer {token}` pour toutes les requêtes
7. `AuthTokenFilter` valide le token et charge le contexte Spring Security
8. Accès autorisé si role requis présent

**SQL:**
```sql
-- 1. Charger utilisateur
SELECT u.*, r.cod_role, r.lib_role 
FROM hab_users u
LEFT JOIN hab_users_roles ur ON u.id = ur.user_id
LEFT JOIN hab_role r ON ur.role_id = r.id
WHERE u.email = ?;

-- 2. Charger privilèges (menus accessibles)
SELECT m.cod_menu, m.lib_menu, md.lib_mod
FROM hab_privilege p
JOIN hab_menu m ON p.cod_menu = m.cod_menu
JOIN hab_module md ON m.codmod = md.cod_mod
WHERE p.cod_role IN (SELECT role_id FROM hab_users_roles WHERE user_id = ?);
```

---

### 2️⃣ Création d'une Réclamation

**Tables Impliquées:** Reclamation, NatureReclamant, Objet, Service, Canal, Statut

**Processus:**
1. Client/Agent appelle `POST /api/reclamations` avec données JSON
2. `ReclamationService.createReclamation()` appelé
3. Génération référence unique: `RECxxxxxxxxxx` (timestamp + random)
4. Chargement des entités liées:
   - `NatureReclamant` (PART/ENTR)
   - `Objet` (ex: OBJ001 - Problème carte)
   - `Canal` (EMAIL/TELEPHONE/GUICHET)
5. Attribution statut par défaut: `NOUVEAU`
6. Remplissage métadonnées:
   - `dat_cre` = now()
   - `dat_rec` = now()
   - `user_cre` = ID utilisateur connecté
   - `xml_generated` = false
   - `notified_email` = false
   - `notified_sms` = false
7. Sauvegarde en base via `ReclamationRepository`
8. Retour de l'objet Reclamation avec ref_rec générée

**SQL:**
```sql
-- Insertion réclamation
INSERT INTO rec_reclamation (
  ref_rec, nom_rec, pren_rec, email_rec, gsm_rec,
  desc_rec, nat_rec, id_objRec, id_canal, sta_rec,
  dat_rec, dat_cre, user_cre, xml_gen, notified_email, notified_sms
) VALUES (
  'REC1718743200123', 'Dupont', 'Jean', 'jean@mail.com', 21612345678,
  'Ma carte ne fonctionne plus', 1, 5, 2, 'NOUVEAU',
  NOW(), NOW(), 1, false, false, false
);
```

---

### 3️⃣ Affectation à un Agent

**Tables Impliquées:** Reclamation, Users, Role, Objet, Service

**Processus:**
1. Responsable/Admin consulte réclamations `NOUVEAU` via `GET /api/reclamations/status/NOUVEAU`
2. Système affiche réclamations avec objet → service
3. Responsable sélectionne une réclamation et un agent du service concerné
4. Appel `PUT /api/reclamations/{refRec}/assign?agentId=xxx` (endpoint à créer)
5. Mise à jour:
   - Ajout champ `agent_id` (à créer) → FK vers `hab_users.id`
   - Changement statut: `NOUVEAU` → `EN_COURS`
   - `dat_mod` = now()
   - `user_mod` = ID responsable

**SQL:**
```sql
-- Récupérer agents du service IT
SELECT u.id, u.firstName, u.lastName, u.email
FROM hab_users u
JOIN hab_users_roles ur ON u.id = ur.user_id
JOIN hab_role r ON ur.role_id = r.id
WHERE r.cod_role = 'AGENT'
AND u.disabled = false;

-- Affecter réclamation (requiert ajout colonne agent_id)
UPDATE rec_reclamation
SET agent_id = ?, sta_rec = 'EN_COURS', dat_mod = NOW(), user_mod = ?
WHERE ref_rec = ?;
```

⚠️ **ATTENTION:** La table `Reclamation` ne possède pas de colonne `agent_id` actuellement. C'est une amélioration à apporter.

---

### 4️⃣ Traitement de la Réclamation

**Tables Impliquées:** Reclamation, Statut

**Processus:**
1. Agent consulte ses réclamations via `GET /api/reclamations?agent=xxx&status=EN_COURS`
2. Agent ouvre une réclamation et analyse le problème
3. Agent saisit une réponse via `POST /api/reclamations/{refRec}/response`
4. `ReclamationService.addResponse()` appelé
5. Mise à jour:
   - `comp_rec` = texte de la réponse
   - `dat_rep` = now()
   - `sta_rec` = `TRAITE`
6. Notification client (si configuré):
   - Email envoyé via service de notification
   - `notified_email` = true
   - SMS si numéro présent
   - `notified_sms` = true

**SQL:**
```sql
-- Ajouter réponse
UPDATE rec_reclamation
SET comp_rec = ?, dat_rep = NOW(), sta_rec = 'TRAITE'
WHERE ref_rec = ?;

-- Marquer comme notifié
UPDATE rec_reclamation
SET notified_email = true, notified_sms = true
WHERE ref_rec = ?;
```

---

### 5️⃣ Suivi du Statut

**Tables Impliquées:** Reclamation, Statut

**Processus:**
1. Client/Agent consulte une réclamation via `GET /api/reclamations/{refRec}`
2. Système affiche:
   - Informations client
   - Description problème (`desc_rec`)
   - Statut actuel (`sta_rec` → `Statut.lib_sta`)
   - Dates clés: `dat_rec`, `dat_rep`
   - Réponse si disponible (`comp_rec`)
3. Historique des changements de statut (nécessite table d'audit - à créer)

---

### 6️⃣ Clôture de la Réclamation

**Tables Impliquées:** Reclamation, Statut

**Processus:**
1. Après délai satisfaction client (ex: 7 jours après `TRAITE`)
2. Responsable/Système appelle `PUT /api/reclamations/{refRec}/status?codSta=CLOTURE`
3. `ReclamationService.updateStatus()` appelé
4. Mise à jour:
   - `sta_rec` = `CLOTURE`
   - `dat_mod` = now()
5. Réclamation archivée (non supprimée, pour historique)

**SQL:**
```sql
-- Clôturer réclamation
UPDATE rec_reclamation
SET sta_rec = 'CLOTURE', dat_mod = NOW(), user_mod = ?
WHERE ref_rec = ?;
```

---

### 7️⃣ Génération XML Réglementaire (BCT)

**Tables Impliquées:** Reclamation, ReclamationGenerated, RubriqueXML, ReclamationDt (DTO), ParamGen

**Processus:**
1. Responsable lance export via `POST /api/reclamations/export-xml?startDate=...&endDate=...`
2. Service récupère réclamations `CLOTURE` non exportées (`xml_generated = false`)
3. Chargement paramètres depuis `ParamGen` (AGE_JEUNE, AGE_MIDDLE...)
4. Construction `ReclamationDt` avec statistiques:
   - Réclamations par nature (PART/ENTR)
   - Réclamations par canal
   - Répartition genre/âge
5. Génération fichier XML via JAXB (`MapAdapter`, `NestedMapAdapter`)
6. Sauvegarde fichier: `/exports/reclamations_YYYYMMDD.xml`
7. Marquage réclamations:
   - `xml_generated` = true
   - `dat_gen_xml` = now()
   - `user_gen_xml` = ID utilisateur

8. Enregistrement dans `ReclamationGenerated` pour chaque réclamation exportée

**SQL:**
```sql
-- Récupérer réclamations à exporter
SELECT * FROM rec_reclamation
WHERE sta_rec = 'CLOTURE'
AND xml_generated = false
AND dat_rec BETWEEN ? AND ?;

-- Marquer comme exporté
UPDATE rec_reclamation
SET xml_generated = true, dat_gen_xml = NOW(), user_gen_xml = ?
WHERE ref_rec = ?;

-- Enregistrer dans journal
INSERT INTO rec_reclamation_gen (ref_rec, dat_gen_xml)
VALUES (?, NOW());
```

---

## 🔍 Analyse Critique

### ✅ Points Forts

1. **Séparation claire des domaines** (hab_, rec_, sed_)
2. **Entité centrale Reclamation bien conçue** avec toutes les infos nécessaires
3. **Workflow statut bien modélisé** (NOUVEAU → EN_COURS → TRAITE → CLOTURE)
4. **Relations JPA cohérentes** et bien définies
5. **Traçabilité** via user_cre, user_mod, dat_cre, dat_mod
6. **Conformité réglementaire** (export XML BCT)
7. **Sécurité RBAC** bien implémentée (Users, Role, Privilege)
8. **Notifications trackées** (notified_email, notified_sms)

---

### ⚠️ Points Faibles & Problèmes Identifiés

#### 1. **Absence de colonne `agent_id` dans Reclamation**
**Problème:** Impossible de savoir quel agent traite une réclamation  
**Impact:** Pas de suivi de charge de travail, pas de responsabilisation  
**Solution:** Ajouter `agent_id` FK vers `hab_users.id`


#### 2. **Absence d'historique des changements de statut**
**Problème:** On ne sait pas quand le statut a changé, combien de temps dans chaque état  
**Impact:** Impossible de calculer les délais de traitement réels, pas d'audit  
**Solution:** Créer table `rec_statut_history` (ref_rec, old_status, new_status, dat_change, user_change)

#### 3. **Table `Person` totalement inutile**
**Problème:** Classe POJO non annotée @Entity, non utilisée dans le code  
**Solution:** **SUPPRIMER** cette classe

#### 4. **Table `Path` peu utilisée**
**Problème:** Utilité limitée, préfixe `sed_` incohérent  
**Impact:** Confusion dans l'architecture  
**Solution:** Documenter son usage OU supprimer si inutilisée

#### 5. **Absence de table `Service` lié à `Reclamation`**
**Problème:** Reclamation → Objet → Service (indirect)  
**Impact:** Requêtes complexes pour filtrer par service  
**Solution:** Ajouter `service_id` directement dans Reclamation (dénormalisation)

#### 6. **userCre/userMod stockés comme Long**
**Problème:** Pas de FK vers `Users`, juste un nombre  
**Impact:** Pas d'intégrité référentielle, impossible de joindre  
**Solution:** Remplacer par `@ManyToOne` vers Users

#### 7. **Pas de champ `priorite` dans Reclamation**
**Problème:** Toutes les réclamations traitées dans l'ordre d'arrivée  
**Impact:** Réclamations urgentes pas priorisées  
**Solution:** Ajouter `priorite` (BASSE, NORMALE, HAUTE, URGENTE)


#### 8. **Pas de système de pièces jointes**
**Problème:** Client ne peut pas joindre de documents (screenshot, facture...)  
**Impact:** Agent doit demander documents par email séparément  
**Solution:** Créer table `rec_attachment` (id, ref_rec, file_name, file_path, mime_type, dat_upload)

#### 9. **Table `Domaine` peu exploitée**
**Problème:** Utilisée uniquement pour regrouper modules  
**Impact:** Couche d'abstraction supplémentaire sans valeur ajoutée  
**Solution:** Conserver si frontend l'utilise, sinon simplifier

#### 10. **Pas de soft delete**
**Problème:** Suppression définitive via `DELETE`  
**Impact:** Perte de données, pas d'historique  
**Solution:** Ajouter `deleted` boolean + `dat_delete` dans toutes les tables importantes

---

## 🚀 Propositions d'Amélioration

### 🔧 Améliorations Prioritaires (Must Have)

#### 1. Ajouter colonne `agent_id` dans Reclamation
```sql
ALTER TABLE rec_reclamation
ADD COLUMN agent_id VARCHAR(255),
ADD CONSTRAINT fk_reclamation_agent
FOREIGN KEY (agent_id) REFERENCES hab_users(id);
```

**Modification JPA:**
```java
@ManyToOne(optional = true)
@JoinColumn(name = "agent_id", referencedColumnName = "id")
private Users agent;
```

---

#### 2. Créer table historique des statuts
```sql
CREATE TABLE rec_statut_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    dat_change DATETIME NOT NULL,
    user_change VARCHAR(255),
    comment TEXT,
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec),
    FOREIGN KEY (old_status) REFERENCES rec_statut(cod_sta),
    FOREIGN KEY (new_status) REFERENCES rec_statut(cod_sta),
    FOREIGN KEY (user_change) REFERENCES hab_users(id)
);
```

**Entité JPA:**
```java
@Entity
@Table(name = "rec_statut_history")
public class StatutHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ref_rec")
    private Reclamation reclamation;
    
    @ManyToOne
    @JoinColumn(name = "old_status")
    private Statut oldStatus;
    
    @ManyToOne
    @JoinColumn(name = "new_status")
    private Statut newStatus;
    
    @Column(name = "dat_change")
    private Date dateChange;
    
    @ManyToOne
    @JoinColumn(name = "user_change")
    private Users userChange;
    
    private String comment;
}
```

---

#### 3. Remplacer userCre/userMod par relations JPA
```java
// Dans Reclamation.java
@ManyToOne(optional = true)
@JoinColumn(name = "user_cre", referencedColumnName = "id")
private Users createur;

@ManyToOne(optional = true)
@JoinColumn(name = "user_mod", referencedColumnName = "id")
private Users modificateur;
```

#### 4. Ajouter priorité dans Reclamation
```sql
ALTER TABLE rec_reclamation
ADD COLUMN priorite VARCHAR(20) DEFAULT 'NORMALE';
```

**Enum Java:**
```java
public enum Priorite {
    BASSE,
    NORMALE,
    HAUTE,
    URGENTE
}
```

#### 5. Créer table pièces jointes
```sql
CREATE TABLE rec_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    dat_upload DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_upload VARCHAR(255),
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec),
    FOREIGN KEY (user_upload) REFERENCES hab_users(id)
);
```

---

### 🔄 Améliorations Secondaires (Nice to Have)

#### 6. Ajouter soft delete
```sql
-- Pour toutes les tables importantes
ALTER TABLE rec_reclamation ADD COLUMN deleted BOOLEAN DEFAULT false;
ALTER TABLE rec_reclamation ADD COLUMN dat_delete DATETIME;
ALTER TABLE hab_users ADD COLUMN deleted BOOLEAN DEFAULT false;
```

#### 7. Ajouter table commentaires/notes internes
```sql
CREATE TABLE rec_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    comment_text TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT true,
    dat_comment DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_comment VARCHAR(255),
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec),
    FOREIGN KEY (user_comment) REFERENCES hab_users(id)
);
```

#### 8. Ajouter SLA (Service Level Agreement)
```sql
ALTER TABLE rec_objet ADD COLUMN sla_hours INT DEFAULT 48;
ALTER TABLE rec_reclamation ADD COLUMN sla_deadline DATETIME;
```

#### 9. Supprimer entités inutiles
- **Person.java** → À SUPPRIMER
- **Path** → Documenter OU supprimer si non utilisée

---


## 🎨 Diagramme UML Simplifié

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DOMAINE HABILITATION (hab_)                          │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │   Domaine    │
                    │  [hab_domaine]│
                    │──────────────│
                    │ cod_dom (PK) │
                    │ lib_mod      │
                    └──────┬───────┘
                           │ 1
                           │
                           │ *
                    ┌──────▼───────┐
                    │    Module    │
                    │ [hab_module] │
                    │──────────────│
                    │ cod_mod (PK) │
                    │ cod_dom (FK) │
                    │ lib_mod      │
                    │ sta_mod      │
                    └──────┬───────┘
                           │ 1
                           │
                           │ *
                    ┌──────▼───────┐
                    │     Menu     │
                    │  [hab_menu]  │
                    │──────────────│
                    │ cod_menu(PK) │
                    │ codmod (FK)  │
                    │ lib_menu     │
                    └──────┬───────┘
                           │
                           │ *
                           │
    ┌──────────────┐       │       ┌──────────────┐
    │     Role     │       │       │   Privilege  │
    │  [hab_role]  │───────┼──────▶│[hab_privilege]│
    │──────────────│   1   │   *   │──────────────│
    │ id (PK)      │───────┼──────▶│ id (PK)      │
    │ cod_role     │       │       │ cod_role(FK) │
    │ lib_role     │       │       │ cod_menu(FK) │
    └──────┬───────┘       │       └──────────────┘
           │               │
           │ *             │
           │               │
    ┌──────▼────────────┐  │
    │ hab_users_roles   │  │
    │ [Table liaison]   │  │
    │───────────────────│  │
    │ user_id (FK)      │  │
    │ role_id (FK)      │  │
    └──────┬────────────┘  │
           │ *             │
           │               │
           │ 1             │
    ┌──────▼───────┐       │
    │     Users    │       │
    │  [hab_users] │       │
    │──────────────│       │
    │ id (PK)      │       │
    │ email        │───────┘
    │ password     │   * (via Path)
    │ firstName    │
    │ lastName     │
    │ disabled     │
    │ nbcnx        │
    └──────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        DOMAINE RÉCLAMATION (rec_)                            │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌─────────────────┐         ┌──────────────────┐
    │ NatureReclamant │         │      Canal       │
    │[rec_nature_cli] │         │   [rec_canal]    │
    │─────────────────│         │──────────────────│
    │ id (PK)         │         │ id (PK)          │
    │ cod_nat         │         │ lib_can          │
    │ lib_nat         │         └─────────┬────────┘
    └────────┬────────┘                   │
             │ 1                           │ 1
             │                             │
             │ *                           │ *
    ┌────────▼────────────────────────────▼─────────────┐
    │              ❤️ RECLAMATION (CENTRALE)            │
    │                 [rec_reclamation]                 │
    │───────────────────────────────────────────────────│
    │ ref_rec (PK)          │ obs_rec                   │
    │ typ_rec               │ comp_rec (réponse)        │
    │ nat_rec (FK)          │ dat_rep                   │
    │ nom_rec, pren_rec     │ notified_email            │
    │ email_rec, gsm_rec    │ notified_sms              │
    │ datnais_rec, genre_rec│ xml_generated             │
    │ numcpt_rec            │ dat_gen_xml               │
    │ desc_rec              │ user_gen_xml              │
    │ dat_rec               │ dat_cre, dat_mod          │
    │ id_objRec (FK)        │ user_cre, user_mod        │
    │ id_canal (FK)         │ [MANQUE: agent_id]        │
    │ sta_rec (FK)          │                           │
    └───────┬───────────────┬───────────────┬───────────┘
            │ *             │ *             │ *
            │               │               │
            │ 1             │ 1             │ 1
    ┌───────▼──────┐  ┌─────▼──────┐  ┌────▼──────┐
    │     Objet    │  │   Statut   │  │ RecGen    │
    │  [rec_objet] │  │[rec_statut]│  │[rec_rec_  │
    │──────────────│  │────────────│  │ gen]      │
    │ id (PK)      │  │ cod_sta(PK)│  │───────────│
    │ cod_obj      │  │ lib_sta    │  │ref_rec(PK)│
    │ lib_obj      │  │ msg_sta    │  │dat_gen_xml│
    │ id_cod_ser(FK)  └────────────┘  └───────────┘
    └───────┬──────┘
            │ *
            │
            │ 1
    ┌───────▼──────┐
    │    Service   │
    │ [rec_service]│
    │──────────────│
    │ cod_ser (PK) │
    │ lib_ser      │
    │ email1_ser   │
    │ email2_ser   │
    └──────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      TABLES CONFIGURATION & REPORTING                        │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐       ┌──────────────┐
    │  ParamGen    │       │ RubriqueXML  │
    │[rec_param_gen│       │[rec_rubrique_│
    │──────────────│       │     xml]     │
    │ id (PK)      │       │──────────────│
    │ lib_param    │       │ cod_rub (PK) │
    │ val_param    │       │ lib_rub      │
    └──────────────┘       │ abrv_rub     │
                           └──────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      CLASSES NON-PERSISTÉES / UTILITAIRES                    │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐       ┌──────────────┐       ┌──────────────┐
    │ReclamationDt │       │  MapAdapter  │       │NestedMap     │
    │   (DTO)      │       │   (JAXB)     │       │  Adapter     │
    │──────────────│       │──────────────│       │──────────────│
    │ Non JPA      │       │ Utilitaire   │       │ Utilitaire   │
    │ Export XML   │       │ XML sérialis.│       │ XML sérialis.│
    └──────────────┘       └──────────────┘       └──────────────┘

    ┌──────────────┐
    │    Person    │ ❌ À SUPPRIMER (inutile, non utilisée)
    │──────────────│
    │ Non JPA      │
    │ POJO vide    │
    └──────────────┘

    ┌──────────────┐ ⚠️ Utilité limitée
    │     Path     │
    │ [sed_cci_path│
    │──────────────│
    │ id (PK)      │
    │ path         │
    │ id_user (FK) │
    │ cod_mod (FK) │
    └──────────────┘
```

---

## 📊 Cardinalités Résumées

### Relations Principales

| Relation | Cardinalité | Type JPA | Justification Métier |
|----------|-------------|----------|----------------------|
| **Users ↔ Role** | N:M | @ManyToMany | Un utilisateur peut être ADMIN+AGENT |
| **Role → Privilege** | 1:N | @OneToMany | Un rôle a plusieurs menus accessibles |
| **Privilege → Menu** | N:1 | @ManyToOne | Plusieurs privilèges pour un menu |
| **Domaine → Module** | 1:N | @OneToMany | Un domaine contient plusieurs modules |
| **Module → Menu** | 1:N | @OneToMany | Un module contient plusieurs menus |
| **Reclamation → Statut** | N:1 | @ManyToOne | Plusieurs réclamations dans un statut |
| **Reclamation → Objet** | N:1 | @ManyToOne | Plusieurs réclamations pour un objet |
| **Reclamation → Canal** | N:1 | @ManyToOne | Plusieurs réclamations par canal |
| **Reclamation → NatureReclamant** | N:1 | @ManyToOne | Plusieurs réclamations par type client |
| **Objet → Service** | N:1 | @ManyToOne | Plusieurs objets par service |
| **Reclamation ↔ ReclamationGenerated** | 1:1 | Implicite | Une réclamation = un export |

---


## ✅ Respect des Bonnes Pratiques Spring Boot / JPA

### ✅ Bonnes Pratiques Respectées

#### 1. **Conventions de nommage**
- Tables préfixées par domaine (`hab_`, `rec_`, `sed_`)
- Colonnes snake_case (`dat_cre`, `user_mod`)
- Classes Java en PascalCase
- Attributs Java en camelCase

#### 2. **Annotations JPA correctes**
```java
@Entity
@Table(name = "rec_reclamation")
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ManyToOne / @OneToMany
@JoinColumn(name = "...")
```

#### 3. **Séparation des responsabilités**
- **Model** : Entités JPA
- **Repository** : Accès données (extends JpaRepository)
- **Service** : Logique métier
- **Controller** : API REST
- **DTO** : Transfert de données (LoginRequest, SignupRequest...)

#### 4. **Sécurité**
- JWT avec Spring Security
- BCrypt pour les mots de passe
- RBAC (Role-Based Access Control)
- AuthTokenFilter avec shouldNotFilter()
- CORS configuré

#### 5. **Relations JPA**
- Cascade correctement définie
- FetchType approprié (LAZY par défaut, EAGER pour roles)
- `mappedBy` pour les relations bidirectionnelles
- `@JoinColumn` avec `referencedColumnName`

#### 6. **Repositories**
- Extends `JpaRepository<Entity, ID>`
- Méthodes query dérivées : `findByStatRec_CodSta()`
- Méthodes custom avec `@Query`

---

### ⚠️ Points d'Amélioration

#### 1. **Absence de validation**
```java
// À ajouter dans les entités
@NotNull
@NotBlank
@Email
@Size(min = 8, max = 50)
@Pattern(regexp = "...")
```

#### 2. **userCre/userMod non typés**
```java
// Actuel
private Long userCre;  // ❌ Pas de FK JPA

// Recommandé
@ManyToOne
@JoinColumn(name = "user_cre")
private Users createur;  // ✅ Relation JPA
```

#### 3. **Absence d'audit automatique**
```java
// Recommandé
@EntityListeners(AuditingEntityListener.class)
public class Reclamation {
    @CreatedDate
    private Date datCre;
    
    @LastModifiedDate
    private Date datMod;
    
    @CreatedBy
    private String userCre;
    
    @LastModifiedBy
    private String userMod;
}
```

#### 4. **Pas de soft delete**
```java
// Recommandé
@SQLDelete(sql = "UPDATE rec_reclamation SET deleted = true WHERE ref_rec = ?")
@Where(clause = "deleted = false")
public class Reclamation {
    private Boolean deleted = false;
}
```

#### 5. **Pas de DTO pour réponses**
```java
// Recommandé : créer ReclamationResponseDTO
// pour éviter d'exposer toutes les colonnes
public class ReclamationResponseDTO {
    private String refRec;
    private String nomRec;
    private String prenRec;
    private String descRec;
    private String statutLibelle;
    private String objetLibelle;
    // ... champs nécessaires uniquement
}
```

---

## 🔥 Problèmes Critiques à Corriger

### 🚨 Priorité 1 (Bloquant)

#### 1. **Ajouter `agent_id` dans Reclamation**
**Impact:** Impossible de tracer qui traite quoi  
**SQL:**
```sql
ALTER TABLE rec_reclamation
ADD COLUMN agent_id VARCHAR(255),
ADD CONSTRAINT fk_reclamation_agent
FOREIGN KEY (agent_id) REFERENCES hab_users(id);
```

**Java:**
```java
@ManyToOne
@JoinColumn(name = "agent_id", referencedColumnName = "id")
private Users agent;
```

---

### ⚠️ Priorité 2 (Important)

#### 2. **Créer table `rec_statut_history`**
**Impact:** Pas d'historique, impossible de calculer les délais

**SQL:**
```sql
CREATE TABLE rec_statut_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    dat_change DATETIME NOT NULL,
    user_change VARCHAR(255),
    comment TEXT,
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec),
    FOREIGN KEY (user_change) REFERENCES hab_users(id),
    INDEX idx_ref_rec (ref_rec),
    INDEX idx_dat_change (dat_change)
);
```

#### 3. **Supprimer classe `Person.java`**
**Raison:** Classe POJO vide, non utilisée, confusion

---

### 📈 Priorité 3 (Nice to have)

#### 4. **Ajouter priorité**
```sql
ALTER TABLE rec_reclamation
ADD COLUMN priorite VARCHAR(20) DEFAULT 'NORMALE';
```

#### 5. **Créer table pièces jointes**
```sql
CREATE TABLE rec_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    dat_upload DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec)
);
```

#### 6. **Ajouter soft delete**
```sql
ALTER TABLE rec_reclamation ADD COLUMN deleted BOOLEAN DEFAULT false;
ALTER TABLE hab_users ADD COLUMN deleted BOOLEAN DEFAULT false;
```

---


## 📋 Classes à Supprimer ou Documenter

### ❌ À SUPPRIMER

#### 1. **Person.java**
```java
// Fichier: src/main/java/tn/reclamationapp/model/Person.java
// Raison: POJO vide, pas d'annotation @Entity, non utilisée
// Action: SUPPRIMER IMMÉDIATEMENT
```

**Commande:**
```bash
rm src/main/java/tn/reclamationapp/model/Person.java
```

---

### ⚠️ À DOCUMENTER OU SUPPRIMER

#### 2. **Path.java**
**Problème:**
- Préfixe `sed_` incohérent avec `hab_` et `rec_`
- Utilité peu claire
- Non utilisée dans les services actuels

**Action:** Si non utilisée dans le frontend → SUPPRIMER  
**Alternative:** Documenter son usage réel

---

### ⚠️ À VÉRIFIER

#### 3. **Domaine.java**
**Problème:** Couche d'abstraction peu exploitée  
**Utilité:** Uniquement pour regrouper modules  
**Action:** Conserver si frontend l'utilise, sinon fusionner avec Module

---

## 🎯 Recommandations Finales

### Architecture Globale

#### ✅ Points Forts
1. **Séparation claire des domaines métier** (Habilitation, Réclamation, Configuration)
2. **Entité Reclamation bien conçue** avec tous les champs nécessaires
3. **Workflow statut cohérent** et complet
4. **Sécurité JWT robuste** avec RBAC
5. **Traçabilité** via user_cre, user_mod, dat_cre, dat_mod
6. **Conformité réglementaire** (export XML BCT)

#### ⚠️ Améliorations Urgentes
1. **Ajouter `agent_id`** dans Reclamation pour traçabilité
2. **Créer `rec_statut_history`** pour historique complet
3. **Supprimer `Person.java`** (inutile)
4. **Documenter ou supprimer `Path.java`**
5. **Ajouter priorité** dans réclamations
6. **Créer table attachments** pour pièces jointes

---

### Proposition d'évolution

#### Phase 1 (Immédiat - 1 semaine)
- [ ] Ajouter colonne `agent_id` dans Reclamation
- [ ] Créer table `rec_statut_history`
- [ ] Supprimer `Person.java`
- [ ] Ajouter endpoint `/api/reclamations/{refRec}/assign`
- [ ] Modifier ReclamationService pour historiser les statuts

#### Phase 2 (Court terme - 2 semaines)
- [ ] Ajouter colonne `priorite` dans Reclamation
- [ ] Créer table `rec_attachment`
- [ ] Implémenter upload de fichiers
- [ ] Ajouter endpoints pièces jointes
- [ ] Remplacer userCre/userMod par relations JPA

#### Phase 3 (Moyen terme - 1 mois)
- [ ] Implémenter notifications email/SMS réelles
- [ ] Ajouter SLA (Service Level Agreement)
- [ ] Créer dashboards statistiques
- [ ] Soft delete sur toutes les tables importantes
- [ ] Audit automatique avec Spring Data JPA Auditing

---

## 📊 Statistiques du Modèle

### Répartition des Entités

| Domaine | Nombre Tables | Pourcentage | Importance |
|---------|---------------|-------------|------------|
| **Réclamation (rec_)** | 9 | 56% | ❤️ CŒUR MÉTIER |
| **Habilitation (hab_)** | 6 | 38% | 🔐 Sécurité |
| **Technique (sed_)** | 1 | 6% | ⚙️ Utilitaire |
| **Total JPA** | 16 | 100% | |
| **Utilitaires** | 3 | - | Non JPA |
| **À supprimer** | 1 | - | Person |

### Complexité des Relations

| Type Relation | Nombre | Exemple |
|---------------|--------|---------|
| **ManyToMany** | 1 | Users ↔ Role |
| **ManyToOne** | 10 | Reclamation → Statut |
| **OneToMany** | 3 | Role → Privilege |
| **Total** | 14 relations | |

### Qualité du Code

| Métrique | Valeur | Status |
|----------|--------|--------|
| **Entités JPA** | 16 | ✅ |
| **Repositories** | 14 | ✅ |
| **Services** | 4 | ✅ |
| **Controllers** | 5 | ✅ |
| **Endpoints API** | 40+ | ✅ |
| **Relations bien définies** | 14/14 | ✅ |
| **Tables inutiles** | 1 (Person) | ⚠️ |
| **Colonnes manquantes** | 2 (agent_id, priorite) | ⚠️ |
| **Tables manquantes** | 2 (history, attachment) | ⚠️ |

---

## 🏆 Conclusion

### Évaluation Globale : **8.5/10**

#### Ce qui est Excellent ✅
- Architecture solide et cohérente
- Entité centrale Reclamation très complète
- Sécurité JWT bien implémentée
- Relations JPA correctes
- Workflow métier clair et logique
- Traçabilité présente (user_cre, dat_cre...)
- Conformité réglementaire (XML BCT)

#### Points à Corriger Rapidement ⚠️
- Ajouter `agent_id` dans Reclamation
- Créer table d'historique des statuts
- Supprimer classe `Person.java` inutile
- Ajouter priorité aux réclamations

#### Évolutions Souhaitables 📈
- Table pièces jointes
- Soft delete
- Relations JPA pour userCre/userMod
- SLA et alertes
- Audit automatique

---

## 📚 Références

### Documentation Consultée
- Entités JPA : 21 classes analysées
- Repositories : 14 interfaces
- Services : 4 classes (ReclamationService, UsersService, RoleService, ReferenceDataService)
- Controllers : 5 classes (40+ endpoints)
- Configuration : SecurityConfig, AuthTokenFilter, JwtUtils

### Fichiers Analysés
```
src/main/java/tn/reclamationapp/
├── model/              (21 classes)
├── repository/         (14 interfaces)
├── service/            (4 services)
├── controller/         (5 controllers)
├── security/           (5 classes)
├── config/             (1 classe)
└── dto/                (4 classes)
```

---

**Date d'analyse:** 18 juin 2026  
**Analyste:** Kiro AI Assistant  
**Version Application:** 0.0.1-SNAPSHOT  
**Spring Boot:** 4.1.0  
**Base de données:** MySQL 8.0  

---

## 📎 Annexes

### A. Script SQL Complet de Correction

```sql
-- 1. Ajouter agent_id
ALTER TABLE rec_reclamation
ADD COLUMN agent_id VARCHAR(255),
ADD CONSTRAINT fk_reclamation_agent
FOREIGN KEY (agent_id) REFERENCES hab_users(id);

-- 2. Créer table historique
CREATE TABLE rec_statut_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    dat_change DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_change VARCHAR(255),
    comment TEXT,
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec),
    FOREIGN KEY (old_status) REFERENCES rec_statut(cod_sta),
    FOREIGN KEY (new_status) REFERENCES rec_statut(cod_sta),
    FOREIGN KEY (user_change) REFERENCES hab_users(id),
    INDEX idx_ref_rec (ref_rec),
    INDEX idx_dat_change (dat_change)
);

-- 3. Ajouter priorité
ALTER TABLE rec_reclamation
ADD COLUMN priorite VARCHAR(20) DEFAULT 'NORMALE'
CHECK (priorite IN ('BASSE', 'NORMALE', 'HAUTE', 'URGENTE'));

-- 4. Créer table attachments
CREATE TABLE rec_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    dat_upload DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_upload VARCHAR(255),
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec),
    FOREIGN KEY (user_upload) REFERENCES hab_users(id),
    INDEX idx_ref_rec (ref_rec)
);

-- 5. Ajouter soft delete
ALTER TABLE rec_reclamation ADD COLUMN deleted BOOLEAN DEFAULT false;
ALTER TABLE hab_users ADD COLUMN deleted BOOLEAN DEFAULT false;
ALTER TABLE rec_reclamation ADD COLUMN dat_delete DATETIME;
ALTER TABLE hab_users ADD COLUMN dat_delete DATETIME;

-- 6. Ajouter SLA
ALTER TABLE rec_objet ADD COLUMN sla_hours INT DEFAULT 48;
ALTER TABLE rec_reclamation ADD COLUMN sla_deadline DATETIME;

-- 7. Index de performance
CREATE INDEX idx_rec_statut ON rec_reclamation(sta_rec);
CREATE INDEX idx_rec_dat_rec ON rec_reclamation(dat_rec);
CREATE INDEX idx_rec_agent ON rec_reclamation(agent_id);
CREATE INDEX idx_rec_email ON rec_reclamation(email_rec);
```

---

### B. Checklist de Vérification

- [ ] Toutes les tables ont une clé primaire
- [ ] Toutes les FK ont une contrainte FOREIGN KEY
- [ ] Toutes les relations JPA sont bidirectionnelles (si nécessaire)
- [ ] FetchType approprié (LAZY par défaut)
- [ ] Cascade défini correctement
- [ ] Index sur colonnes de recherche fréquentes
- [ ] Validation Bean Validation sur DTOs
- [ ] Soft delete sur tables importantes
- [ ] Audit automatique (CreatedDate, CreatedBy...)
- [ ] DTO pour réponses API (ne pas exposer entités)

---

**FIN DE L'ANALYSE**

