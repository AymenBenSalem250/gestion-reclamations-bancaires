# Reclamation App - API Documentation

## Overview
Application REST API pour la gestion des réclamations avec authentification JWT et contrôle d'accès basé sur les rôles.

## Configuration

### Base de données MySQL
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/reclamation
spring.datasource.username=root
spring.datasource.password=
```

### JWT Configuration
```properties
app.jwt.secret=VerySecretKeyForJWTTokenGenerationPleaseChangeInProduction123456789
app.jwt.expiration=86400000  # 24 heures
```

## Endpoints API

### Authentification

#### POST /api/auth/login
Authentification d'un utilisateur
```json
{
  "email": "user@example.com",
  "password": "password"
}
```
Réponse:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": "user1",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["USER", "ADMIN"]
}
```

#### POST /api/auth/signup
Inscription d'un nouvel utilisateur
```json
{
  "id": "user1",
  "firstName": "John",
  "lastName": "Doe",
  "email": "user@example.com",
  "password": "password123",
  "roles": ["USER"]
}
```

#### POST /api/auth/change-password
Changer le mot de passe
```
POST /api/auth/change-password?userId=user1&oldPassword=old&newPassword=new
```

---

### Réclamations

#### GET /api/reclamations
Liste toutes les réclamations
- **Autorisation**: USER, ADMIN

#### GET /api/reclamations/{refRec}
Récupérer une réclamation par référence
- **Autorisation**: USER, ADMIN

#### POST /api/reclamations
Créer une nouvelle réclamation
- **Autorisation**: USER, ADMIN
```json
{
  "nomRec": "Dupont",
  "prenRec": "Jean",
  "emailRec": "jean.dupont@example.com",
  "gsmRec": 21612345678,
  "descRec": "Description de la réclamation",
  "natRec": { "id": 1 },
  "objet": { "id": 1 },
  "canal": { "id": 1 }
}
```

#### PUT /api/reclamations/{refRec}
Mettre à jour une réclamation
- **Autorisation**: USER, ADMIN

#### DELETE /api/reclamations/{refRec}
Supprimer une réclamation
- **Autorisation**: ADMIN

#### PUT /api/reclamations/{refRec}/status
Mettre à jour le statut d'une réclamation
```
PUT /api/reclamations/{refRec}/status?codSta=ENCOURS
```
- **Autorisation**: USER, ADMIN

#### PUT /api/reclamations/{refRec}/response
Ajouter une réponse à une réclamation
```
PUT /api/reclamations/{refRec}/response?response=Réponse détaillée
```
- **Autorisation**: USER, ADMIN

#### GET /api/reclamations/status/{codSta}
Récupérer les réclamations par statut
- **Autorisation**: USER, ADMIN

#### GET /api/reclamations/search
Rechercher des réclamations
```
GET /api/reclamations/search?search=Dupont
```
- **Autorisation**: USER, ADMIN

#### GET /api/reclamations/by-date
Réclamations par plage de dates
```
GET /api/reclamations/by-date?startDate=2024-01-01&endDate=2024-12-31
```
- **Autorisation**: USER, ADMIN

#### GET /api/reclamations/statistics
Statistiques des réclamations
- **Autorisation**: USER, ADMIN

---

### Utilisateurs

#### GET /api/users
Liste tous les utilisateurs
- **Autorisation**: ADMIN

#### GET /api/users/{id}
Récupérer un utilisateur par ID
- **Autorisation**: USER, ADMIN

#### POST /api/users
Créer un nouvel utilisateur
```
POST /api/users?roles=USER,ADMIN
```
- **Autorisation**: ADMIN

#### PUT /api/users/{id}
Mettre à jour un utilisateur
- **Autorisation**: USER, ADMIN

#### DELETE /api/users/{id}
Supprimer un utilisateur
- **Autorisation**: ADMIN

#### PUT /api/users/{id}/disable
Désactiver un utilisateur
- **Autorisation**: ADMIN

#### PUT /api/users/{id}/enable
Activer un utilisateur
- **Autorisation**: ADMIN

#### PUT /api/users/{id}/reset-password
Réinitialiser le mot de passe
```
PUT /api/users/{id}/reset-password?newPassword=newpass123
```
- **Autorisation**: ADMIN

---

### Données de référence

#### Statuts
- GET /api/reference/statuts
- GET /api/reference/statuts/{codSta}
- POST /api/reference/statuts (ADMIN)
- PUT /api/reference/statuts/{codSta} (ADMIN)
- DELETE /api/reference/statuts/{codSta} (ADMIN)

#### Services
- GET /api/reference/services
- GET /api/reference/services/{codSer}
- POST /api/reference/services (ADMIN)
- PUT /api/reference/services/{codSer} (ADMIN)
- DELETE /api/reference/services/{codSer} (ADMIN)

#### Objets
- GET /api/reference/objets
- GET /api/reference/objets/{codObj}
- GET /api/reference/objets/service/{codSer}
- POST /api/reference/objets (ADMIN)
- PUT /api/reference/objets/{id} (ADMIN)
- DELETE /api/reference/objets/{id} (ADMIN)

#### Canaux
- GET /api/reference/canals
- GET /api/reference/canals/{id}
- POST /api/reference/canals (ADMIN)
- PUT /api/reference/canals/{id} (ADMIN)
- DELETE /api/reference/canals/{id} (ADMIN)

#### Nature des réclamants
- GET /api/reference/nature-reclamants
- GET /api/reference/nature-reclamants/{id}
- POST /api/reference/nature-reclamants (ADMIN)
- PUT /api/reference/nature-reclamants/{id} (ADMIN)
- DELETE /api/reference/nature-reclamants/{id} (ADMIN)

#### Paramètres généraux
- GET /api/reference/params
- GET /api/reference/params/name?libParam=PARAM_NAME
- POST /api/reference/params (ADMIN)
- PUT /api/reference/params/{id} (ADMIN)
- DELETE /api/reference/params/{id} (ADMIN)

---

### Rôles et Privilèges

#### GET /api/roles
Liste tous les rôles
- **Autorisation**: ADMIN

#### GET /api/roles/{id}
Récupérer un rôle par ID
- **Autorisation**: ADMIN

#### POST /api/roles
Créer un nouveau rôle
- **Autorisation**: ADMIN

#### GET /api/roles/{id}/privileges
Liste les privilèges d'un rôle
- **Autorisation**: ADMIN

#### POST /api/roles/{roleId}/privileges
Ajouter un privilège à un rôle
```
POST /api/roles/1/privileges?codMenu=MENU_CODE
```
- **Autorisation**: ADMIN

---

## Codes de statut

- **NOUVEAU**: Réclamation nouvellement créée
- **ENCOURS**: Réclamation en cours de traitement
- **TRAITE**: Réclamation traitée
- **CLOTURE**: Réclamation clôturée

## Sécurité

### Authentification JWT
Toutes les requêtes (sauf /api/auth/**) nécessitent un token JWT valide dans le header:
```
Authorization: Bearer <your-jwt-token>
```

### Rôles
- **ADMIN**: Accès complet à toutes les fonctionnalités
- **USER**: Accès aux réclamations et données de référence (lecture/écriture limitée)

## Démarrage de l'application

```bash
# Avec Maven wrapper
./mvnw spring-boot:run

# Ou sur Windows
mvnw.cmd spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## Structure des entités principales

### Reclamation
- refRec: Référence unique
- typeRec: Type de réclamation
- nomRec, prenRec: Nom et prénom du réclamant
- emailRec, gsmRec: Contact
- descRec: Description
- natRec: Nature du réclamant (relation)
- objet: Objet de la réclamation (relation)
- canal: Canal de communication (relation)
- statRec: Statut (relation)
- datRec: Date de création
- datCre, datMod: Métadonnées de création/modification

### Users
- id: Identifiant unique
- email: Email (unique)
- password: Mot de passe (crypté)
- firstName, lastName: Nom complet
- disabled: Compte désactivé?
- defaultPassword: Mot de passe par défaut?
- nbrAttemps: Nombre de tentatives de connexion
- roles: Collection de rôles

### Role
- id: Identifiant
- librole: Libellé du rôle
- codrole: Code du rôle
- privileges: Liste des privilèges associés
