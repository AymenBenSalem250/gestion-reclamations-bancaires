# 🚀 Guide Complet de Test Postman - Reclamation App

## 📋 Table des Matières
1. [Liste des Endpoints](#endpoints)
2. [Configuration Postman](#configuration)
3. [Procédure de Test Complète](#procedure)
4. [Correction du Problème 401](#fix-401)
5. [Collection Postman JSON](#collection)

---

## 🌐 1. LISTE DES ENDPOINTS <a name="endpoints"></a>

### **Endpoints Publics** (pas de token requis)

| Méthode | URL | Description | Rôle requis |
|---------|-----|-------------|-------------|
| `POST` | `/api/auth/signup` | Créer un compte utilisateur | Aucun (Public) |
| `POST` | `/api/auth/login` | Se connecter et obtenir JWT | Aucun (Public) |
| `POST` | `/api/auth/change-password` | Changer mot de passe | Aucun (Public) |

### **Endpoints Protégés** (token JWT requis)

| Méthode | URL | Description | Rôle requis |
|---------|-----|-------------|-------------|
| `GET` | `/api/users` | Liste tous les utilisateurs | ADMIN |
| `GET` | `/api/users/{id}` | Récupérer un utilisateur | USER, ADMIN |
| `POST` | `/api/users` | Créer un utilisateur | ADMIN |
| `PUT` | `/api/users/{id}` | Modifier un utilisateur | USER, ADMIN |
| `DELETE` | `/api/users/{id}` | Supprimer un utilisateur | ADMIN |
| `GET` | `/api/reclamations` | Liste toutes les réclamations | USER, ADMIN |
| `POST` | `/api/reclamations` | Créer une réclamation | USER, ADMIN |
| `PUT` | `/api/reclamations/{refRec}` | Modifier une réclamation | USER, ADMIN |
| `DELETE` | `/api/reclamations/{refRec}` | Supprimer une réclamation | ADMIN |
| `GET` | `/api/reference/**` | Données de référence | USER, ADMIN |
| `POST/PUT/DELETE` | `/api/reference/**` | Modifier données référence | ADMIN |

---

## ⚙️ 2. CONFIGURATION POSTMAN <a name="configuration"></a>

### **Variables d'Environnement**

Créer un environnement Postman nommé `Reclamation-Dev`:

```json
{
  "name": "Reclamation-Dev",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "type": "default",
      "enabled": true
    },
    {
      "key": "token",
      "value": "",
      "type": "default",
      "enabled": true
    },
    {
      "key": "adminEmail",
      "value": "admin@reclamation.tn",
      "type": "default",
      "enabled": true
    },
    {
      "key": "adminPassword",
      "value": "Admin@123",
      "type": "default",
      "enabled": true
    }
  ]
}
```

---

## 🧪 3. PROCÉDURE DE TEST COMPLÈTE <a name="procedure"></a>

### **Étape 0: Préparer la base de données**

Avant de tester, vous devez avoir au moins UN rôle dans la table `hab_role`:

```sql
-- Connectez-vous à MySQL
USE reclamation;

-- Créer les rôles de base
INSERT INTO hab_role (id, cod_role, lib_role) VALUES 
(1, 'ADMIN', 'Administrateur'),
(2, 'USER', 'Utilisateur'),
(3, 'AGENT', 'Agent'),
(4, 'RESPONSABLE', 'Responsable'),
(5, 'CLIENT', 'Client');
```

### **Étape 1: Signup (Créer un Administrateur)**

**Endpoint:** `POST {{baseUrl}}/api/auth/signup`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "id": "admin001",
  "firstName": "Admin",
  "lastName": "System",
  "email": "admin@reclamation.tn",
  "password": "Admin@123",
  "roles": ["ADMIN"]
}
```

**Réponse attendue (201 Created):**
```json
{
  "message": "User registered successfully!"
}
```

**⚠️ Erreurs possibles:**
- `400 Bad Request`: Email déjà utilisé ou validation échouée
- `500 Internal Server Error`: Rôle "ADMIN" n'existe pas dans `hab_role`

---

### **Étape 2: Login (Obtenir le JWT Token)**

**Endpoint:** `POST {{baseUrl}}/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "admin@reclamation.tn",
  "password": "Admin@123"
}
```

**Réponse attendue (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": "admin001",
  "email": "admin@reclamation.tn",
  "firstName": "Admin",
  "lastName": "System",
  "roles": ["ADMIN"]
}
```

**📝 Script Postman pour sauvegarder le token:**

Onglet **Tests** dans Postman:
```javascript
// Sauvegarder le token dans la variable d'environnement
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    console.log("Token saved:", jsonData.token);
}
```

---

### **Étape 3: Tester un Endpoint Protégé**

**Endpoint:** `GET {{baseUrl}}/api/users`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**⚡ Configuration dans Postman:**
1. Onglet **Authorization**
2. Type: `Bearer Token`
3. Token: `{{token}}` (variable d'environnement)

**Réponse attendue (200 OK):**
```json
[
  {
    "id": "admin001",
    "email": "admin@reclamation.tn",
    "firstName": "Admin",
    "lastName": "System",
    "disabled": false,
    "roles": [...]
  }
]
```

---

### **Étape 4: Créer un Utilisateur (ADMIN uniquement)**

**Endpoint:** `POST {{baseUrl}}/api/users?roles=USER,AGENT`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "id": "user001",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "User@123"
}
```

**Query Parameters:**
- `roles=USER,AGENT`

**Réponse attendue (201 Created):**
```json
{
  "id": "user001",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "disabled": false,
  "roles": [...]
}
```

---

### **Étape 5: Tester les Rôles (403 Forbidden)**

**Scénario:** Se connecter avec un compte USER et essayer d'accéder à `/api/users` (ADMIN only)

1. **Login avec USER:**
```json
{
  "email": "john.doe@example.com",
  "password": "User@123"
}
```

2. **Appeler GET /api/users** avec le token USER

**Réponse attendue (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

## 🔧 4. CORRECTION DU PROBLÈME 401 <a name="fix-401"></a>

### **Symptôme:**
- `POST /api/auth/login` → **401 Unauthorized**
- `POST /api/auth/signup` → **401 Unauthorized**

### **Cause Racine:**
Le `AuthTokenFilter` s'exécute sur **tous** les endpoints, y compris `/api/auth/**`. Quand aucun token n'est présent, le filtre ne bloque pas explicitement, MAIS Spring Security considère qu'il n'y a pas d'authentification et déclenche `AuthEntryPointJwt`.

### **Solution Appliquée:**

Ajout de `shouldNotFilter()` dans `AuthTokenFilter.java`:

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth/") || path.startsWith("/api/public/");
}
```

### **Vérifications à Faire:**

✅ **1. SecurityFilterChain**
```java
.authorizeHttpRequests(auth -> 
    auth.requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/public/**").permitAll()
        .anyRequest().authenticated()
);
```

✅ **2. AuthTokenFilter ne s'applique pas aux endpoints publics**
```java
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth/") || path.startsWith("/api/public/");
}
```

✅ **3. CORS activé**
```java
http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

✅ **4. CSRF désactivé (pour API REST)**
```java
http.csrf(csrf -> csrf.disable())
```

---

## 📦 5. TESTS CAS D'ERREUR

### **Test 1: Login avec mauvais mot de passe**
```json
{
  "email": "admin@reclamation.tn",
  "password": "WrongPassword"
}
```
**Réponse:** `401 Unauthorized` - "Invalid email or password"

---

### **Test 2: Signup avec email existant**
```json
{
  "id": "admin002",
  "firstName": "Admin",
  "lastName": "Duplicate",
  "email": "admin@reclamation.tn",
  "password": "Admin@123",
  "roles": ["ADMIN"]
}
```
**Réponse:** `400 Bad Request` - "Error: Email is already in use!"

---

### **Test 3: Signup avec validation échouée**
```json
{
  "id": "",
  "firstName": "Admin",
  "lastName": "Test",
  "email": "invalid-email",
  "password": "123",
  "roles": ["ADMIN"]
}
```
**Réponse:** `400 Bad Request` avec détails des erreurs de validation:
```json
{
  "id": "User ID is required",
  "email": "Email should be valid",
  "password": "Password must be at least 6 characters"
}
```

---

### **Test 4: Appel endpoint protégé sans token**
**Request:** `GET /api/users` (sans header Authorization)

**Réponse:** `401 Unauthorized`
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/users"
}
```

---

### **Test 5: Token JWT expiré**
**Réponse:** `401 Unauthorized` - "JWT token is expired"

---

### **Test 6: Token JWT invalide**
**Request:** `GET /api/users` avec `Authorization: Bearer invalid_token_xyz`

**Réponse:** `401 Unauthorized` - "Invalid JWT token"

---

## 🎯 RÉSUMÉ DES CODES HTTP

| Code | Signification | Contexte |
|------|---------------|----------|
| `200 OK` | Succès | Login, GET requests |
| `201 Created` | Ressource créée | Signup, POST requests |
| `400 Bad Request` | Validation échouée | Body JSON incorrect |
| `401 Unauthorized` | Non authentifié | Pas de token / Token invalide |
| `403 Forbidden` | Accès refusé | Token valide mais rôle insuffisant |
| `404 Not Found` | Ressource introuvable | ID utilisateur inexistant |
| `500 Internal Server Error` | Erreur serveur | Exception non gérée |

---

## 🔑 MAPPING EMAIL ↔ USERNAME

**Important:** Votre système utilise **EMAIL** comme identifiant unique (pas de username séparé).

- `LoginRequest`: champ `email` (pas `username`)
- `UserDetails.getUsername()`: retourne l'**email**
- JWT subject: contient l'**email**
- Authentication: basée sur l'**email**

---

## 🚀 COMMANDES POUR RELANCER L'APPLICATION

```bash
# Recompiler avec la correction
.\mvnw.cmd clean compile

# Démarrer l'application
.\mvnw.cmd spring-boot:run
```

Attendre le message:
```
Started ReclamationAppApplication in X.XXX seconds
```

---

## 📝 NOTES IMPORTANTES

1. **Roles dans la DB:** Les rôles doivent exister dans `hab_role` avant le signup
2. **Token Expiration:** 24 heures (86400000 ms)
3. **Password Encoding:** BCrypt
4. **CORS:** Autorisé pour `localhost:3000` et `localhost:4200`
5. **Session:** STATELESS (pas de session HTTP)
6. **Login Attempts:** 3 tentatives max → compte désactivé

---

## ✅ CHECKLIST DE VALIDATION

- [ ] Base de données MySQL démarrée
- [ ] Tables créées (Hibernate `ddl-auto=update`)
- [ ] Rôles insérés dans `hab_role`
- [ ] Application Spring Boot démarrée
- [ ] Postman configuré avec l'environnement
- [ ] Signup réussi → 201 Created
- [ ] Login réussi → 200 OK + token
- [ ] Token sauvegardé automatiquement
- [ ] Endpoint protégé accessible avec token → 200 OK
- [ ] Endpoint protégé sans token → 401 Unauthorized
- [ ] Endpoint ADMIN avec token USER → 403 Forbidden
