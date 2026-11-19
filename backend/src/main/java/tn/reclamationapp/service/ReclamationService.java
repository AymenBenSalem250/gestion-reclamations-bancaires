package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reclamationapp.dto.ReclamationRequest;
import tn.reclamationapp.dto.ReclamationResponse;
import tn.reclamationapp.dto.ReclamationUpdateRequest;
import tn.reclamationapp.exception.BusinessException;
import tn.reclamationapp.model.*;
import tn.reclamationapp.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReclamationService {

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private StatutRepository statutRepository;

    @Autowired
    private ObjetRepository objetRepository;

    @Autowired
    private CanalRepository canalRepository;

    @Autowired
    private NatureReclamantRepository natureReclamantRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;


    // ==========================================================
    // ✅ MAPPING CENTRALISÉ : Reclamation → ReclamationResponse
    // Méthode unique réutilisée par getById, getPage, update, updateStatus
    // ==========================================================

    private ReclamationResponse buildToDto(Reclamation rec) {

        ReclamationResponse dto = new ReclamationResponse();

        dto.setRefRec(rec.getRefRec());
        dto.setTypeRec(rec.getTypeRec());
        dto.setDescRec(rec.getDescRec());
        dto.setCompRec(rec.getCompRec());
        dto.setDatRec(rec.getDatRec());
        dto.setDatRep(rec.getDatRep());

        // ✅ Statut — libellé + code (le code permet au front de bloquer la modification)
        if (rec.getStatRec() != null) {
            dto.setStatutDisplay(rec.getStatRec().getLibSta());
            dto.setStatutCode(rec.getStatRec().getCodSta());
        } else {
            dto.setStatutDisplay("-");
        }

        // ✅ Canal — libellé + ID pour pré-remplir le select
        if (rec.getCanal() != null) {
            dto.setCanalDisplay(rec.getCanal().getLibCan());
            dto.setCanalId(rec.getCanal().getId());
        } else {
            dto.setCanalDisplay("-");
        }

        // ✅ Objet — libellé + ID pour pré-remplir le select
        if (rec.getObjet() != null) {
            dto.setObjetDisplay(rec.getObjet().getLibObj());
            dto.setObjetId(rec.getObjet().getId());
        } else {
            dto.setObjetDisplay("-");
        }

        // ✅ CLIENT : affichage numéro de compte et nature depuis rec_nature_client
        //    nat_clt contient un CODE (ENT, PART, PRO...) → on résout en libNat via findByCodNat
        if ("CLIENT".equalsIgnoreCase(rec.getTypeRec())) {
            dto.setClientDisplay(rec.getClient() != null ? rec.getClient().getNumCpt() : "-");
            if (rec.getClient() != null && rec.getClient().getNatClt() != null) {
                String libNat = natureReclamantRepository.findByCodNat(rec.getClient().getNatClt())
                        .map(NatureReclamant::getLibNat)
                        .orElse(rec.getClient().getNatClt()); // fallback : code brut si inconnu
                dto.setNatureDisplay(libNat);
            } else {
                dto.setNatureDisplay("-");
            }
            // Pas de natRecId pour les clients — la nature vient de l'entité Client
        } else {
            // NON_CLIENT : affichage CIN/matricule et nature réclamant
            dto.setClientDisplay(rec.getNumPidRec() != null ? rec.getNumPidRec() : "-");
            if (rec.getNatRec() != null) {
                dto.setNatureDisplay(rec.getNatRec().getLibNat());
                dto.setNatRecId(rec.getNatRec().getId());
            } else {
                dto.setNatureDisplay("-");
            }
        }

        // ✅ Email & Téléphone — affichés en lecture seule, jamais modifiables
        dto.setEmailRec(rec.getEmailRec());
        dto.setTelephone(rec.getGsmRec() != null ? String.valueOf(rec.getGsmRec()) : null);

        if (rec.getNotificationTypes() != null) {
            dto.setNotificationTypeIds(rec.getNotificationTypes().stream().map(NotificationType::getId).toList());
        } else {
            dto.setNotificationTypeIds(new ArrayList<>());
        }

        return dto;
    }

    // ==========================================================
    // ✅ GET BY ID (DTO)
    // ==========================================================

    public Optional<ReclamationResponse> getReclamationById(String refRec) {
        return reclamationRepository.findByRefRec(refRec)
                .map(this::buildToDto);
    }

    // ==========================================================
    // ✅ CREATE
    // ==========================================================

    public Reclamation createReclamation(ReclamationRequest dto) {

        Reclamation rec = new Reclamation();
        rec.setTypeRec(dto.getTypeRec());

        // ✅ RÉCUPÉRATION UTILISATEUR CONNECTÉ
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {

            Users currentUser = usersRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // ✅ IMPORTANT : remplir user_cre
            rec.setUserCre(Long.valueOf(currentUser.getId()));
        }

        // CLIENT TSB
        if ("CLIENT".equalsIgnoreCase(dto.getTypeRec())) {
            Client client = clientRepository.findById(dto.getCodeClt())
                    .orElseThrow(() -> new RuntimeException("Client introuvable"));
            rec.setClient(client);
            rec.setNumCptRec(dto.getNumCpt());
        }

        // NON CLIENT
        if ("NON_CLIENT".equalsIgnoreCase(dto.getTypeRec())) {

            if (dto.getNatRecId() != null) {
                NatureReclamant nat = natureReclamantRepository.findById(dto.getNatRecId())
                        .orElseThrow(() -> new RuntimeException("Nature introuvable"));
                rec.setNatRec(nat);
            }

            rec.setNomRec(dto.getNomRec());
            rec.setPrenRec(dto.getPrenRec());

            rec.setTypPidRec(dto.getCin() != null ? "CIN" : "MATRICULE");
            rec.setNumPidRec(dto.getCin() != null ? dto.getCin() : dto.getMatriculeFiscal());
        }

        rec.setDescRec(dto.getDescRec());
        rec.setEmailRec(dto.getEmailRec());

        if (dto.getTelephone() != null && !dto.getTelephone().isBlank()) {
            if (!isValidTunisianPhone(dto.getTelephone())) {
                throw new IllegalArgumentException(
                        "Numéro tunisien invalide : 8 chiffres commençant par 2, 5, 7 ou 9."
                );
            }
            rec.setGsmRec(Long.parseLong(dto.getTelephone()));
        }

        // Liaisons dynamiques
        Objet objet = objetRepository.findById(dto.getObjetId())
                .orElseThrow(() -> new RuntimeException("Objet introuvable"));
        rec.setObjet(objet);

        Canal canal = canalRepository.findById(dto.getCanalId())
                .orElseThrow(() -> new RuntimeException("Canal introuvable"));
        rec.setCanal(canal);

        // Statut par défaut dynamique
        Statut statut = statutRepository.findByCodSta("01")
                .orElseThrow(() -> new RuntimeException("Statut par défaut introuvable"));

        rec.setStatRec(statut);

        // ✅ NotificationTypes — multi-sélection : on charge tous les types actifs demandés
        if (dto.getNotificationTypeIds() != null && !dto.getNotificationTypeIds().isEmpty()) {
            List<NotificationType> types =
                notificationTypeRepository.findAllById(dto.getNotificationTypeIds());
            rec.setNotificationTypes(types);
        }

        rec.setRefRec(generateReference());
        rec.setDatRec(new Date());
        rec.setDatCre(new Date());

        // ✅ Sauvegarde AVANT notification
        Reclamation saved = reclamationRepository.save(rec);

        notifyCreation(saved);

        return saved;
    }

    // ==========================================================
    // ✅ UPDATE (Modification) — CORRIGÉ
    // Règles :
    //  - Statuts "02" (Traité) et "03" (Rejeté) bloquent la modification
    //  - Email et téléphone sont NON modifiables
    //  - Seuls : natRecId, canalId, objetId, descRec, compRec
    // ==========================================================

    public ReclamationResponse updateReclamation(String refRec, ReclamationUpdateRequest request) {

        Reclamation rec = reclamationRepository.findByRefRec(refRec)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable : " + refRec));

        // ✅ CONTRÔLE ACCÈS PAR SERVICE — ROLE_USER limité à son propre service
        Users currentUser = resolveCurrentUser();
        boolean isAdmin = isAdmin(currentUser);

        // ✅ 1️⃣ Vérifier accès service pour USER
        if (!isAdmin) {
            checkServiceAccess(rec, currentUser,
                    "Modification impossible : cette réclamation n'appartient pas à votre service.");
        }

        // ✅ 2️⃣ Règle métier statut
        String codSta = rec.getStatRec() != null ? rec.getStatRec().getCodSta() : null;

        /*
          ✅ ROLE_ADMIN → peut modifier toujours
          ✅ ROLE_USER → peut modifier uniquement si EN_COURS
        */
        if (!isAdmin && !"01".equals(codSta)) {
            throw new BusinessException(
                    "Modification impossible : la réclamation n'est plus en cours."
            );
        }

        // ✅ Champs modifiables autorisés
        if (request.getDescRec() != null && !request.getDescRec().isBlank()) {
            rec.setDescRec(request.getDescRec());
        }

        // Commentaire interne — toujours écrasable (peut être vidé volontairement)
        rec.setCompRec(request.getCompRec());

        if (request.getCanalId() != null) {
            Canal canal = canalRepository.findById(request.getCanalId())
                    .orElseThrow(() -> new RuntimeException("Canal introuvable : id=" + request.getCanalId()));
            rec.setCanal(canal);
        }

        if (request.getObjetId() != null) {
            Objet objet = objetRepository.findById(request.getObjetId())
                    .orElseThrow(() -> new RuntimeException("Objet introuvable : id=" + request.getObjetId()));
            rec.setObjet(objet);
        }

        // Nature modifiable uniquement pour les NON_CLIENT
        if (request.getNatRecId() != null && !"CLIENT".equalsIgnoreCase(rec.getTypeRec())) {
            NatureReclamant nat = natureReclamantRepository.findById(request.getNatRecId())
                    .orElseThrow(() -> new RuntimeException("Nature introuvable : id=" + request.getNatRecId()));
            rec.setNatRec(nat);
        }

        // ✅ NotificationTypes — multi-sélection : mise à jour si EN_COURS
        if ("01".equals(codSta)) {

            if (request.getNotificationTypeIds() != null
                && !request.getNotificationTypeIds().isEmpty()) {

                List<NotificationType> types =
                    notificationTypeRepository.findAllById(request.getNotificationTypeIds());

                rec.setNotificationTypes(types);

            } else {
                rec.setNotificationTypes(new ArrayList<>());
            }
        }

        // ❌ telephone et emailRec : NON modifiables — aucune ligne ici
        rec.setDatMod(new Date());

        reclamationRepository.save(rec);

        return buildToDto(rec);
    }

    // ==========================================================
    // ✅ UPDATE STATUS (Traitement) — CORRIGÉ
    // Règles :
    //  - commentaire obligatoire si codSta == "03"
    //  - Sauvegarde la décision EN BASE avant d'envoyer les notifications
    //  - Met à jour datRep
    //  - Met à jour notifiedEmail et notifiedSms
    //  - Ne bloque JAMAIS la transaction si email/SMS échoue
    // ==========================================================

    public ReclamationResponse updateStatus(String refRec, String codSta, String commentaire) {

        Reclamation rec = reclamationRepository.findByRefRec(refRec)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable : " + refRec));

        // ✅ CONTRÔLE ACCÈS PAR SERVICE — ROLE_USER limité à son propre service
        Users currentUser = resolveCurrentUser();
        if (!isAdmin(currentUser)) {
            checkServiceAccess(rec, currentUser,
                    "Traitement interdit : cette réclamation n'appartient pas à votre service.");
        }

        Statut statut = statutRepository.findByCodSta(codSta)
                .orElseThrow(() -> new RuntimeException("Statut introuvable : " + codSta));

        // ✅ VALIDATION MÉTIER : commentaire obligatoire si rejet "03"
        if ("03".equals(codSta) && (commentaire == null || commentaire.isBlank())) {
            throw new IllegalArgumentException(
                    "Le commentaire est obligatoire pour un rejet (statut 03)."
            );
        }

        // ✅ Mise à jour des champs métier
        rec.setStatRec(statut);
        rec.setDatRep(new Date());    // Date de réponse
        rec.setDatMod(new Date());    // Date de modification

        // Commentaire agent (toujours persisté si fourni)
        if (commentaire != null && !commentaire.isBlank()) {
            rec.setCompRec(commentaire);
        }

        // ✅ ÉTAPE 1 — Sauvegarde de la décision EN BASE avant tout envoi
        reclamationRepository.save(rec);

        // ✅ ÉTAPE 2 — Notifications (après sauvegarde — ne bloquent jamais la transaction)
        notifyDecision(rec);

        // ✅ ÉTAPE 3 — Retour DTO complet
        return buildToDto(rec);
    }

    // ==========================================================
    // ✅ NOTIFICATIONS
    // ==========================================================

    private void notifyCreation(Reclamation rec) {

        List<NotificationType> types = rec.getNotificationTypes();

        if (types == null || types.isEmpty()) return;

        for (NotificationType type : types) {

            NotificationType fresh =
                notificationTypeRepository.findById(type.getId()).orElse(null);

            if (fresh == null || !Boolean.TRUE.equals(fresh.getActive())) continue;

            notificationDispatcher.dispatch(fresh, rec);
        }

        // ✅ Persistance des flags de notification en base
        reclamationRepository.save(rec);
    }

    private void notifyDecision(Reclamation rec) {

        List<NotificationType> types = rec.getNotificationTypes();

        if (types == null || types.isEmpty()) return;

        for (NotificationType type : types) {

            NotificationType fresh =
                notificationTypeRepository.findById(type.getId()).orElse(null);

            if (fresh == null || !Boolean.TRUE.equals(fresh.getActive())) continue;

            notificationDispatcher.dispatch(fresh, rec);
        }

        // ✅ Persistance des flags notifiedEmail / notifiedSms
        reclamationRepository.save(rec);
    }

    // ==========================================================
    // ✅ SÉCURITÉ MÉTIER — helpers internes
    // ==========================================================

    /**
     * Résout l'utilisateur courant depuis le SecurityContext.
     * Lève une RuntimeException si l'utilisateur n'est pas authentifié.
     */
    private Users resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            return usersRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }

    /**
     * Retourne true si l'utilisateur possède le rôle ADMIN.
     */
    private boolean isAdmin(Users user) {
        return user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodrole()) || "ADMIN".equals(r.getCodrole()));
    }

    /**
     * Vérifie que la réclamation appartient au service de l'utilisateur.
     * Lève une {@link BusinessException} avec le message fourni si l'accès est refusé.
     */
    private void checkServiceAccess(Reclamation rec, Users user, String errorMessage) {
        if (user.getService() == null
                || rec.getObjet() == null
                || rec.getObjet().getService() == null
                || !user.getService().getCodSer().equals(rec.getObjet().getService().getCodSer())) {
            throw new BusinessException(errorMessage);
        }
    }

    // ==========================================================
    // ✅ UTILS
    // ==========================================================

    /**
     * Valide un numéro de téléphone tunisien.
     * Règle : 8 chiffres exactement, commençant par 2, 5, 7 ou 9.
     * Regex : ^[2579]\d{7}$
     */
    private boolean isValidTunisianPhone(String phone) {
        return phone != null && phone.matches("^[2579]\\d{7}$");
    }

    private String generateReference() {
        int year = LocalDate.now().getYear();
        long count = reclamationRepository.countByYear(year) + 1;
        return "REC-" + year + "-" + String.format("%06d", count);
    }

    // ==========================================================
    // ✅ XML GENERATION FLAG
    // ==========================================================

    public void markAsXmlGenerated(String refRec, String userGenXml) {

        Reclamation rec = reclamationRepository.findByRefRec(refRec)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        rec.setXmlGenerated(true);
        rec.setDatGenXml(new Date());
        rec.setUserGenXml(userGenXml);

        reclamationRepository.save(rec);
    }

    // ==========================================================
    // ✅ GET ALL (DTO VERSION — paginé)
    // ==========================================================

    public Page<ReclamationResponse> getReclamationsPage(int page, int size, String search, String codSta) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "datRec")
        );

        org.springframework.data.jpa.domain.Specification<Reclamation> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            // 1️⃣ PÉRIMÈTRE DE SÉCURITÉ (ADMIN vs USER)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                Users user = usersRepository.findByEmail(userDetails.getUsername()).orElse(null);
                
                if (user != null) {
                    boolean isAdmin = user.getRoles().stream()
                            .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodrole()) || "ADMIN".equals(r.getCodrole()));
                    
                    if (!isAdmin) {
                        if (user.getService() != null) {
                            predicates.add(cb.equal(root.join("objet").join("service").get("codSer"), user.getService().getCodSer()));
                        } else {
                            // User sans service, et non admin -> voit rien
                            predicates.add(cb.disjunction());
                        }
                    }
                }
            }

            // 2️⃣ FILTRE PAR STATUT
            if (codSta != null && !codSta.trim().isEmpty() && !"ALL".equalsIgnoreCase(codSta)) {
                predicates.add(cb.equal(root.join("statRec").get("codSta"), codSta));
            }
            
            // 3️⃣ RECHERCHE DYNAMIQUE
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                jakarta.persistence.criteria.Join<Reclamation, Client> clientJoin = root.join("client", jakarta.persistence.criteria.JoinType.LEFT);
                
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("refRec")), searchPattern),
                        cb.like(cb.lower(clientJoin.get("numCpt")), searchPattern),
                        cb.like(cb.lower(clientJoin.get("nomClt")), searchPattern),
                        cb.like(cb.lower(clientJoin.get("prenClt")), searchPattern)
                ));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return reclamationRepository.findAll(spec, pageable)
                .map(this::buildToDto);
    }
}