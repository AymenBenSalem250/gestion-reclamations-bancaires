# 💻 Exemples de Code Complets - Architecture Professionnelle

**Application:** Gestion Réclamations Bancaires TSB  
**Conformité:** BCT 2022-08  
**Date:** 18 juin 2026

---

## 📋 Table des Matières

1. [DTOs Request/Response](#1-dtos-requestresponse)
2. [Mapper MapStruct](#2-mapper-mapstruct)
3. [Repository Optimisé](#3-repository-optimisé)
4. [Service Interface + Impl](#4-service-interface--impl)
5. [Controller Complet](#5-controller-complet)
6. [Exceptions Personnalisées](#6-exceptions-personnalisées)
7. [GlobalExceptionHandler](#7-globalexceptionhandler)
8. [Configuration Sécurité](#8-configuration-sécurité)
9. [Pagination et Recherche](#9-pagination-et-recherche)
10. [Tests Unitaires](#10-tests-unitaires)

---

## 1. DTOs Request/Response

### 📦 ReclamationRequestDTO.java

```java
package tn.reclamationapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReclamationRequestDTO {
    
    // ========== Informations Réclamant ==========
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nomRec;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenRec;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String emailRec;
    
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^\\d{8,15}$", message = "Le numéro doit contenir entre 8 et 15 chiffres")
    private String gsmRec;
    
    @Past(message = "La date de naissance doit être dans le passé")
    private Date datnaisRec;
    
    @Pattern(regexp = "^(M|F)$", message = "Le genre doit être M ou F")
    private String genreRec;
    
    // Entreprise (optionnel si PART)
    @Size(max = 200)
    private String raisocRec;
    
    // Compte bancaire (optionnel)
    @Pattern(regexp = "^\\d{10,20}$", message = "Numéro de compte invalide")
    private String numcptRec;
    
    // ========== Réclamation ==========
    
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 2000, message = "La description doit contenir entre 10 et 2000 caractères")
    private String descRec;
    
    @Size(max = 1000)
    private String obsRec;  // Observations (optionnel)
    
    // ========== Relations (IDs) ==========
    
    @NotNull(message = "La nature du réclamant est obligatoire")
    @Positive(message = "ID nature réclamant invalide")
    private Long natureReclamantId;
    
    @NotNull(message = "Le canal est obligatoire")
    @Positive(message = "ID canal invalide")
    private Long canalId;
    
    @NotNull(message = "L'objet de la réclamation est obligatoire")
    @Positive(message = "ID objet invalide")
    private Long objetId;
    
    // Note: Pas de statut (défini automatiquement à NOUVEAU)
    // Note: Pas de dates (générées automatiquement)
}
```

### 📦 ReclamationResponseDTO.java

```java
package tn.reclamationapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Exclure champs null
public class ReclamationResponseDTO {
    
    // ========== Identifiant ==========
    private String refRec;
    
    // ========== Informations Réclamant ==========
    private String nomRec;
    private String prenRec;
    private String emailRec;
    private String gsmRec;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date datnaisRec;
    
    private String genreRec;
    private String raisocRec;
    private String numcptRec;
    
    // ========== Réclamation ==========
    private String descRec;
    private String obsRec;
    private String compRec;  // Complément/Réponse
    
    // ========== Dates ==========
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date datRec;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date datRep;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date datCre;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date datMod;
    
    // ========== Relations (DTOs imbriqués) ==========
    private StatutDTO statut;
    private ObjetDTO objet;
    private CanalDTO canal;
    private NatureReclamantDTO natureReclamant;
    
    // ========== Notifications ==========
    private Boolean notifiedEmail;
    private Boolean notifiedSms;
    
    // Note: Pas de userCre, userMod (sécurité)
    // Note: Pas de xmlGenerated (interne)
}
```

### 📦 DTOs Imbriqués

```java
package tn.reclamationapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatutDTO {
    private String codSta;
    private String libSta;
    private String msgSta;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjetDTO {
    private Long id;
    private String codObj;
    private String libObj;
    private ServiceDTO service;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private String codSer;
    private String libSer;
    private String email1Ser;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanalDTO {
    private Long id;
    private String libCan;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NatureReclamantDTO {
    private Long id;
    private String codNat;
    private String libNat;
}
```

### 📦 PageResponseDTO.java (Pagination)

```java
package tn.reclamationapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {
    
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;
    
    // Méthode utilitaire
    public static <T> PageResponseDTO<T> of(
            List<T> content, 
            int page, 
            int size, 
            long total) {
        
        int totalPages = (int) Math.ceil((double) total / size);
        
        return PageResponseDTO.<T>builder()
            .content(content)
            .pageNumber(page)
            .pageSize(size)
            .totalElements(total)
            .totalPages(totalPages)
            .last(page >= totalPages - 1)
            .first(page == 0)
            .empty(content.isEmpty())
            .build();
    }
}
```

### 📦 ReclamationUpdateDTO.java

```java
package tn.reclamationapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReclamationUpdateDTO {
    
    @NotBlank
    @Size(min = 2, max = 100)
    private String nomRec;
    
    @NotBlank
    @Size(min = 2, max = 100)
    private String prenRec;
    
    @NotBlank
    @Email
    private String emailRec;
    
    @NotBlank
    @Pattern(regexp = "^\\d{8,15}$")
    private String gsmRec;
    
    @Size(min = 10, max = 2000)
    private String descRec;
    
    @Size(max = 1000)
    private String obsRec;
    
    // Permet de changer l'objet si nécessaire
    @Positive
    private Long objetId;
    
    // Note: Pas de changement statut ici (endpoint dédié)
}
```

---

## 2. Mapper MapStruct

### 🔄 ReclamationMapper.java

```java
package tn.reclamationapp.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import tn.reclamationapp.dto.request.ReclamationRequestDTO;
import tn.reclamationapp.dto.request.ReclamationUpdateDTO;
import tn.reclamationapp.dto.response.*;
import tn.reclamationapp.model.*;
import tn.reclamationapp.repository.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class ReclamationMapper {
    
    @Autowired
    protected CanalRepository canalRepository;
    
    @Autowired
    protected ObjetRepository objetRepository;
    
    @Autowired
    protected NatureReclamantRepository natureReclamantRepository;
    
    @Autowired
    protected StatutRepository statutRepository;
    
    // ========== Entity → DTO ==========
    
    @Mapping(target = "statut", source = "statRec")
    @Mapping(target = "natureReclamant", source = "natureReclamant")
    @Mapping(target = "gsmRec", expression = "java(String.valueOf(entity.getGsmRec()))")
    public abstract ReclamationResponseDTO toDto(Reclamation entity);
    
    public abstract List<ReclamationResponseDTO> toDtoList(List<Reclamation> entities);
    
    // ========== Request DTO → Entity ==========
    
    @Mapping(target = "refRec", ignore = true)  // Généré auto
    @Mapping(target = "datRec", ignore = true)  // Généré auto
    @Mapping(target = "datCre", ignore = true)  // Audit auto
    @Mapping(target = "datMod", ignore = true)  // Audit auto
    @Mapping(target = "userCre", ignore = true)  // Audit auto
    @Mapping(target = "userMod", ignore = true)  // Audit auto
    @Mapping(target = "statRec", ignore = true)  // Défini dans service
    @Mapping(target = "xmlGenerated", ignore = true)
    @Mapping(target = "notifiedEmail", ignore = true)
    @Mapping(target = "notifiedSms", ignore = true)
    @Mapping(target = "canal", source = "canalId", qualifiedByName = "canalIdToCanal")
    @Mapping(target = "objet", source = "objetId", qualifiedByName = "objetIdToObjet")
    @Mapping(target = "natureReclamant", source = "natureReclamantId", qualifiedByName = "natureIdToNature")
    @Mapping(target = "gsmRec", expression = "java(Long.parseLong(dto.getGsmRec()))")
    public abstract Reclamation toEntity(ReclamationRequestDTO dto);
    
    // ========== Update DTO → Entity (partial update) ==========
    
    @Mapping(target = "refRec", ignore = true)
    @Mapping(target = "datRec", ignore = true)
    @Mapping(target = "statRec", ignore = true)
    @Mapping(target = "objet", source = "objetId", qualifiedByName = "objetIdToObjet")
    @Mapping(target = "gsmRec", expression = "java(Long.parseLong(dto.getGsmRec()))")
    public abstract void updateEntityFromDto(ReclamationUpdateDTO dto, @MappingTarget Reclamation entity);
    
    // ========== Mappers Personnalisés ==========
    
    @Named("canalIdToCanal")
    protected Canal mapCanal(Long canalId) {
        if (canalId == null) return null;
        return canalRepository.findById(canalId)
            .orElseThrow(() -> new ResourceNotFoundException("Canal", canalId));
    }
    
    @Named("objetIdToObjet")
    protected Objet mapObjet(Long objetId) {
        if (objetId == null) return null;
        return objetRepository.findById(objetId)
            .orElseThrow(() -> new ResourceNotFoundException("Objet", objetId));
    }
    
    @Named("natureIdToNature")
    protected NatureReclamant mapNatureReclamant(Long natureId) {
        if (natureId == null) return null;
        return natureReclamantRepository.findById(natureId)
            .orElseThrow(() -> new ResourceNotFoundException("NatureReclamant", natureId));
    }
    
    // ========== Mappers Relations ==========
    
    @Mapping(target = "service", source = "service")
    public abstract ObjetDTO toObjetDto(Objet objet);
    
    public abstract ServiceDTO toServiceDto(Service service);
    
    public abstract StatutDTO toStatutDto(Statut statut);
    
    public abstract CanalDTO toCanalDto(Canal canal);
    
    public abstract NatureReclamantDTO toNatureReclamantDto(NatureReclamant nature);
}
```

### 📦 pom.xml (Dépendances MapStruct)

```xml
<dependencies>
    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---


## 3. Repository Optimisé

### 💾 ReclamationRepository.java

```java
package tn.reclamationapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Reclamation;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamationRepository extends 
        JpaRepository<Reclamation, String>,
        JpaSpecificationExecutor<Reclamation> {
    
    // ========== Recherche Optimisée avec JOIN FETCH ==========
    
    /**
     * Recherche par ID avec toutes les relations (évite N+1)
     */
    @Query("SELECT r FROM Reclamation r " +
           "LEFT JOIN FETCH r.statRec " +
           "LEFT JOIN FETCH r.objet o " +
           "LEFT JOIN FETCH o.service " +
           "LEFT JOIN FETCH r.canal " +
           "LEFT JOIN FETCH r.natureReclamant " +
           "WHERE r.refRec = :refRec")
    Optional<Reclamation> findByIdWithDetails(@Param("refRec") String refRec);
    
    /**
     * Recherche par email avec EntityGraph (alternative à JOIN FETCH)
     */
    @EntityGraph(attributePaths = {"statRec", "objet", "objet.service", "canal", "natureReclamant"})
    List<Reclamation> findByEmailRec(String emailRec);
    
    // ========== Recherche par Statut ==========
    
    @Query("SELECT r FROM Reclamation r " +
           "LEFT JOIN FETCH r.statRec s " +
           "LEFT JOIN FETCH r.objet " +
           "LEFT JOIN FETCH r.canal " +
           "WHERE s.codSta = :codSta")
    List<Reclamation> findByStatusWithDetails(@Param("codSta") String codSta);
    
    /**
     * Version paginée
     */
    @EntityGraph(attributePaths = {"statRec", "objet", "canal"})
    Page<Reclamation> findByStatRec_CodSta(String codSta, Pageable pageable);
    
    // ========== Recherche par Période ==========
    
    @Query("SELECT r FROM Reclamation r " +
           "LEFT JOIN FETCH r.statRec " +
           "LEFT JOIN FETCH r.objet " +
           "WHERE r.datRec BETWEEN :startDate AND :endDate " +
           "ORDER BY r.datRec DESC")
    List<Reclamation> findByDateRangeWithDetails(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    Page<Reclamation> findByDatRecBetween(Date startDate, Date endDate, Pageable pageable);
    
    // ========== Recherche par Nom/Prénom ==========
    
    @Query("SELECT r FROM Reclamation r " +
           "WHERE LOWER(r.nomRec) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.prenRec) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Reclamation> searchByNomOrPrenom(@Param("search") String search);
    
    // ========== Statistiques ==========
    
    /**
     * Compter réclamations par statut et période
     */
    @Query("SELECT COUNT(r) FROM Reclamation r " +
           "WHERE r.statRec.codSta = :codSta " +
           "AND r.datRec BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(
        @Param("codSta") String codSta,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    /**
     * Compter réclamations non notifiées
     */
    @Query("SELECT COUNT(r) FROM Reclamation r " +
           "WHERE r.statRec.codSta = :codSta " +
           "AND r.notifiedEmail = false")
    Long countUnnotified(@Param("codSta") String codSta);
    
    // ========== Export XML ==========
    
    /**
     * Réclamations clôturées non exportées
     */
    @Query("SELECT r FROM Reclamation r " +
           "LEFT JOIN FETCH r.statRec " +
           "LEFT JOIN FETCH r.objet " +
           "LEFT JOIN FETCH r.canal " +
           "LEFT JOIN FETCH r.natureReclamant " +
           "WHERE r.statRec.codSta = 'CLOTURE' " +
           "AND r.xmlGenerated = false " +
           "AND r.datRec BETWEEN :startDate AND :endDate")
    List<Reclamation> findForXmlExport(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
    
    // ========== Vérifications ==========
    
    boolean existsByEmailRecAndDatRecAfter(String emailRec, Date dateAfter);
    
    boolean existsByRefRec(String refRec);
    
    // ========== Projections DTO (Performance Optimale) ==========
    
    /**
     * Projection pour liste légère (sans lazy loading)
     */
    @Query("SELECT new tn.reclamationapp.dto.response.ReclamationListItemDTO(" +
           "r.refRec, r.nomRec, r.prenRec, r.emailRec, r.datRec, " +
           "s.libSta, o.libObj) " +
           "FROM Reclamation r " +
           "JOIN r.statRec s " +
           "JOIN r.objet o " +
           "WHERE r.datRec BETWEEN :startDate AND :endDate")
    List<ReclamationListItemDTO> findListItems(
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
}
```

---

## 4. Service Interface + Impl

### 🔧 ReclamationService.java (Interface)

```java
package tn.reclamationapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.reclamationapp.dto.request.ReclamationRequestDTO;
import tn.reclamationapp.dto.request.ReclamationUpdateDTO;
import tn.reclamationapp.dto.response.PageResponseDTO;
import tn.reclamationapp.dto.response.ReclamationResponseDTO;
import tn.reclamationapp.specification.ReclamationSearchCriteria;

import java.util.Date;
import java.util.List;

public interface ReclamationService {
    
    /**
     * Créer une nouvelle réclamation
     * @param dto données de la réclamation
     * @return réclamation créée avec référence générée
     */
    ReclamationResponseDTO create(ReclamationRequestDTO dto);
    
    /**
     * Récupérer une réclamation par sa référence
     * @param refRec référence de la réclamation
     * @return réclamation trouvée
     * @throws ResourceNotFoundException si introuvable
     */
    ReclamationResponseDTO findById(String refRec);
    
    /**
     * Liste paginée de toutes les réclamations
     */
    PageResponseDTO<ReclamationResponseDTO> findAll(int page, int size, String sortBy, String direction);
    
    /**
     * Recherche dynamique avec filtres multiples
     */
    PageResponseDTO<ReclamationResponseDTO> search(
        ReclamationSearchCriteria criteria,
        int page, int size, String sortBy, String direction
    );
    
    /**
     * Mettre à jour une réclamation
     */
    ReclamationResponseDTO update(String refRec, ReclamationUpdateDTO dto);
    
    /**
     * Changer le statut d'une réclamation
     * @param comment commentaire optionnel
     */
    ReclamationResponseDTO updateStatus(String refRec, String newStatus, String comment);
    
    /**
     * Ajouter une réponse à la réclamation
     */
    ReclamationResponseDTO addResponse(String refRec, String response);
    
    /**
     * Marquer comme notifiée
     */
    void markAsNotified(String refRec, boolean email, boolean sms);
    
    /**
     * Supprimer une réclamation (soft delete)
     */
    void delete(String refRec);
    
    /**
     * Réclamations par statut
     */
    List<ReclamationResponseDTO> findByStatus(String codSta);
    
    /**
     * Réclamations par période
     */
    List<ReclamationResponseDTO> findByDateRange(Date startDate, Date endDate);
    
    /**
     * Statistiques
     */
    Long countByStatus(String codSta);
    Long countByStatusAndDateRange(String codSta, Date startDate, Date endDate);
}
```

### ⚙️ ReclamationServiceImpl.java (Implémentation)

```java
package tn.reclamationapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reclamationapp.dto.request.ReclamationRequestDTO;
import tn.reclamationapp.dto.request.ReclamationUpdateDTO;
import tn.reclamationapp.dto.response.PageResponseDTO;
import tn.reclamationapp.dto.response.ReclamationResponseDTO;
import tn.reclamationapp.exception.BadRequestException;
import tn.reclamationapp.exception.ConflictException;
import tn.reclamationapp.exception.ResourceNotFoundException;
import tn.reclamationapp.mapper.ReclamationMapper;
import tn.reclamationapp.model.*;
import tn.reclamationapp.repository.*;
import tn.reclamationapp.security.UserDetailsImpl;
import tn.reclamationapp.specification.ReclamationSearchCriteria;
import tn.reclamationapp.specification.ReclamationSpecification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)  // Par défaut lecture
@RequiredArgsConstructor
@Slf4j
public class ReclamationServiceImpl implements ReclamationService {
    
    private final ReclamationRepository reclamationRepository;
    private final StatutRepository statutRepository;
    private final StatutHistoryRepository statutHistoryRepository;
    private final ReclamationMapper reclamationMapper;
    
    // ========== CREATE ==========
    
    @Override
    @Transactional  // Écriture
    public ReclamationResponseDTO create(ReclamationRequestDTO dto) {
        log.info("Création réclamation - Email: {}, Canal: {}", 
            dto.getEmailRec(), dto.getCanalId());
        
        // Validation métier : pas de réclamation récente pour ce client
        Date yesterday = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
        if (reclamationRepository.existsByEmailRecAndDatRecAfter(dto.getEmailRec(), yesterday)) {
            throw new ConflictException(
                "Une réclamation existe déjà pour cet email dans les dernières 24h"
            );
        }
        
        // Conversion DTO → Entity
        Reclamation reclamation = reclamationMapper.toEntity(dto);
        
        // Génération référence unique
        reclamation.setRefRec(generateReference());
        
        // Définir statut initial
        Statut statutNouveau = statutRepository.findByCodSta("NOUVEAU")
            .orElseThrow(() -> new ResourceNotFoundException("Statut", "NOUVEAU"));
        reclamation.setStatRec(statutNouveau);
        
        // Métadonnées
        reclamation.setDatRec(new Date());
        reclamation.setXmlGenerated(false);
        reclamation.setNotifiedEmail(false);
        reclamation.setNotifiedSms(false);
        
        // Sauvegarde
        Reclamation saved = reclamationRepository.save(reclamation);
        
        log.info("Réclamation créée avec succès - Ref: {}", saved.getRefRec());
        
        // Entity → DTO
        return reclamationMapper.toDto(saved);
    }
    
    // ========== READ ==========
    
    @Override
    public ReclamationResponseDTO findById(String refRec) {
        log.debug("Recherche réclamation: {}", refRec);
        
        Reclamation reclamation = reclamationRepository.findByIdWithDetails(refRec)
            .orElseThrow(() -> new ResourceNotFoundException("Réclamation", refRec));
        
        return reclamationMapper.toDto(reclamation);
    }
    
    @Override
    public PageResponseDTO<ReclamationResponseDTO> findAll(
            int page, int size, String sortBy, String direction) {
        
        log.debug("Liste réclamations - Page: {}, Size: {}", page, size);
        
        // Validation
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;
        
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
    
    @Override
    public PageResponseDTO<ReclamationResponseDTO> search(
            ReclamationSearchCriteria criteria,
            int page, int size, String sortBy, String direction) {
        
        log.debug("Recherche réclamations avec critères: {}", criteria);
        
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
        
        // Convertir
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
    
    // ========== UPDATE ==========
    
    @Override
    @Transactional
    public ReclamationResponseDTO update(String refRec, ReclamationUpdateDTO dto) {
        log.info("Mise à jour réclamation: {}", refRec);
        
        Reclamation reclamation = findEntityById(refRec);
        
        // Mise à jour partielle via mapper
        reclamationMapper.updateEntityFromDto(dto, reclamation);
        
        Reclamation updated = reclamationRepository.save(reclamation);
        
        log.info("Réclamation {} mise à jour avec succès", refRec);
        
        return reclamationMapper.toDto(updated);
    }
    
    @Override
    @Transactional
    public ReclamationResponseDTO updateStatus(String refRec, String newStatus, String comment) {
        log.info("Changement statut réclamation {} vers {}", refRec, newStatus);
        
        Reclamation reclamation = findEntityById(refRec);
        String oldStatus = reclamation.getStatRec().getCodSta();
        
        // Validation transition statut
        validateStatusTransition(oldStatus, newStatus);
        
        // Charger nouveau statut
        Statut statut = statutRepository.findByCodSta(newStatus)
            .orElseThrow(() -> new ResourceNotFoundException("Statut", newStatus));
        
        // Mettre à jour
        reclamation.setStatRec(statut);
        
        // Si TRAITE, mettre date réponse
        if ("TRAITE".equals(newStatus) && reclamation.getDatRep() == null) {
            reclamation.setDatRep(new Date());
        }
        
        Reclamation updated = reclamationRepository.save(reclamation);
        
        // Enregistrer historique
        saveStatusHistory(refRec, oldStatus, newStatus, comment);
        
        log.info("AUDIT: Statut réclamation {} modifié: {} → {} par {}", 
            refRec, oldStatus, newStatus, getCurrentUserId());
        
        return reclamationMapper.toDto(updated);
    }
    
    @Override
    @Transactional
    public ReclamationResponseDTO addResponse(String refRec, String response) {
        log.info("Ajout réponse à réclamation: {}", refRec);
        
        if (response == null || response.trim().isEmpty()) {
            throw new BadRequestException("La réponse ne peut pas être vide");
        }
        
        Reclamation reclamation = findEntityById(refRec);
        
        reclamation.setCompRec(response);
        reclamation.setDatRep(new Date());
        
        // Passer en TRAITE si pas déjà fait
        if (!"TRAITE".equals(reclamation.getStatRec().getCodSta())) {
            Statut statutTraite = statutRepository.findByCodSta("TRAITE")
                .orElseThrow(() -> new ResourceNotFoundException("Statut", "TRAITE"));
            reclamation.setStatRec(statutTraite);
        }
        
        Reclamation updated = reclamationRepository.save(reclamation);
        
        log.info("Réponse ajoutée à réclamation {}", refRec);
        
        return reclamationMapper.toDto(updated);
    }
    
    @Override
    @Transactional
    public void markAsNotified(String refRec, boolean email, boolean sms) {
        Reclamation reclamation = findEntityById(refRec);
        
        if (email) reclamation.setNotifiedEmail(true);
        if (sms) reclamation.setNotifiedSms(true);
        
        reclamationRepository.save(reclamation);
        
        log.info("Réclamation {} marquée comme notifiée (email={}, sms={})", 
            refRec, email, sms);
    }
    
    // ========== DELETE ==========
    
    @Override
    @Transactional
    public void delete(String refRec) {
        log.warn("Suppression réclamation: {}", refRec);
        
        Reclamation reclamation = findEntityById(refRec);
        
        // Soft delete
        reclamation.setDeleted(true);
        reclamation.setDatDelete(new Date());
        
        reclamationRepository.save(reclamation);
        
        log.info("AUDIT: Réclamation {} supprimée par {}", refRec, getCurrentUserId());
    }
    
    // ========== QUERIES ==========
    
    @Override
    public List<ReclamationResponseDTO> findByStatus(String codSta) {
        List<Reclamation> reclamations = reclamationRepository.findByStatusWithDetails(codSta);
        return reclamations.stream()
            .map(reclamationMapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ReclamationResponseDTO> findByDateRange(Date startDate, Date endDate) {
        List<Reclamation> reclamations = reclamationRepository
            .findByDateRangeWithDetails(startDate, endDate);
        return reclamations.stream()
            .map(reclamationMapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public Long countByStatus(String codSta) {
        return reclamationRepository.countByStatusAndDateRange(
            codSta, 
            new Date(0),  // Depuis toujours
            new Date()    // Jusqu'à maintenant
        );
    }
    
    @Override
    public Long countByStatusAndDateRange(String codSta, Date startDate, Date endDate) {
        return reclamationRepository.countByStatusAndDateRange(codSta, startDate, endDate);
    }
    
    // ========== MÉTHODES PRIVÉES ==========
    
    private Reclamation findEntityById(String refRec) {
        return reclamationRepository.findByIdWithDetails(refRec)
            .orElseThrow(() -> new ResourceNotFoundException("Réclamation", refRec));
    }
    
    private String generateReference() {
        return "REC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    private String getCurrentUserId() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
            return userDetails.getId();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
    
    private void validateStatusTransition(String oldStatus, String newStatus) {
        // Règles métier : transitions autorisées
        if ("CLOTURE".equals(oldStatus)) {
            throw new BadRequestException(
                "Impossible de modifier le statut d'une réclamation clôturée"
            );
        }
        
        if ("ANNULE".equals(oldStatus) && !"NOUVEAU".equals(newStatus)) {
            throw new BadRequestException(
                "Une réclamation annulée ne peut être réouverte qu'en statut NOUVEAU"
            );
        }
    }
    
    private void saveStatusHistory(String refRec, String oldStatus, 
                                    String newStatus, String comment) {
        StatutHistory history = new StatutHistory();
        history.setRefRec(refRec);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setDatChange(new Date());
        history.setUserChange(getCurrentUserId());
        history.setComment(comment);
        
        statutHistoryRepository.save(history);
    }
}
```

---


## 5. Controller Complet

### 🎮 ReclamationController.java

```java
package tn.reclamationapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.reclamationapp.dto.request.ReclamationRequestDTO;
import tn.reclamationapp.dto.request.ReclamationUpdateDTO;
import tn.reclamationapp.dto.response.PageResponseDTO;
import tn.reclamationapp.dto.response.ReclamationResponseDTO;
import tn.reclamationapp.exception.ErrorResponse;
import tn.reclamationapp.service.ReclamationService;
import tn.reclamationapp.specification.ReclamationSearchCriteria;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Réclamations", description = "API de gestion des réclamations bancaires")
@SecurityRequirement(name = "bearer-jwt")
public class ReclamationController {
    
    private final ReclamationService reclamationService;
    
    // ========== CREATE ==========
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(
        summary = "Créer une nouvelle réclamation",
        description = "Enregistre une nouvelle réclamation client avec génération automatique de la référence"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Réclamation créée avec succès",
            content = @Content(schema = @Schema(implementation = ReclamationResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Données invalides",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflit - Réclamation récente existe déjà",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ReclamationResponseDTO> create(
            @Valid @RequestBody ReclamationRequestDTO dto) {
        
        log.info("REST: Création réclamation - Email: {}", dto.getEmailRec());
        
        ReclamationResponseDTO created = reclamationService.create(dto);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }
    
    // ========== READ ==========
    
    @GetMapping("/{refRec}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE', 'CLIENT')")
    @Operation(
        summary = "Récupérer une réclamation par sa référence",
        description = "Retourne le détail complet d'une réclamation"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Réclamation trouvée",
            content = @Content(schema = @Schema(implementation = ReclamationResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Réclamation introuvable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ReclamationResponseDTO> findById(
            @Parameter(description = "Référence de la réclamation", example = "REC1718743200123")
            @PathVariable String refRec) {
        
        log.debug("REST: Recherche réclamation: {}", refRec);
        
        ReclamationResponseDTO response = reclamationService.findById(refRec);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(
        summary = "Liste paginée des réclamations",
        description = "Retourne toutes les réclamations avec pagination et tri"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = PageResponseDTO.class))
        )
    })
    public ResponseEntity<PageResponseDTO<ReclamationResponseDTO>> findAll(
            @Parameter(description = "Numéro de page (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") @Positive int size,
            
            @Parameter(description = "Champ de tri", example = "datRec")
            @RequestParam(defaultValue = "datRec") String sortBy,
            
            @Parameter(description = "Direction du tri", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.debug("REST: Liste réclamations - Page: {}, Size: {}", page, size);
        
        PageResponseDTO<ReclamationResponseDTO> response = 
            reclamationService.findAll(page, size, sortBy, direction);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(
        summary = "Recherche dynamique avec filtres multiples",
        description = "Recherche avancée de réclamations par référence, email, statut, période, nom..."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Résultats de recherche",
            content = @Content(schema = @Schema(implementation = PageResponseDTO.class))
        )
    })
    public ResponseEntity<PageResponseDTO<ReclamationResponseDTO>> search(
            @Parameter(description = "Référence réclamation") 
            @RequestParam(required = false) String refRec,
            
            @Parameter(description = "Email du réclamant") 
            @RequestParam(required = false) String email,
            
            @Parameter(description = "Code statut", example = "NOUVEAU") 
            @RequestParam(required = false) String statut,
            
            @Parameter(description = "Date début (format: yyyy-MM-dd)", example = "2026-01-01") 
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            
            @Parameter(description = "Date fin (format: yyyy-MM-dd)", example = "2026-12-31") 
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin,
            
            @Parameter(description = "Nom ou prénom") 
            @RequestParam(required = false) String nomPrenom,
            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Positive int size,
            @RequestParam(defaultValue = "datRec") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.debug("REST: Recherche réclamations avec critères");
        
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
    
    @GetMapping("/status/{codSta}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(
        summary = "Réclamations par statut",
        description = "Retourne toutes les réclamations ayant un statut donné"
    )
    public ResponseEntity<List<ReclamationResponseDTO>> findByStatus(
            @Parameter(description = "Code statut", example = "NOUVEAU")
            @PathVariable String codSta) {
        
        log.debug("REST: Réclamations par statut: {}", codSta);
        
        List<ReclamationResponseDTO> response = reclamationService.findByStatus(codSta);
        
        return ResponseEntity.ok(response);
    }
    
    // ========== UPDATE ==========
    
    @PutMapping("/{refRec}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(
        summary = "Mettre à jour une réclamation",
        description = "Modifie les informations d'une réclamation existante"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Réclamation mise à jour",
            content = @Content(schema = @Schema(implementation = ReclamationResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Réclamation introuvable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ReclamationResponseDTO> update(
            @PathVariable String refRec,
            @Valid @RequestBody ReclamationUpdateDTO dto) {
        
        log.info("REST: Mise à jour réclamation: {}", refRec);
        
        ReclamationResponseDTO updated = reclamationService.update(refRec, dto);
        
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{refRec}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(
        summary = "Changer le statut d'une réclamation",
        description = "Fait évoluer le statut d'une réclamation dans le workflow"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Statut mis à jour",
            content = @Content(schema = @Schema(implementation = ReclamationResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Transition de statut invalide",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ReclamationResponseDTO> updateStatus(
            @PathVariable String refRec,
            
            @Parameter(description = "Nouveau statut", example = "EN_COURS", required = true)
            @RequestParam String codSta,
            
            @Parameter(description = "Commentaire optionnel")
            @RequestParam(required = false) String comment) {
        
        log.info("REST: Changement statut {} vers {}", refRec, codSta);
        
        ReclamationResponseDTO updated = 
            reclamationService.updateStatus(refRec, codSta, comment);
        
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{refRec}/response")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'RESPONSABLE')")
    @Operation(
        summary = "Ajouter une réponse à la réclamation",
        description = "Enregistre la réponse de la banque à la réclamation"
    )
    public ResponseEntity<ReclamationResponseDTO> addResponse(
            @PathVariable String refRec,
            
            @Parameter(description = "Texte de la réponse", required = true)
            @RequestBody String response) {
        
        log.info("REST: Ajout réponse à réclamation: {}", refRec);
        
        ReclamationResponseDTO updated = reclamationService.addResponse(refRec, response);
        
        return ResponseEntity.ok(updated);
    }
    
    // ========== DELETE ==========
    
    @DeleteMapping("/{refRec}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Supprimer une réclamation (soft delete)",
        description = "Marque une réclamation comme supprimée (conservation données BCT)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Réclamation supprimée"),
        @ApiResponse(responseCode = "404", description = "Réclamation introuvable")
    })
    public ResponseEntity<Void> delete(@PathVariable String refRec) {
        log.warn("REST: Suppression réclamation: {}", refRec);
        
        reclamationService.delete(refRec);
        
        return ResponseEntity.noContent().build();
    }
    
    // ========== STATISTIQUES ==========
    
    @GetMapping("/statistics/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    @Operation(
        summary = "Compter réclamations par statut",
        description = "Statistiques : nombre de réclamations par statut"
    )
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Code statut", example = "NOUVEAU")
            @RequestParam String codSta) {
        
        log.debug("REST: Statistiques - Count by status: {}", codSta);
        
        Long count = reclamationService.countByStatus(codSta);
        
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/statistics/count-by-period")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    @Operation(
        summary = "Compter réclamations par statut et période",
        description = "Statistiques : nombre de réclamations sur une période donnée"
    )
    public ResponseEntity<Long> countByStatusAndPeriod(
            @RequestParam String codSta,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        log.debug("REST: Statistiques - Count by status {} and period {} to {}", 
            codSta, startDate, endDate);
        
        Long count = reclamationService.countByStatusAndDateRange(codSta, startDate, endDate);
        
        return ResponseEntity.ok(count);
    }
}
```

---

## 6. Exceptions Personnalisées

### ⚠️ BusinessException.java (Classe Mère)

```java
package tn.reclamationapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    protected BusinessException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    protected BusinessException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
```

### 🚫 ResourceNotFoundException.java

```java
package tn.reclamationapp.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resource, Object id) {
        super(
            String.format("%s avec identifiant '%s' introuvable", resource, id),
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
```

### ❌ BadRequestException.java

```java
package tn.reclamationapp.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST, cause);
    }
}
```

### ⚡ ConflictException.java

```java
package tn.reclamationapp.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    
    public ConflictException(String message) {
        super(message, "CONFLICT", HttpStatus.CONFLICT);
    }
}
```

### 🔒 UnauthorizedException.java

```java
package tn.reclamationapp.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}
```

### 🛡️ ForbiddenException.java

```java
package tn.reclamationapp.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}
```

### 📋 ErrorResponse.java (DTO)

```java
package tn.reclamationapp.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int status;
    private String error;
    private String message;
    private String errorCode;
    private String path;
    
    // Pour erreurs de validation (champ → message)
    private Map<String, String> details;
    
    // Builder personnalisé
    public static ErrorResponse of(int status, String error, String message, String errorCode) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .message(message)
            .errorCode(errorCode)
            .build();
    }
}
```

---

