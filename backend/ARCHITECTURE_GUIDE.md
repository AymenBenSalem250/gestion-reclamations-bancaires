# 🏗️ Guide d'Architecture Spring Boot - Application Gestion Réclamations Bancaires TSB

**Conformité:** Circulaire BCT 2022-08  
**Date:** 18 juin 2026  
**Version:** 2.0  
**Spring Boot:** 4.1.0

---

## 📋 Table des Matières

1. [Structure de Packages Idéale](#1-structure-de-packages-idéale)
2. [Architecture 3-Tiers](#2-architecture-3-tiers)
3. [Bonnes Pratiques Spring Boot](#3-bonnes-pratiques-spring-boot)
4. [Gestion Transactionnelle](#4-gestion-transactionnelle)
5. [Optimisation JPA](#5-optimisation-jpa)
6. [DTOs et Mappers](#6-dtos-et-mappers)
7. [Gestion des Exceptions](#7-gestion-des-exceptions)
8. [Sécurité JWT](#8-sécurité-jwt)
9. [Pagination et Recherche](#9-pagination-et-recherche)
10. [Audit et Logging](#10-audit-et-logging)
11. [Architecture Finale](#11-architecture-finale)
12. [Conseils Professionnels](#12-conseils-professionnels)

---

## 1. Structure de Packages Idéale

### 📁 Architecture Recommandée

```
src/main/java/tn/reclamationapp/
│
├── 📦 config/                          # Configuration Spring
│   ├── SecurityConfig.java
│   ├── JpaConfig.java
│   ├── WebConfig.java
│   ├── OpenApiConfig.java
│   └── AuditConfig.java
│
├── 📦 controller/                      # API REST Endpoints
│   ├── ReclamationController.java
│   ├── UsersController.java
│   ├── RoleController.java
│   ├── ReferenceDataController.java
│   └── ReportingController.java
│
├── 📦 service/                         # Interfaces métier
│   ├── ReclamationService.java
│   ├── UsersService.java
│   ├── RoleService.java
│   ├── ReferenceDataService.java
│   ├── NotificationService.java
│   └── ReportingService.java
│
├── 📦 service/impl/                    # Implémentations
│   ├── ReclamationServiceImpl.java
│   ├── UsersServiceImpl.java
│   ├── RoleServiceImpl.java
│   ├── ReferenceDataServiceImpl.java
│   ├── NotificationServiceImpl.java
│   └── ReportingServiceImpl.java
│
├── 📦 repository/                      # Accès données JPA
│   ├── ReclamationRepository.java
│   ├── UsersRepository.java
│   ├── RoleRepository.java
│   ├── StatutRepository.java
│   ├── ObjetRepository.java
│   ├── CanalRepository.java
│   └── ... (autres repositories)
│
├── 📦 model/                           # Entités JPA
│   ├── Reclamation.java
│   ├── Users.java
│   ├── Role.java
│   ├── Statut.java
│   ├── Objet.java
│   └── ... (autres entités)
│
├── 📦 dto/                             # Data Transfer Objects
│   ├── request/                        # DTOs requêtes
│   │   ├── ReclamationRequestDTO.java
│   │   ├── ReclamationUpdateDTO.java
│   │   ├── UserRegistrationDTO.java
│   │   └── LoginRequestDTO.java
│   │
│   └── response/                       # DTOs réponses
│       ├── ReclamationResponseDTO.java
│       ├── ReclamationDetailDTO.java
│       ├── UserResponseDTO.java
│       ├── JwtResponseDTO.java
│       └── PageResponseDTO.java
│
├── 📦 mapper/                          # Conversion Entity ↔ DTO
│   ├── ReclamationMapper.java
│   ├── UsersMapper.java
│   ├── RoleMapper.java
│   └── MapperUtils.java
│
├── 📦 exception/                       # Gestion erreurs
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── BusinessException.java
│   ├── ConflictException.java
│   └── ErrorResponse.java
│
├── 📦 security/                        # Sécurité JWT
│   ├── JwtUtils.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── UserDetailsImpl.java
│   └── UserDetailsServiceImpl.java
│
├── 📦 validation/                      # Validateurs custom
│   ├── ReclamationValidator.java
│   ├── EmailValidator.java
│   └── PhoneNumberValidator.java
│
├── 📦 specification/                   # Recherche dynamique
│   ├── ReclamationSpecification.java
│   └── GenericSpecification.java
│
├── 📦 audit/                           # Audit trail
│   ├── AuditListener.java
│   ├── Auditable.java
│   └── AuditLog.java
│
├── 📦 util/                            # Utilitaires
│   ├── DateUtils.java
│   ├── StringUtils.java
│   ├── Constants.java
│   └── ReferenceGenerator.java
│
├── 📦 enums/                           # Énumérations
│   ├── StatutReclamation.java
│   ├── CanalType.java
│   ├── Priorite.java
│   └── RoleType.java
│
└── ReclamationAppApplication.java      # Main
```

### 🎯 Avantages de cette Structure

✅ **Séparation claire des responsabilités**  
✅ **Maintenabilité et scalabilité**  
✅ **Tests unitaires facilités**  
✅ **Conformité architecture hexagonale**  
✅ **Réutilisabilité du code**

---

## 2. Architecture 3-Tiers

### 🏛️ Principe de Séparation

```
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE PRÉSENTATION                       │
│                      (Controllers)                           │
│  • Validation des entrées                                    │
│  • Conversion DTO ↔ réponse                                  │
│  • Gestion HTTP (GET, POST, PUT, DELETE)                     │
│  • Documentation API (Swagger)                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                     COUCHE MÉTIER                            │
│                   (Services + Impl)                          │
│  • Logique business                                          │
│  • Validation métier                                         │
│  • Gestion des transactions (@Transactional)                 │
│  • Orchestration des repositories                            │
│  • Transformation des données                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  COUCHE PERSISTANCE                          │
│                    (Repositories)                            │
│  • Accès base de données                                     │
│  • Requêtes JPA/JPQL                                         │
│  • Gestion des relations                                     │
│  • Optimisation fetch strategies                             │
└─────────────────────────────────────────────────────────────┘
```

### 📌 Règles Strictes

| Couche | Doit | Ne Doit Pas |
|--------|------|-------------|
| **Controller** | Valider entrées, appeler service, retourner DTO | Contenir logique métier, accéder au repository |
| **Service** | Logique métier, orchestrer repos, gérer transactions | Retourner entités JPA directement au controller |
| **Repository** | Requêtes SQL, gestion persistence | Contenir logique métier |

---

## 3. Bonnes Pratiques Spring Boot

### ✅ Controller Layer

```java
@RestController
@RequestMapping("/api/reclamations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Réclamations", description = "API de gestion des réclamations")
public class ReclamationController {
    
    private final ReclamationService reclamationService;
    
    // ✅ Bon : Utilise DTO, validation, ResponseEntity
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Créer une réclamation")
    public ResponseEntity<ReclamationResponseDTO> create(
            @Valid @RequestBody ReclamationRequestDTO dto) {
        
        ReclamationResponseDTO created = reclamationService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }
    
    // ❌ Mauvais : Retourne entité, pas de validation
    @PostMapping("/bad")
    public Reclamation createBad(Reclamation reclamation) {
        return reclamationService.saveBad(reclamation);
    }
}
```

### ✅ Service Layer

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReclamationServiceImpl implements ReclamationService {
    
    private final ReclamationRepository reclamationRepository;
    private final ReclamationMapper reclamationMapper;
    
    @Override
    @Transactional  // ✅ Écriture : @Transactional
    public ReclamationResponseDTO create(ReclamationRequestDTO dto) {
        log.info("Création réclamation pour email: {}", dto.getEmailRec());
        
        // Validation métier
        validateReclamation(dto);
        
        // Conversion DTO → Entity
        Reclamation reclamation = reclamationMapper.toEntity(dto);
        
        // Génération référence unique
        reclamation.setRefRec(generateReference());
        
        // Sauvegarde
        Reclamation saved = reclamationRepository.save(reclamation);
        
        // Entity → DTO
        return reclamationMapper.toDto(saved);
    }
}
```

### ✅ Repository Layer

```java
@Repository
public interface ReclamationRepository extends 
        JpaRepository<Reclamation, String>,
        JpaSpecificationExecutor<Reclamation> {
    
    // ✅ Query methods
    List<Reclamation> findByStatRec_CodSta(String codSta);
    
    // ✅ Custom query avec JOIN FETCH (évite N+1)
    @Query("SELECT r FROM Reclamation r " +
           "LEFT JOIN FETCH r.statRec " +
           "LEFT JOIN FETCH r.objet " +
           "LEFT JOIN FETCH r.canal " +
           "WHERE r.refRec = :refRec")
    Optional<Reclamation> findByIdWithDetails(@Param("refRec") String refRec);
    
    // ✅ Query native si nécessaire
    @Query(value = "SELECT COUNT(*) FROM rec_reclamation " +
                   "WHERE sta_rec = :codSta " +
                   "AND dat_rec BETWEEN :start AND :end", 
           nativeQuery = true)
    Long countByStatusAndDateRange(
        @Param("codSta") String codSta,
        @Param("start") Date start,
        @Param("end") Date end
    );
}
```

---


## 4. Gestion Transactionnelle

### 📌 Quand Utiliser @Transactional

#### ✅ Utilisations Correctes

```java
@Service
@Transactional(readOnly = true)  // ✅ Par défaut en lecture
public class ReclamationServiceImpl implements ReclamationService {
    
    // ✅ Lecture simple : utilise @Transactional(readOnly=true) de la classe
    @Override
    public ReclamationResponseDTO findById(String refRec) {
        Reclamation rec = reclamationRepository.findById(refRec)
            .orElseThrow(() -> new ResourceNotFoundException("Réclamation", refRec));
        return mapper.toDto(rec);
    }
    
    // ✅ Écriture : override avec @Transactional
    @Override
    @Transactional
    public ReclamationResponseDTO create(ReclamationRequestDTO dto) {
        Reclamation rec = mapper.toEntity(dto);
        Reclamation saved = reclamationRepository.save(rec);
        return mapper.toDto(saved);
    }
    
    // ✅ Écriture multiple : une seule transaction
    @Override
    @Transactional
    public void updateStatusAndNotify(String refRec, String newStatus) {
        // 1. Mettre à jour statut
        Reclamation rec = reclamationRepository.findById(refRec)
            .orElseThrow(() -> new ResourceNotFoundException("Réclamation", refRec));
        rec.setStatRec(newStatus);
        reclamationRepository.save(rec);
        
        // 2. Enregistrer historique
        StatutHistory history = new StatutHistory();
        history.setRefRec(refRec);
        history.setNewStatus(newStatus);
        statutHistoryRepository.save(history);
        
        // 3. Notifier client (si échec, rollback automatique)
        notificationService.sendEmail(rec.getEmailRec(), newStatus);
    }
    
    // ✅ Propagation REQUIRES_NEW pour opération indépendante
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(String action, String details) {
        AuditLog log = new AuditLog(action, details);
        auditRepository.save(log);
        // Cette transaction est indépendante, ne rollback pas si parent échoue
    }
}
```

#### ❌ Erreurs Courantes

```java
// ❌ @Transactional sur méthode privée (ne fonctionne pas)
@Transactional
private void updateInternal() { }

// ❌ @Transactional sur Controller (mauvaise séparation)
@RestController
public class ReclamationController {
    @Transactional  // ❌ Non ! Transactions = Service
    @PostMapping
    public ResponseEntity<Reclamation> create() { }
}

// ❌ Try-catch qui avale l'exception (rollback ne se fait pas)
@Transactional
public void update() {
    try {
        reclamationRepository.save(rec);
    } catch (Exception e) {
        // ❌ Exception avalée, transaction commit quand même
        log.error("Erreur", e);
    }
}
```

### 📊 Matrice @Transactional

| Opération | @Transactional | readOnly | Propagation | Raison |
|-----------|----------------|----------|-------------|--------|
| **Lecture simple** | Oui | true | REQUIRED | Performance |
| **Écriture simple** | Oui | false | REQUIRED | Intégrité |
| **Écriture multiple** | Oui | false | REQUIRED | Atomicité |
| **Audit indépendant** | Oui | false | REQUIRES_NEW | Isolation |
| **Lecture sans transaction** | Non | - | - | Rare, petite table |

---

## 5. Optimisation JPA

### 🔥 LAZY vs EAGER

#### Règle d'Or : **LAZY par défaut**

```java
@Entity
@Table(name = "rec_reclamation")
public class Reclamation {
    
    // ✅ LAZY par défaut (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sta_rec")
    private Statut statRec;
    
    // ✅ LAZY par défaut (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_objRec")
    private Objet objet;
    
    // ❌ EAGER : à éviter sauf exception
    @ManyToOne(fetch = FetchType.EAGER)  // ❌ Charge toujours
    @JoinColumn(name = "nat_rec")
    private NatureReclamant natureReclamant;
    
    // ✅ LAZY pour collections (OneToMany)
    @OneToMany(mappedBy = "reclamation", fetch = FetchType.LAZY)
    private Set<Attachment> attachments = new HashSet<>();
}
```

#### 📌 Quand Utiliser EAGER

| Cas | Utiliser EAGER | Exemple |
|-----|----------------|---------|
| **Relation toujours nécessaire** | ✅ Oui | User → Roles (toujours chargés) |
| **Petite table référence** | ✅ Oui | Statut (5 lignes) |
| **Relation optionnelle** | ❌ Non | Reclamation → Agent |
| **Collections** | ❌ Jamais | OneToMany, ManyToMany |

### 🚫 Problème N+1 et Solutions

#### ❌ Problème N+1

```java
// ❌ Génère N+1 requêtes SQL
List<Reclamation> reclamations = reclamationRepository.findAll();
for (Reclamation rec : reclamations) {
    String statut = rec.getStatRec().getLibSta();  // SELECT pour chaque réclamation
    String objet = rec.getObjet().getLibObj();     // SELECT pour chaque réclamation
}
// Résultat : 1 SELECT réclamations + N SELECT statuts + N SELECT objets
```

#### ✅ Solution 1 : JOIN FETCH

```java
@Query("SELECT r FROM Reclamation r " +
       "LEFT JOIN FETCH r.statRec " +
       "LEFT JOIN FETCH r.objet " +
       "LEFT JOIN FETCH r.canal " +
       "WHERE r.datRec BETWEEN :start AND :end")
List<Reclamation> findByDateRangeWithDetails(
    @Param("start") Date start,
    @Param("end") Date end
);
// Résultat : 1 seule requête SQL avec JOIN
```

#### ✅ Solution 2 : @EntityGraph

```java
@EntityGraph(attributePaths = {"statRec", "objet", "canal", "natureReclamant"})
List<Reclamation> findByStatRec_CodSta(String codSta);
// Charge automatiquement les relations spécifiées
```

#### ✅ Solution 3 : DTO Projection

```java
@Query("SELECT new tn.reclamationapp.dto.ReclamationListDTO(" +
       "r.refRec, r.nomRec, r.prenRec, r.emailRec, r.datRec, " +
       "s.libSta, o.libObj) " +
       "FROM Reclamation r " +
       "JOIN r.statRec s " +
       "JOIN r.objet o " +
       "WHERE r.datRec BETWEEN :start AND :end")
List<ReclamationListDTO> findReclamationsList(
    @Param("start") Date start,
    @Param("end") Date end
);
// Charge uniquement les colonnes nécessaires (performance optimale)
```

### 📊 Comparaison Performance

| Méthode | Requêtes SQL | Performance | Utilisation |
|---------|--------------|-------------|-------------|
| **Sans optimisation** | 1 + N + N | ❌ Lente | À éviter |
| **JOIN FETCH** | 1 | ✅ Rapide | Détails entité |
| **@EntityGraph** | 1 | ✅ Rapide | Cas simples |
| **DTO Projection** | 1 | ✅✅ Très rapide | Listes, reporting |

---

## 6. DTOs et Mappers

### 📦 Pourquoi Utiliser les DTOs ?

✅ **Sécurité** : Ne pas exposer mot de passe, champs sensibles  
✅ **Flexibilité** : Format différent de l'entité  
✅ **Performance** : Moins de données transférées  
✅ **Versioning** : API V1, V2 avec DTOs différents  
✅ **Validation** : Validation spécifique par cas d'usage  

### 🏗️ Structure DTOs

```java
// ========== REQUEST DTOs ==========

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReclamationRequestDTO {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nomRec;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenRec;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String emailRec;
    
    @Pattern(regexp = "^\\d{8,15}$", message = "Numéro téléphone invalide")
    private String gsmRec;
    
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 2000)
    private String descRec;
    
    @NotNull(message = "Le canal est obligatoire")
    private Long canalId;
    
    @NotNull(message = "L'objet est obligatoire")
    private Long objetId;
    
    @NotNull(message = "La nature du réclamant est obligatoire")
    private Long natureReclamantId;
    
    // Pas de statut : défini automatiquement à NOUVEAU
    // Pas de dates : générées automatiquement
    // Pas d'IDs internes
}

// ========== RESPONSE DTOs ==========

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReclamationResponseDTO {
    
    private String refRec;
    private String nomRec;
    private String prenRec;
    private String emailRec;
    private String gsmRec;
    private String descRec;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date datRec;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date datRep;
    
    // Relations : uniquement infos utiles
    private StatutDTO statut;
    private ObjetDTO objet;
    private CanalDTO canal;
    private NatureReclamantDTO natureReclamant;
    
    // Pas de user_cre, user_mod (sécurité)
    // Pas de champs techniques inutiles
}

@Data
@Builder
public class StatutDTO {
    private String codSta;
    private String libSta;
}

// ========== DTO Liste (pagination) ==========

@Data
@Builder
public class ReclamationListItemDTO {
    private String refRec;
    private String nomRec;
    private String prenRec;
    private String emailRec;
    private Date datRec;
    private String statutLibelle;  // Juste le libellé
    private String objetLibelle;
    
    // Minimal : performance optimale
}
```

### 🔄 Mappers (MapStruct Recommandé)

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReclamationMapper {
    
    // Entity → DTO
    @Mapping(target = "statut", source = "statRec")
    @Mapping(target = "natureReclamant", source = "natureReclamant")
    ReclamationResponseDTO toDto(Reclamation reclamation);
    
    // Request DTO → Entity
    @Mapping(target = "refRec", ignore = true)  // Généré auto
    @Mapping(target = "datRec", ignore = true)  // Généré auto
    @Mapping(target = "statRec", ignore = true) // Défini dans service
    @Mapping(target = "canal", source = "canalId", qualifiedByName = "canalIdToCanal")
    @Mapping(target = "objet", source = "objetId", qualifiedByName = "objetIdToObjet")
    Reclamation toEntity(ReclamationRequestDTO dto);
    
    // Custom mapping methods
    @Named("canalIdToCanal")
    default Canal mapCanal(Long canalId) {
        if (canalId == null) return null;
        Canal canal = new Canal();
        canal.setId(canalId);
        return canal;
    }
    
    List<ReclamationResponseDTO> toDtoList(List<Reclamation> reclamations);
}
```

### 🛠️ Alternative : Mapper Manuel

```java
@Component
@RequiredArgsConstructor
public class ReclamationMapperImpl {
    
    private final StatutRepository statutRepository;
    private final CanalRepository canalRepository;
    
    public ReclamationResponseDTO toDto(Reclamation entity) {
        if (entity == null) return null;
        
        return ReclamationResponseDTO.builder()
            .refRec(entity.getRefRec())
            .nomRec(entity.getNomRec())
            .prenRec(entity.getPrenRec())
            .emailRec(entity.getEmailRec())
            .gsmRec(String.valueOf(entity.getGsmRec()))
            .descRec(entity.getDescRec())
            .datRec(entity.getDatRec())
            .statut(toStatutDto(entity.getStatRec()))
            .objet(toObjetDto(entity.getObjet()))
            .build();
    }
    
    public Reclamation toEntity(ReclamationRequestDTO dto) {
        if (dto == null) return null;
        
        Reclamation entity = new Reclamation();
        entity.setNomRec(dto.getNomRec());
        entity.setPrenRec(dto.getPrenRec());
        entity.setEmailRec(dto.getEmailRec());
        entity.setGsmRec(Long.parseLong(dto.getGsmRec()));
        entity.setDescRec(dto.getDescRec());
        
        // Charger relations
        Canal canal = canalRepository.findById(dto.getCanalId())
            .orElseThrow(() -> new ResourceNotFoundException("Canal", dto.getCanalId()));
        entity.setCanal(canal);
        
        return entity;
    }
}
```

---


## 7. Gestion des Exceptions

### 🎯 Hiérarchie des Exceptions

```java
// ========== Exception de Base ==========
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    protected BusinessException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}

// ========== Exceptions Spécifiques ==========

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(
            String.format("%s avec ID '%s' introuvable", resource, id),
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND
        );
    }
}

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }
}

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message, "CONFLICT", HttpStatus.CONFLICT);
    }
}

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}
```

### 🛡️ GlobalExceptionHandler Complet

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // ========== Exceptions Métier ==========
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ex.getHttpStatus().value())
            .error(ex.getHttpStatus().getReasonPhrase())
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .build();
            
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ex.getHttpStatus().value())
            .error(ex.getHttpStatus().getReasonPhrase())
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .build();
            
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }
    
    // ========== Validation Errors ==========
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Erreur de validation des données")
            .errorCode("VALIDATION_ERROR")
            .details(errors)
            .build();
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // ========== Sécurité ==========
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message("Accès refusé")
            .errorCode("ACCESS_DENIED")
            .build();
            
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    // ========== Database Errors ==========
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Violation de contrainte d'intégrité";
        if (ex.getMessage().contains("Duplicate entry")) {
            message = "Cette entrée existe déjà";
        } else if (ex.getMessage().contains("foreign key constraint")) {
            message = "Impossible de supprimer : des données liées existent";
        }
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(message)
            .errorCode("DATA_INTEGRITY_VIOLATION")
            .build();
            
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    // ========== Generic Exception ==========
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("Une erreur inattendue s'est produite")
            .errorCode("INTERNAL_ERROR")
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// ========== Error Response DTO ==========

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int status;
    private String error;
    private String message;
    private String errorCode;
    private String path;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> details;  // Pour validation errors
}
```

### 📋 Utilisation dans les Services

```java
@Service
@RequiredArgsConstructor
public class ReclamationServiceImpl implements ReclamationService {
    
    private final ReclamationRepository reclamationRepository;
    
    @Override
    public ReclamationResponseDTO findById(String refRec) {
        // ✅ Lance ResourceNotFoundException si introuvable
        Reclamation rec = reclamationRepository.findById(refRec)
            .orElseThrow(() -> new ResourceNotFoundException("Réclamation", refRec));
        
        return mapper.toDto(rec);
    }
    
    @Override
    @Transactional
    public ReclamationResponseDTO create(ReclamationRequestDTO dto) {
        // Validation métier
        if (reclamationRepository.existsByEmailRecAndDatRecAfter(
                dto.getEmailRec(), Date.from(Instant.now().minus(1, ChronoUnit.DAYS)))) {
            throw new ConflictException(
                "Une réclamation existe déjà pour cet email dans les dernières 24h"
            );
        }
        
        // Vérifier existence canal
        Canal canal = canalRepository.findById(dto.getCanalId())
            .orElseThrow(() -> new ResourceNotFoundException("Canal", dto.getCanalId()));
        
        // Suite du traitement...
    }
}
```

---

## 8. Sécurité JWT

### 🔐 Configuration Sécurité Complète

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Admin only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                
                // Agent et Admin
                .requestMatchers("/api/reclamations/**").hasAnyRole("ADMIN", "AGENT", "RESPONSABLE")
                
                // Tous authentifiés
                .anyRequest().authenticated()
            )
            
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthEntryPoint)
            )
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4200",
            "https://reclamations.tsb.tn"  // Production
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Force 12 pour sécurité bancaire
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### 🔑 JwtUtils Amélioré

```java
@Component
@Slf4j
public class JwtUtils {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")  // 24h par défaut
    private long jwtExpiration;
    
    // Générer token
    public String generateToken(UserDetailsImpl userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userDetails.getEmail());
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    // Valider token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    
    // Extraire email
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### 🛡️ AuthenticationFilter

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                String email = jwtUtils.getEmailFromToken(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Cannot set user authentication", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/public/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/");
    }
    
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

---


## 9. Pagination et Recherche

### 📄 Pagination avec Page et Pageable

```java
// ========== Repository ==========
@Repository
public interface ReclamationRepository extends 
        JpaRepository<Reclamation, String>,
        JpaSpecificationExecutor<Reclamation> {
    
    // Pagination simple
    Page<Reclamation> findByStatRec_CodSta(String codSta, Pageable pageable);
    
    // Pagination avec JOIN FETCH (éviter N+1)
    @Query("SELECT DISTINCT r FROM Reclamation r " +
           "LEFT JOIN FETCH r.statRec " +
           "LEFT JOIN FETCH r.objet " +
           "LEFT JOIN FETCH r.canal " +
           "WHERE r.statRec.codSta = :codSta")
    Page<Reclamation> findByStatusWithDetails(
        @Param("codSta") String codSta,
        Pageable pageable
    );
}

// ========== Service ==========
@Service
@RequiredArgsConstructor
public class ReclamationServiceImpl implements ReclamationService {
    
    private final ReclamationRepository reclamationRepository;
    private final ReclamationMapper reclamationMapper;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ReclamationResponseDTO> findAll(
            int page, int size, String sortBy, String direction) {
        
        // Validation
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;  // Max 100 pour performance
        
        // Créer Pageable
        Sort sort = direction.equalsIgnoreCase("DESC") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Récupérer page
        Page<Reclamation> pageResult = reclamationRepository.findAll(pageable);
        
        // Convertir en DTO
        List<ReclamationResponseDTO> content = pageResult.getContent()
            .stream()
            .map(reclamationMapper::toDto)
            .collect(Collectors.toList());
        
        // Créer réponse paginée
        return PageResponseDTO.<ReclamationResponseDTO>builder()
            .content(content)
            .pageNumber(pageResult.getNumber())
            .pageSize(pageResult.getSize())
            .totalElements(pageResult.getTotalElements())
            .totalPages(pageResult.getTotalPages())
            .last(pageResult.isLast())
            .first(pageResult.isFirst())
            .empty(pageResult.isEmpty())
            .build();
    }
}

// ========== Controller ==========
@RestController
@RequestMapping("/api/reclamations")
@RequiredArgsConstructor
public class ReclamationController {
    
    private final ReclamationService reclamationService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(summary = "Liste paginée des réclamations")
    public ResponseEntity<PageResponseDTO<ReclamationResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "datRec") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        PageResponseDTO<ReclamationResponseDTO> response = 
            reclamationService.findAll(page, size, sortBy, direction);
        
        return ResponseEntity.ok(response);
    }
}

// ========== DTO Réponse Paginée ==========
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;
}
```

### 🔍 Recherche Dynamique avec Specification

```java
// ========== Specification Builder ==========
@Component
public class ReclamationSpecification {
    
    public static Specification<Reclamation> hasFilters(
            String refRec,
            String email,
            String statut,
            Date dateDebut,
            Date dateFin,
            String nomPrenom) {
        
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtre par référence
            if (StringUtils.hasText(refRec)) {
                predicates.add(cb.like(
                    cb.lower(root.get("refRec")), 
                    "%" + refRec.toLowerCase() + "%"
                ));
            }
            
            // Filtre par email
            if (StringUtils.hasText(email)) {
                predicates.add(cb.equal(
                    cb.lower(root.get("emailRec")), 
                    email.toLowerCase()
                ));
            }
            
            // Filtre par statut
            if (StringUtils.hasText(statut)) {
                predicates.add(cb.equal(
                    root.get("statRec").get("codSta"), 
                    statut
                ));
            }
            
            // Filtre par période
            if (dateDebut != null && dateFin != null) {
                predicates.add(cb.between(
                    root.get("datRec"), 
                    dateDebut, 
                    dateFin
                ));
            } else if (dateDebut != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("datRec"), 
                    dateDebut
                ));
            } else if (dateFin != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("datRec"), 
                    dateFin
                ));
            }
            
            // Filtre par nom ou prénom
            if (StringUtils.hasText(nomPrenom)) {
                String pattern = "%" + nomPrenom.toLowerCase() + "%";
                Predicate nomPredicate = cb.like(
                    cb.lower(root.get("nomRec")), pattern
                );
                Predicate prenomPredicate = cb.like(
                    cb.lower(root.get("prenRec")), pattern
                );
                predicates.add(cb.or(nomPredicate, prenomPredicate));
            }
            
            // JOIN FETCH pour éviter N+1
            if (query != null) {
                query.distinct(true);
                root.fetch("statRec", JoinType.LEFT);
                root.fetch("objet", JoinType.LEFT);
                root.fetch("canal", JoinType.LEFT);
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

// ========== Service ==========
@Override
@Transactional(readOnly = true)
public PageResponseDTO<ReclamationResponseDTO> search(
        ReclamationSearchCriteria criteria,
        int page, int size, String sortBy, String direction) {
    
    // Créer specification
    Specification<Reclamation> spec = ReclamationSpecification.hasFilters(
        criteria.getRefRec(),
        criteria.getEmail(),
        criteria.getStatut(),
        criteria.getDateDebut(),
        criteria.getDateFin(),
        criteria.getNomPrenom()
    );
    
    // Créer pageable
    Sort sort = direction.equalsIgnoreCase("DESC") 
        ? Sort.by(sortBy).descending() 
        : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    
    // Rechercher
    Page<Reclamation> pageResult = reclamationRepository.findAll(spec, pageable);
    
    // Convertir en DTO
    List<ReclamationResponseDTO> content = pageResult.getContent()
        .stream()
        .map(reclamationMapper::toDto)
        .collect(Collectors.toList());
    
    return PageResponseDTO.<ReclamationResponseDTO>builder()
        .content(content)
        .pageNumber(pageResult.getNumber())
        .pageSize(pageResult.getSize())
        .totalElements(pageResult.getTotalElements())
        .totalPages(pageResult.getTotalPages())
        .last(pageResult.isLast())
        .first(pageResult.isFirst())
        .empty(pageResult.isEmpty())
        .build();
}

// ========== Controller ==========
@GetMapping("/search")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
@Operation(summary = "Recherche dynamique avec filtres multiples")
public ResponseEntity<PageResponseDTO<ReclamationResponseDTO>> search(
        @RequestParam(required = false) String refRec,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String statut,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin,
        @RequestParam(required = false) String nomPrenom,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "datRec") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction) {
    
    ReclamationSearchCriteria criteria = ReclamationSearchCriteria.builder()
        .refRec(refRec)
        .email(email)
        .statut(statut)
        .dateDebut(dateDebut)
        .dateFin(dateFin)
        .nomPrenom(nomPrenom)
        .build();
    
    PageResponseDTO<ReclamationResponseDTO> response = 
        reclamationService.search(criteria, page, size, sortBy, direction);
    
    return ResponseEntity.ok(response);
}

// ========== Critères de Recherche DTO ==========
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReclamationSearchCriteria {
    private String refRec;
    private String email;
    private String statut;
    private Date dateDebut;
    private Date dateFin;
    private String nomPrenom;
}
```

---

## 10. Audit et Logging

### 📝 Audit Automatique avec JPA

```java
// ========== Entité Abstraite Auditable ==========
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class Auditable {
    
    @CreatedDate
    @Column(name = "dat_cre", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date datCre;
    
    @LastModifiedDate
    @Column(name = "dat_mod")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datMod;
    
    @CreatedBy
    @Column(name = "user_cre", updatable = false)
    private String userCre;
    
    @LastModifiedBy
    @Column(name = "user_mod")
    private String userMod;
}

// ========== Configuration Audit ==========
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}

@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("SYSTEM");
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return Optional.of(userDetails.getId());  // ID utilisateur
    }
}

// ========== Utilisation dans Reclamation ==========
@Entity
@Table(name = "rec_reclamation")
public class Reclamation extends Auditable {
    
    @Id
    @Column(name = "ref_rec")
    private String refRec;
    
    // ... autres champs
    
    // datCre, datMod, userCre, userMod gérés automatiquement !
}
```

### 📊 Logging Professionnel

```java
// ========== Configuration Logback (logback-spring.xml) ==========
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/reclamation-app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/reclamation-app-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy 
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Security Audit Appender -->
    <appender name="SECURITY_AUDIT" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/security-audit.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/security-audit-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>90</maxHistory>  <!-- 3 mois pour conformité -->
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Logger pour sécurité -->
    <logger name="tn.reclamationapp.security" level="INFO" additivity="false">
        <appender-ref ref="SECURITY_AUDIT"/>
    </logger>
    
    <!-- Root logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
    
    <!-- Niveaux spécifiques -->
    <logger name="tn.reclamationapp" level="DEBUG"/>
    <logger name="org.springframework.web" level="INFO"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>  <!-- SQL queries -->
    <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE"/>  <!-- Params -->
</configuration>

// ========== Service avec Logging ==========
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReclamationServiceImpl implements ReclamationService {
    
    private final ReclamationRepository reclamationRepository;
    
    @Override
    @Transactional
    public ReclamationResponseDTO create(ReclamationRequestDTO dto) {
        log.info("Création réclamation - Email: {}, Canal: {}", 
            dto.getEmailRec(), dto.getCanalId());
        
        try {
            // Validation
            validateReclamation(dto);
            
            // Création
            Reclamation reclamation = mapper.toEntity(dto);
            reclamation.setRefRec(generateReference());
            
            // Sauvegarde
            Reclamation saved = reclamationRepository.save(reclamation);
            
            log.info("Réclamation créée avec succès - Ref: {}", saved.getRefRec());
            
            return mapper.toDto(saved);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de réclamation pour email: {}", 
                dto.getEmailRec(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void updateStatus(String refRec, String newStatus) {
        log.info("Mise à jour statut réclamation {} vers {}", refRec, newStatus);
        
        Reclamation rec = findEntityById(refRec);
        String oldStatus = rec.getStatRec().getCodSta();
        
        // Mise à jour
        rec.setStatRec(statutRepository.findByCodSta(newStatus)
            .orElseThrow(() -> new ResourceNotFoundException("Statut", newStatus)));
        
        reclamationRepository.save(rec);
        
        log.info("Statut réclamation {} modifié: {} → {}", refRec, oldStatus, newStatus);
        
        // Audit sécurité pour actions sensibles
        if ("CLOTURE".equals(newStatus)) {
            log.info("AUDIT: Réclamation {} clôturée par utilisateur {}", 
                refRec, SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }
}
```

---


## 11. Architecture Finale

### 🎯 Rôles et Responsabilités

```
┌─────────────────────────────────────────────────────────────────┐
│                         CONTROLLER                               │
│  📋 Responsabilités:                                             │
│    • Recevoir requêtes HTTP                                      │
│    • Valider paramètres (@Valid)                                 │
│    • Appeler le service approprié                                │
│    • Retourner ResponseEntity<DTO>                               │
│    • Documenter API (@Operation, @Tag)                           │
│                                                                   │
│  ❌ Ne DOIT PAS:                                                 │
│    • Contenir logique métier                                     │
│    • Accéder directement au repository                           │
│    • Gérer les transactions                                      │
│    • Retourner entités JPA                                       │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                          SERVICE                                 │
│  ⚙️ Responsabilités:                                             │
│    • Logique métier principale                                   │
│    • Validation business rules                                   │
│    • Orchestration de plusieurs repositories                     │
│    • Gestion transactions (@Transactional)                       │
│    • Conversion Entity ↔ DTO (via Mapper)                        │
│    • Logging des opérations importantes                          │
│    • Lancer exceptions métier                                    │
│                                                                   │
│  ❌ Ne DOIT PAS:                                                 │
│    • Gérer HTTP (status codes, headers)                          │
│    • Retourner entités JPA au controller                         │
│    • Contenir requêtes SQL directes                              │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        REPOSITORY                                │
│  💾 Responsabilités:                                             │
│    • Accès base de données                                       │
│    • Requêtes JPQL/SQL                                           │
│    • Query methods (findBy...)                                   │
│    • Custom queries (@Query)                                     │
│    • Optimisation fetch (JOIN FETCH, @EntityGraph)               │
│                                                                   │
│  ❌ Ne DOIT PAS:                                                 │
│    • Contenir logique métier                                     │
│    • Gérer transactions (c'est le service)                       │
│    • Valider données                                             │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                          ENTITY                                  │
│  🗄️ Responsabilités:                                             │
│    • Mapping table ↔ objet Java                                  │
│    • Définir relations JPA                                       │
│    • Contraintes base données                                    │
│                                                                   │
│  ❌ Ne DOIT PAS:                                                 │
│    • Contenir logique métier                                     │
│    • Être exposée directement au client                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                            DTO                                   │
│  📦 Responsabilités:                                             │
│    • Représenter données échangées avec client                   │
│    • Validation entrées (@NotNull, @Email...)                    │
│    • Format JSON adapté au client                                │
│    • Masquer données sensibles                                   │
│                                                                   │
│  ✅ Avantages:                                                   │
│    • Découplage API ↔ Model                                      │
│    • Sécurité (pas de password exposé)                           │
│    • Versioning API (DTO V1, DTO V2)                             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                           MAPPER                                 │
│  🔄 Responsabilités:                                             │
│    • Conversion Entity → DTO (toDto)                             │
│    • Conversion DTO → Entity (toEntity)                          │
│    • Mapping relationnel (IDs → Entities)                        │
│                                                                   │
│  💡 Outils:                                                      │
│    • MapStruct (recommandé, performance)                         │
│    • ModelMapper (simple, réflexion)                             │
│    • Manuel (contrôle total)                                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         EXCEPTION                                │
│  ⚠️ Responsabilités:                                             │
│    • Définir exceptions métier custom                            │
│    • GlobalExceptionHandler (@RestControllerAdvice)              │
│    • ErrorResponse standardisé                                   │
│    • Logging erreurs                                             │
│                                                                   │
│  📋 Types:                                                       │
│    • ResourceNotFoundException (404)                             │
│    • BadRequestException (400)                                   │
│    • ConflictException (409)                                     │
│    • UnauthorizedException (401)                                 │
│    • ForbiddenException (403)                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       CONFIGURATION                              │
│  ⚙️ Responsabilités:                                             │
│    • SecurityConfig (JWT, CORS, autorisation)                    │
│    • JpaConfig (audit, datasource)                               │
│    • WebConfig (CORS, intercepteurs)                             │
│    • OpenApiConfig (Swagger documentation)                       │
│                                                                   │
│  📁 Fichiers:                                                    │
│    • application.properties (config générale)                    │
│    • application-dev.properties (développement)                  │
│    • application-prod.properties (production)                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 12. Conseils Professionnels

### 🏦 Spécificités Bancaires (Conformité BCT 2022-08)

#### 1. Sécurité Renforcée

```java
// ✅ Password Policy stricte
@Configuration
public class PasswordPolicyConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Force 12 rounds (lent = sécurisé)
    }
    
    // Validation password personnalisée
    public boolean isPasswordValid(String password) {
        return password.length() >= 12 &&
               password.matches(".*[A-Z].*") &&      // Majuscule
               password.matches(".*[a-z].*") &&      // Minuscule
               password.matches(".*\\d.*") &&        // Chiffre
               password.matches(".*[!@#$%^&*].*");   // Caractère spécial
    }
}

// ✅ Expiration token plus courte pour banque
app.jwt.expiration=3600000  # 1 heure (au lieu de 24h)

// ✅ Audit trail obligatoire
@Aspect
@Component
@Slf4j
public class AuditAspect {
    
    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result"
    )
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        String user = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        
        log.info("AUDIT: User={}, Action={}, Entity={}, Result={}", 
            user, 
            auditable.action(),
            joinPoint.getSignature().getName(),
            result
        );
        
        // Sauvegarder dans table rec_audit
        auditRepository.save(new AuditLog(user, auditable.action(), ...));
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    String action();
}

// Utilisation
@Auditable(action = "UPDATE_RECLAMATION_STATUS")
public ReclamationResponseDTO updateStatus(String refRec, String status) {
    // ...
}
```

#### 2. Conservation des Données (Conformité)

```java
// ✅ Soft delete obligatoire (pas de DELETE physique)
@Entity
@SQLDelete(sql = "UPDATE rec_reclamation SET deleted = true, dat_delete = NOW() WHERE ref_rec = ?")
@Where(clause = "deleted = false")
public class Reclamation extends Auditable {
    
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @Column(name = "dat_delete")
    private Date datDelete;
}

// ✅ Archivage automatique après 5 ans
@Scheduled(cron = "0 0 2 * * *")  // 2h du matin chaque jour
public void archiveOldReclamations() {
    Date fiveYearsAgo = Date.from(Instant.now().minus(1825, ChronoUnit.DAYS));
    
    List<Reclamation> toArchive = reclamationRepository
        .findByStatRec_CodStaAndDatRecBefore("CLOTURE", fiveYearsAgo);
    
    for (Reclamation rec : toArchive) {
        // Copier vers table d'archivage
        ReclamationArchive archive = new ReclamationArchive(rec);
        archiveRepository.save(archive);
        
        // Soft delete
        rec.setDeleted(true);
        rec.setDatDelete(new Date());
        reclamationRepository.save(rec);
    }
    
    log.info("AUDIT: {} réclamations archivées", toArchive.size());
}
```

#### 3. Traçabilité Complète

```sql
-- Table historique statuts (obligatoire BCT)
CREATE TABLE rec_statut_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ref_rec VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    dat_change DATETIME NOT NULL,
    user_change VARCHAR(255) NOT NULL,
    comment TEXT,
    FOREIGN KEY (ref_rec) REFERENCES rec_reclamation(ref_rec),
    FOREIGN KEY (user_change) REFERENCES hab_users(id),
    INDEX idx_ref_rec (ref_rec),
    INDEX idx_dat_change (dat_change)
);
```

```java
// Service avec historique automatique
@Override
@Transactional
public ReclamationResponseDTO updateStatus(String refRec, String newStatus, String comment) {
    Reclamation rec = findEntityById(refRec);
    String oldStatus = rec.getStatRec().getCodSta();
    
    // Mettre à jour statut
    rec.setStatRec(statutRepository.findByCodSta(newStatus)
        .orElseThrow(() -> new ResourceNotFoundException("Statut", newStatus)));
    
    // Enregistrer historique
    StatutHistory history = new StatutHistory();
    history.setRefRec(refRec);
    history.setOldStatus(oldStatus);
    history.setNewStatus(newStatus);
    history.setDatChange(new Date());
    history.setUserChange(getCurrentUserId());
    history.setComment(comment);
    statutHistoryRepository.save(history);
    
    Reclamation saved = reclamationRepository.save(rec);
    
    log.info("AUDIT: Statut réclamation {} modifié: {} → {} par {}", 
        refRec, oldStatus, newStatus, getCurrentUserId());
    
    return mapper.toDto(saved);
}
```

#### 4. Export XML Réglementaire BCT

```java
@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {
    
    @Override
    @Transactional
    public File generateXmlReport(Date startDate, Date endDate) {
        log.info("Génération rapport XML BCT du {} au {}", startDate, endDate);
        
        // 1. Récupérer réclamations clôturées non exportées
        List<Reclamation> reclamations = reclamationRepository
            .findByStatRec_CodStaAndDatRecBetweenAndXmlGeneratedFalse(
                "CLOTURE", startDate, endDate
            );
        
        // 2. Charger paramètres
        int ageJeune = Integer.parseInt(paramGenRepository
            .findByLibParam("AGE_JEUNE")
            .orElseThrow()
            .getValParam());
        
        // 3. Calculer statistiques
        ReclamationDt dto = buildStatistics(reclamations, ageJeune);
        
        // 4. Générer XML via JAXB
        File xmlFile = new File("exports/reclamations_" + 
            new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".xml");
        
        JAXBContext context = JAXBContext.newInstance(ReclamationDt.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(dto, xmlFile);
        
        // 5. Marquer comme exporté
        for (Reclamation rec : reclamations) {
            rec.setXmlGenerated(true);
            rec.setDatGenXml(new Date());
            rec.setUserGenXml(getCurrentUserId());
            
            // Enregistrer dans journal
            ReclamationGenerated gen = new ReclamationGenerated();
            gen.setRefRec(rec.getRefRec());
            gen.setDatGenXml(new Date());
            reclamationGeneratedRepository.save(gen);
        }
        
        reclamationRepository.saveAll(reclamations);
        
        log.info("AUDIT: Rapport XML BCT généré - {} réclamations exportées", 
            reclamations.size());
        
        return xmlFile;
    }
}
```

### 🎯 Checklist Finale Avant Production

#### Sécurité
- [ ] JWT secret fort et unique (min 256 bits)
- [ ] Password BCrypt rounds = 12
- [ ] HTTPS activé (certificat SSL valide)
- [ ] CORS configuré uniquement pour domaines autorisés
- [ ] Rate limiting activé (max 100 req/min par IP)
- [ ] Validation entrées (@Valid) sur tous les endpoints
- [ ] SQL injection protection (JPA = OK)
- [ ] XSS protection (Jackson JSON = OK)

#### Performance
- [ ] Toutes relations en LAZY par défaut
- [ ] JOIN FETCH pour éviter N+1
- [ ] Index sur colonnes de recherche (email, dat_rec, sta_rec)
- [ ] Pagination max 100 items
- [ ] Connection pool configuré (HikariCP)
- [ ] Cache activé (@Cacheable) pour données référence

#### Conformité BCT
- [ ] Audit trail complet (toutes actions tracées)
- [ ] Historique statuts sauvegardé
- [ ] Soft delete (pas de suppression physique)
- [ ] Export XML conforme format BCT
- [ ] Conservation 5 ans minimum
- [ ] Logs sécurité séparés (retention 90 jours)

#### Qualité Code
- [ ] Tests unitaires (services) > 80% couverture
- [ ] Tests intégration (repositories)
- [ ] Documentation Swagger/OpenAPI complète
- [ ] Logging approprié (INFO/DEBUG/ERROR)
- [ ] Exception handling global
- [ ] DTOs pour toutes requêtes/réponses

#### Production
- [ ] application-prod.properties configuré
- [ ] Variables environnement externalisées
- [ ] Base de données production (pas H2)
- [ ] Monitoring activé (Actuator)
- [ ] Health checks configurés
- [ ] Backup automatique quotidien

---

## 📚 Ressources et Documentation

### Standards Utilisés
- Spring Boot 4.1.0
- Java 17
- MySQL 8.0
- JWT (jjwt 0.11.5)
- MapStruct 1.5.5
- Lombok 1.18.30
- OpenAPI 3.0 (springdoc)

### Documentation API
```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Gestion Réclamations Bancaires TSB")
                .version("2.0")
                .description("API conforme circulaire BCT 2022-08")
                .contact(new Contact()
                    .name("TSB IT Department")
                    .email("support@tsb.tn"))
                .license(new License()
                    .name("Propriétaire")
                    .url("https://tsb.tn")))
            .servers(Arrays.asList(
                new Server().url("https://api.tsb.tn").description("Production"),
                new Server().url("http://localhost:8080").description("Développement")
            ))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### Accès Swagger
- **URL Dev:** http://localhost:8080/swagger-ui.html
- **URL Prod:** https://api.tsb.tn/swagger-ui.html

---

## ✅ Résumé Final

### Ce Guide Couvre

✅ **Structure packages professionnelle** avec séparation claire  
✅ **Architecture 3-tiers** respectée (Controller / Service / Repository)  
✅ **DTOs et Mappers** pour découplage API ↔ Model  
✅ **Gestion transactionnelle** optimisée (@Transactional)  
✅ **Optimisation JPA** (LAZY, JOIN FETCH, N+1 évité)  
✅ **Exceptions personnalisées** avec GlobalExceptionHandler  
✅ **Sécurité JWT** complète et robuste  
✅ **Pagination et recherche dynamique** (Specification)  
✅ **Audit trail** automatique et logging professionnel  
✅ **Conformité BCT 2022-08** (export XML, traçabilité, conservation)  

### Prochaines Étapes

1. **Lire** ce guide en entier
2. **Appliquer** la structure de packages recommandée
3. **Refactorer** les services existants (interface + impl)
4. **Créer** les DTOs pour chaque endpoint
5. **Implémenter** les Mappers (MapStruct recommandé)
6. **Ajouter** GlobalExceptionHandler complet
7. **Optimiser** les repositories (JOIN FETCH)
8. **Tester** l'application (unit + integration tests)
9. **Documenter** l'API (Swagger/OpenAPI)
10. **Déployer** en production

### Fichiers Exemples à Créer

Dans le prochain message, je vais créer des exemples concrets pour :
- ReclamationController complet
- ReclamationService + ReclamationServiceImpl
- ReclamationRepository optimisé
- DTOs (Request/Response)
- Mapper (MapStruct)
- GlobalExceptionHandler complet
- Configuration Security

---

**Bon courage pour votre soutenance ! 🎓**

Cette architecture est **professionnelle**, **maintenable**, **sécurisée**, **performante** et **conforme BCT**.

