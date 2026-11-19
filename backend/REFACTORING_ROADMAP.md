# 🚀 Roadmap de Refactoring - Application Réclamations TSB

**Objectif:** Transformer l'architecture actuelle en architecture professionnelle conforme BCT 2022-08

---

## 📊 Situation Actuelle vs Cible

| Aspect | Actuel ❌ | Cible ✅ |
|--------|-----------|----------|
| **Structure** | Flat, pas de séparation claire | Packages organisés (dto, mapper, exception...) |
| **Services** | Classe concrète directe | Interface + Implementation |
| **DTOs** | Entités JPA exposées | DTOs Request/Response séparés |
| **Mappers** | Conversion manuelle dans service | MapStruct centralisé |
| **Exceptions** | GlobalExceptionHandler basique | Hiérarchie complète + ErrorResponse |
| **Validation** | Partielle | Bean Validation complète |
| **Repositories** | Requêtes basiques | JOIN FETCH, @EntityGraph, Specification |
| **Transactions** | @Transactional partout | Stratégie optimisée (readOnly) |
| **Pagination** | Absente | Page + Pageable + PageResponseDTO |
| **Recherche** | Simple findBy | Specification dynamique |
| **Audit** | Manuel (userCre, datCre) | @EntityListeners automatique |
| **Sécurité** | JWT basique | JWT + RBAC + Audit complet |

---

## 🎯 Plan d'Action (5 Phases)

### Phase 1: Structure et Fondations (Semaine 1)

#### Jour 1-2: Restructuration Packages

```bash
# Créer nouvelle structure
mkdir src/main/java/tn/reclamationapp/dto/request
mkdir src/main/java/tn/reclamationapp/dto/response
mkdir src/main/java/tn/reclamationapp/mapper
mkdir src/main/java/tn/reclamationapp/exception
mkdir src/main/java/tn/reclamationapp/service/impl
mkdir src/main/java/tn/reclamationapp/specification
mkdir src/main/java/tn/reclamationapp/audit
mkdir src/main/java/tn/reclamationapp/validation
```

**Actions:**
- [x] Créer structure de packages
- [ ] Déplacer classes existantes vers nouveaux packages
- [ ] Vérifier compilation

#### Jour 3-4: DTOs et Mappers

**Actions:**
- [ ] Créer ReclamationRequestDTO (voir EXEMPLES_CODE_COMPLETS.md)
- [ ] Créer ReclamationResponseDTO
- [ ] Créer PageResponseDTO<T>
- [ ] Créer ReclamationUpdateDTO
- [ ] Ajouter dépendance MapStruct au pom.xml
- [ ] Créer ReclamationMapper avec MapStruct
- [ ] Tester génération mapper (mvn compile)

**Fichiers à créer:**
```
dto/request/ReclamationRequestDTO.java
dto/request/ReclamationUpdateDTO.java
dto/response/ReclamationResponseDTO.java
dto/response/PageResponseDTO.java
dto/response/StatutDTO.java
dto/response/ObjetDTO.java
mapper/ReclamationMapper.java
```

#### Jour 5: Exceptions Personnalisées

**Actions:**
- [ ] Créer BusinessException (classe mère)
- [ ] Créer ResourceNotFoundException
- [ ] Créer BadRequestException
- [ ] Créer ConflictException
- [ ] Créer UnauthorizedException
- [ ] Créer ForbiddenException
- [ ] Créer ErrorResponse DTO
- [ ] Améliorer GlobalExceptionHandler

**Fichiers à créer:**
```
exception/BusinessException.java
exception/ResourceNotFoundException.java
exception/BadRequestException.java
exception/ConflictException.java
exception/ErrorResponse.java
```

---

### Phase 2: Refactoring Services (Semaine 2)

#### Jour 6-7: Services Réclamation

**Actions:**
- [ ] Créer interface ReclamationService
- [ ] Renommer ReclamationService actuel en ReclamationServiceImpl
- [ ] Déplacer ReclamationServiceImpl dans service/impl/
- [ ] Implémenter interface dans ReclamationServiceImpl
- [ ] Ajouter @Transactional(readOnly = true) sur classe
- [ ] Ajouter @Transactional sur méthodes d'écriture
- [ ] Remplacer retours Reclamation par ReclamationResponseDTO
- [ ] Utiliser ReclamationMapper pour conversions
- [ ] Ajouter logging (Slf4j)

**Refactoring:**
```java
// Avant
@Service
public class ReclamationService {
    public Reclamation create(Reclamation rec) { ... }
}

// Après
public interface ReclamationService {
    ReclamationResponseDTO create(ReclamationRequestDTO dto);
}

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReclamationServiceImpl implements ReclamationService {
    private final ReclamationMapper mapper;
    
    @Override
    @Transactional
    public ReclamationResponseDTO create(ReclamationRequestDTO dto) {
        log.info("Création réclamation: {}", dto.getEmailRec());
        Reclamation entity = mapper.toEntity(dto);
        // ...
        return mapper.toDto(saved);
    }
}
```

#### Jour 8-9: Services Users, Role, ReferenceData

**Actions:**
- [ ] Appliquer même refactoring pour UsersService
- [ ] Appliquer même refactoring pour RoleService
- [ ] Appliquer même refactoring pour ReferenceDataService
- [ ] Créer DTOs correspondants
- [ ] Créer Mappers correspondants

---

### Phase 3: Optimisation Repositories (Semaine 3)

#### Jour 10-11: ReclamationRepository

**Actions:**
- [ ] Ajouter méthode `findByIdWithDetails` avec JOIN FETCH
- [ ] Ajouter `@EntityGraph` sur méthodes liste
- [ ] Créer méthodes avec Specification (JpaSpecificationExecutor)
- [ ] Ajouter projections DTO pour listes
- [ ] Optimiser requêtes statistiques

**Exemple:**
```java
@Query("SELECT r FROM Reclamation r " +
       "LEFT JOIN FETCH r.statRec " +
       "LEFT JOIN FETCH r.objet " +
       "WHERE r.refRec = :refRec")
Optional<Reclamation> findByIdWithDetails(@Param("refRec") String refRec);
```

#### Jour 12: Specification pour Recherche Dynamique

**Actions:**
- [ ] Créer ReclamationSpecification
- [ ] Implémenter méthode `hasFilters(...)` avec Predicate
- [ ] Tester recherche multi-critères

---

### Phase 4: Refactoring Controllers (Semaine 4)

#### Jour 13-15: ReclamationController

**Actions:**
- [ ] Remplacer tous les retours Reclamation par ReclamationResponseDTO
- [ ] Remplacer paramètres Reclamation par ReclamationRequestDTO
- [ ] Ajouter validation @Valid sur RequestBody
- [ ] Utiliser ResponseEntity<DTO> partout
- [ ] Ajouter annotations Swagger (@Operation, @ApiResponse...)
- [ ] Ajouter @PreAuthorize pour RBAC
- [ ] Implémenter pagination (Pageable)
- [ ] Implémenter recherche dynamique

**Exemple:**
```java
@PostMapping
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
@Operation(summary = "Créer réclamation")
public ResponseEntity<ReclamationResponseDTO> create(
        @Valid @RequestBody ReclamationRequestDTO dto) {
    ReclamationResponseDTO created = reclamationService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

#### Jour 16-17: Autres Controllers

**Actions:**
- [ ] Refactorer UsersController
- [ ] Refactorer RoleController
- [ ] Refactorer ReferenceDataController
- [ ] Refactorer AuthController

---

### Phase 5: Audit, Tests et Documentation (Semaine 5)

#### Jour 18-19: Audit Trail

**Actions:**
- [ ] Créer classe abstraite `Auditable`
- [ ] Configurer JPA Auditing (`@EnableJpaAuditing`)
- [ ] Créer `AuditorAwareImpl`
- [ ] Faire hériter Reclamation de Auditable
- [ ] Créer table `rec_statut_history`
- [ ] Implémenter sauvegarde automatique historique

#### Jour 20-21: Tests

**Actions:**
- [ ] Tests unitaires ReclamationService
- [ ] Tests unitaires ReclamationMapper
- [ ] Tests intégration ReclamationRepository
- [ ] Tests intégration ReclamationController (MockMvc)
- [ ] Tests sécurité JWT

#### Jour 22: Documentation

**Actions:**
- [ ] Configurer Swagger/OpenAPI
- [ ] Ajouter annotations @Operation partout
- [ ] Générer documentation API
- [ ] Créer README technique
- [ ] Créer guide déploiement

---

## 📋 Checklist Complète

### DTOs (7 fichiers)
- [ ] ReclamationRequestDTO
- [ ] ReclamationResponseDTO
- [ ] ReclamationUpdateDTO
- [ ] PageResponseDTO<T>
- [ ] ReclamationSearchCriteria
- [ ] ErrorResponse
- [ ] StatutDTO, ObjetDTO, CanalDTO...

### Mappers (1 fichier)
- [ ] ReclamationMapper (MapStruct)

### Exceptions (7 fichiers)
- [ ] BusinessException
- [ ] ResourceNotFoundException
- [ ] BadRequestException
- [ ] ConflictException
- [ ] UnauthorizedException
- [ ] ForbiddenException
- [ ] GlobalExceptionHandler (améliorer)

### Services (2 fichiers par entité)
- [ ] ReclamationService (interface)
- [ ] ReclamationServiceImpl
- [ ] UsersService + Impl
- [ ] RoleService + Impl
- [ ] ReferenceDataService + Impl

### Repositories (améliorer)
- [ ] ReclamationRepository (JOIN FETCH, EntityGraph)
- [ ] Ajouter JpaSpecificationExecutor

### Specification (1 fichier)
- [ ] ReclamationSpecification

### Controllers (refactorer)
- [ ] ReclamationController
- [ ] UsersController
- [ ] RoleController
- [ ] ReferenceDataController

### Audit (3 fichiers)
- [ ] Auditable (abstract class)
- [ ] AuditConfig (@EnableJpaAuditing)
- [ ] AuditorAwareImpl

### Configuration
- [ ] SecurityConfig (améliorer)
- [ ] OpenApiConfig (Swagger)
- [ ] JpaConfig

---

## 🎯 Résultat Final Attendu

### Architecture Finale

```
src/main/java/tn/reclamationapp/
├── config/                     ✅ Configuration Spring
├── controller/                 ✅ API REST (DTO only)
├── service/                    ✅ Interfaces métier
├── service/impl/               ✅ Implémentations
├── repository/                 ✅ JPA optimisé
├── model/                      ✅ Entités JPA
├── dto/
│   ├── request/                ✅ DTOs entrée
│   └── response/               ✅ DTOs sortie
├── mapper/                     ✅ MapStruct
├── exception/                  ✅ Hiérarchie complète
├── specification/              ✅ Recherche dynamique
├── audit/                      ✅ Audit automatique
└── security/                   ✅ JWT + RBAC
```

### Indicateurs de Succès

- ✅ **0 entités JPA** exposées dans les controllers
- ✅ **100% des services** ont interface + impl
- ✅ **100% des endpoints** utilisent DTOs
- ✅ **Toutes les méthodes write** ont @Transactional
- ✅ **Toutes les méthodes read** ont @Transactional(readOnly=true)
- ✅ **0 problème N+1** (JOIN FETCH ou EntityGraph partout)
- ✅ **Pagination** sur toutes les listes
- ✅ **Recherche dynamique** fonctionnelle
- ✅ **GlobalExceptionHandler** complet
- ✅ **Audit trail** automatique
- ✅ **Documentation Swagger** complète
- ✅ **Tests** > 80% couverture

---

## 📚 Références

### Documents Créés

1. **ARCHITECTURE_GUIDE.md** (15+ pages)
   - Structure packages
   - Bonnes pratiques
   - Gestion transactionnelle
   - Optimisation JPA
   - DTOs et Mappers
   - Exceptions
   - Sécurité
   - Pagination et recherche
   - Audit et logging

2. **EXEMPLES_CODE_COMPLETS.md**
   - DTOs complets
   - Mapper MapStruct
   - Repository optimisé
   - Service Interface + Impl
   - Controller complet
   - Exceptions personnalisées

3. **ANALYSE_COMPLETE_BDD.md**
   - Analyse 21 entités
   - Relations JPA
   - Flux métier
   - Diagramme UML
   - Améliorations recommandées

4. **REFACTORING_ROADMAP.md** (ce document)
   - Plan d'action 5 phases
   - Checklist complète
   - Timeline

### Commandes Utiles

```bash
# Compiler avec MapStruct
mvn clean compile

# Lancer application
.\mvnw.cmd spring-boot:run

# Tests
mvn test

# Générer documentation Swagger
# Accéder à: http://localhost:8080/swagger-ui.html

# Vérifier couverture tests
mvn clean test jacoco:report
```

---

## ✅ Étapes Suivantes

1. **Lire** ARCHITECTURE_GUIDE.md en entier
2. **Copier** exemples de EXEMPLES_CODE_COMPLETS.md
3. **Suivre** ce roadmap phase par phase
4. **Tester** après chaque phase
5. **Commiter** après chaque phase fonctionnelle

---

**Bon courage ! Cette architecture est professionnelle, maintenable et conforme BCT 2022-08. 🎓**

Vous êtes prêt pour votre soutenance !
