package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tn.reclamationapp.dto.SlaDashboardDTO;
import tn.reclamationapp.dto.SlaUrgenceDTO;
import tn.reclamationapp.model.Reclamation;
import tn.reclamationapp.model.Statut;
import tn.reclamationapp.model.Users;
import tn.reclamationapp.repository.ParamGenRepository;
import tn.reclamationapp.repository.ReclamationRepository;
import tn.reclamationapp.repository.UsersRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service d'analyse SLA PREMIUM avec moteur IA heuristique.
 *
 * <h3>Responsabilités :</h3>
 * <ol>
 *   <li>Lecture du paramètre SLA_DAYS depuis {@code rec_param_gen} via {@link ParamGenRepository}</li>
 *   <li>Classification de chaque réclamation : GREEN / ORANGE / RED</li>
 *   <li>Calcul du score de risque IA [0–100] par réclamation</li>
 *   <li>Agrégation des métriques globales dans {@link SlaDashboardDTO}</li>
 *   <li>Respect du filtrage ADMIN/USER identique à {@code ReclamationService.getReclamationsPage}</li>
 * </ol>
 *
 * <h3>Formule IA heuristique :</h3>
 * <pre>
 *   Si dépassé → riskScore = 100.0
 *   Sinon :
 *     baseRisk = (1 - joursRestants / SLA_DAYS) × 50
 *     + 20 si délai moyen service > 12 jours
 *     + 15 si nombre réclamations en cours service > 30
 *     Clamp [0, 100]
 * </pre>
 *
 * <h3>Contraintes respectées :</h3>
 * <ul>
 *   <li>Ne modifie pas ReclamationService, ReclamationAnalyticsService ni AnalyticsDTO</li>
 *   <li>Aucune valeur SLA codée en dur — tout depuis ParamGen</li>
 *   <li>Réutilise le même pattern de sécurité que ReclamationService</li>
 * </ul>
 */
@Service
public class SlaAnalyticsService {

    // ===== Constantes SLA =====

    /** Libellé du paramètre SLA dans rec_param_gen. */
    private static final String SLA_PARAM_KEY = "SLA_DAYS";

    /** Valeur de repli si SLA_DAYS est absent de la table rec_param_gen. */
    private static final int SLA_DAYS_DEFAULT = 15;

    /** Seuil ORANGE : nombre de jours restants ≤ cette valeur → zone orange. */
    private static final int SEUIL_ORANGE = 5;

    // ===== Constantes scoring IA =====

    /** Poids de base du score IA (proportionnel au temps écoulé sur la fenêtre SLA). */
    private static final double WEIGHT_BASE = 50.0;

    /** Malus si le délai moyen de traitement du service dépasse ce seuil (en jours). */
    private static final double SEUIL_DELAI_MOYEN = 12.0;

    /** Valeur du malus délai moyen. */
    private static final double MALUS_DELAI_MOYEN = 20.0;

    /** Seuil de charge du service (nb réclamations en cours) déclenchant le malus. */
    private static final long SEUIL_CHARGE_SERVICE = 30L;

    /** Valeur du malus charge service. */
    private static final double MALUS_CHARGE_SERVICE = 15.0;

    // ===== Injections =====

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private ParamGenRepository paramGenRepository;

    @Autowired
    private UsersRepository usersRepository;

    // ==========================================================================
    // MÉTHODE PRINCIPALE — Point d'entrée du dashboard SLA
    // ==========================================================================

    /**
     * Construit le {@link SlaDashboardDTO} complet pour l'utilisateur courant.
     * <p>
     * Le périmètre des données dépend du rôle :
     * <ul>
     *   <li>ADMIN → toutes les réclamations</li>
     *   <li>USER  → réclamations du service de l'utilisateur uniquement</li>
     * </ul>
     *
     * @return DTO complet prêt à être sérialisé en JSON
     */
    public SlaDashboardDTO buildDashboard() {

        // 1. Lecture dynamique du paramètre SLA_DAYS depuis ParamGen
        int slaDays = resolveSladays();

        // 2. Lecture des données contextuelles IA (délais moyens + charges par service)
        Map<String, Double> delaiMoyenParService  = loadDelaiMoyenParService();
        Map<String, Long>   enCoursByService      = loadEnCoursByService();

        // 3. Récupération des réclamations selon le rôle de l'utilisateur courant
        List<Reclamation> reclamations = resolveReclamationsForCurrentUser();

        // 4. Calcul SLA + scoring IA + classification de chaque réclamation
        Date today = new Date();
        SlaDashboardDTO dto = new SlaDashboardDTO();
        dto.setSlaDays(slaDays);

        long enCoursCount     = 0;
        long clotureeCount    = 0;
        long respectSlaCount  = 0;
        long exceededCount    = 0;
        double totalRiskScore = 0.0;

        // Map intermédiaire : service → [sumRespect, totalEnCours] pour performanceParService
        Map<String, long[]> serviceStats = new HashMap<>();

        List<SlaUrgenceDTO> urgences = new ArrayList<>();

        for (Reclamation rec : reclamations) {

            // 4.1 — Classification en cours vs clôturé (logique identique à ReclamationAnalyticsService)
            boolean isCloturee = isCloturee(rec.getStatRec());

            if (isCloturee) {
                clotureeCount++;
                continue; // Les réclamations clôturées ne participent pas au calcul SLA en cours
            }
            enCoursCount++;

            // 4.2 — Dates SLA
            if (rec.getDatRec() == null) continue;

            Date datLimite  = computeDatLimite(rec.getDatRec(), slaDays);
            long joursRestants = computeJoursRestants(today, datLimite);

            // 4.3 — Classification SLA
            String slaStatus = classifySlaStatus(joursRestants);
            boolean exceeded  = "RED".equals(slaStatus);

            // 4.4 — Données de contexte IA pour ce service
            String serviceLib = resolveServiceLib(rec);
            double delaiMoyen = delaiMoyenParService.getOrDefault(serviceLib, 0.0);
            long   chargeServ = enCoursByService.getOrDefault(serviceLib, 0L);

            // 4.5 — Calcul du score de risque IA
            double riskScore = computeRiskScore(joursRestants, slaDays, delaiMoyen, chargeServ, exceeded);

            // 4.6 — Mise à jour des compteurs SLA
            if (exceeded) {
                exceededCount++;
            } else {
                respectSlaCount++;
            }
            totalRiskScore += riskScore;

            // 4.7 — Stats par service pour performanceParService
            serviceStats.computeIfAbsent(serviceLib, k -> new long[]{0, 0});
            serviceStats.get(serviceLib)[1]++; // total en cours pour ce service
            if (!exceeded) {
                serviceStats.get(serviceLib)[0]++; // respect SLA pour ce service
            }

            // 4.8 — Alimentation de la liste des urgences (ORANGE + RED uniquement)
            if ("ORANGE".equals(slaStatus) || "RED".equals(slaStatus)) {
                urgences.add(buildUrgenceDTO(rec, datLimite, joursRestants, slaStatus, riskScore, serviceLib));
            }
        }

        // 5. Agrégation des totaux dans le DTO
        long totalCount = enCoursCount + clotureeCount;
        dto.setTotal(totalCount);
        dto.setEnCours(enCoursCount);
        dto.setCloturees(clotureeCount);
        dto.setRespectSlaCount(respectSlaCount);
        dto.setExceededCount(exceededCount);

        // 6. Pourcentage de respect SLA (éviter division par zéro)
        double respectPct = enCoursCount > 0
                ? Math.round((double) respectSlaCount / enCoursCount * 10000.0) / 100.0
                : 100.0;
        dto.setRespectPercentage(respectPct);

        // 7. Score de risque moyen global (arrondi à 2 décimales)
        double risqueMoyen = enCoursCount > 0
                ? Math.round(totalRiskScore / enCoursCount * 100.0) / 100.0
                : 0.0;
        dto.setRisqueMoyenGlobal(risqueMoyen);

        // 8. Performance par service (% respect SLA)
        Map<String, Double> performanceParService = new LinkedHashMap<>();
        for (Map.Entry<String, long[]> entry : serviceStats.entrySet()) {
            long[] stats      = entry.getValue();
            long  totalServ   = stats[1];
            long  respectServ = stats[0];
            double pct = totalServ > 0
                    ? Math.round((double) respectServ / totalServ * 10000.0) / 100.0
                    : 100.0;
            performanceParService.put(entry.getKey(), pct);
        }
        dto.setPerformanceParService(performanceParService);

        // 9. Urgences triées par score de risque décroissant (plus critique en premier)
        urgences.sort(Comparator.comparingDouble(SlaUrgenceDTO::getRiskScore).reversed());
        dto.setUrgences(urgences);

        return dto;
    }

    // ==========================================================================
    // MÉTHODES PRIVÉES — Logique SLA et IA
    // ==========================================================================

    /**
     * Lit la valeur SLA_DAYS depuis la table rec_param_gen.
     * En cas d'absence ou de valeur non numérique, retourne {@code SLA_DAYS_DEFAULT} (15 jours).
     */
    private int resolveSladays() {
        return paramGenRepository.findByLibParam(SLA_PARAM_KEY)
                .map(pg -> {
                    try {
                        return Integer.parseInt(pg.getValParam().trim());
                    } catch (NumberFormatException e) {
                        // Valeur configurée mais non parseable → repli sur défaut
                        return SLA_DAYS_DEFAULT;
                    }
                })
                .orElse(SLA_DAYS_DEFAULT);
    }

    /**
     * Calcule la date limite SLA = datRec + slaDays (en millisecondes).
     */
    private Date computeDatLimite(Date datRec, int slaDays) {
        long millis = datRec.getTime() + TimeUnit.DAYS.toMillis(slaDays);
        return new Date(millis);
    }

    /**
     * Calcule le nombre de jours restants entre aujourd'hui et la date limite.
     * Retourne une valeur négative si la date limite est dépassée.
     */
    private long computeJoursRestants(Date today, Date datLimite) {
        long diffMillis = datLimite.getTime() - today.getTime();
        // Conversion en jours (tronquée — pas d'arrondi pour rester prudent)
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    /**
     * Classifie le statut SLA selon les règles métier :
     * <ul>
     *   <li>RED    — joursRestants ≤ 0</li>
     *   <li>ORANGE — joursRestants entre 1 et SEUIL_ORANGE (inclus)</li>
     *   <li>GREEN  — joursRestants > SEUIL_ORANGE</li>
     * </ul>
     */
    private String classifySlaStatus(long joursRestants) {
        if (joursRestants <= 0) return "RED";
        if (joursRestants <= SEUIL_ORANGE) return "ORANGE";
        return "GREEN";
    }

    /**
     * Calcule le score de risque IA heuristique [0–100].
     *
     * <h4>Formule complète :</h4>
     * <pre>
     *   Si dépassé → 100.0
     *   Sinon :
     *     base    = (1 - joursRestants / slaDays) × 50     [0–50]
     *     malus1  = +20 si délai moyen service > 12 jours
     *     malus2  = +15 si charge service > 30 réclamations en cours
     *     score   = clamp(base + malus1 + malus2, 0.0, 100.0)
     * </pre>
     *
     * @param joursRestants   jours restants avant dépassement SLA
     * @param slaDays         fenêtre SLA totale en jours
     * @param delaiMoyenServ  délai moyen de traitement du service (jours)
     * @param chargeServ      nombre de réclamations en cours dans le service
     * @param exceeded        true si la date limite est dépassée
     * @return score de risque arrondi à 2 décimales
     */
    private double computeRiskScore(long joursRestants, int slaDays,
                                    double delaiMoyenServ, long chargeServ,
                                    boolean exceeded) {
        if (exceeded) return 100.0;

        // Base proportionnelle : plus on approche de la limite, plus le risque monte
        double baseRisk = (1.0 - (double) joursRestants / slaDays) * WEIGHT_BASE;

        // Malus contextuels issus des données historiques du service
        double malus = 0.0;
        if (delaiMoyenServ > SEUIL_DELAI_MOYEN) malus += MALUS_DELAI_MOYEN;
        if (chargeServ      > SEUIL_CHARGE_SERVICE) malus += MALUS_CHARGE_SERVICE;

        double score = baseRisk + malus;

        // Clamp [0, 100] — on ne peut jamais dépasser 100 ni descendre sous 0
        return Math.min(100.0, Math.max(0.0, Math.round(score * 100.0) / 100.0));
    }

    /**
     * Détermine si une réclamation est clôturée d'après son statut.
     * Utilise la même logique dynamique que {@code ReclamationAnalyticsService} :
     * les statuts contenant "clôturé", "cloture", "fermé", "favorable", "rejet",
     * "refus" ou "accept" sont considérés comme clôturés.
     *
     * @param statut le statut de la réclamation (peut être null)
     * @return true si clôturée, false si encore en cours
     */
    private boolean isCloturee(Statut statut) {
        if (statut == null) return false;
        String lib = statut.getLibSta() != null ? statut.getLibSta().toLowerCase() : "";
        return lib.contains("clôturé") || lib.contains("cloture")
            || lib.contains("fermé")   || lib.contains("favorable")
            || lib.contains("rejet")   || lib.contains("refus")
            || lib.contains("accept");
    }

    /**
     * Résout le libellé du service associé à une réclamation.
     * Retourne "SERVICE INCONNU" si la réclamation n'a pas de service rattaché.
     */
    private String resolveServiceLib(Reclamation rec) {
        if (rec.getObjet() != null
                && rec.getObjet().getService() != null
                && rec.getObjet().getService().getLibSer() != null) {
            return rec.getObjet().getService().getLibSer();
        }
        return "SERVICE INCONNU";
    }

    /**
     * Construit un {@link SlaUrgenceDTO} à partir d'une réclamation et de ses métriques SLA.
     */
    private SlaUrgenceDTO buildUrgenceDTO(Reclamation rec, Date datLimite,
                                          long joursRestants, String slaStatus,
                                          double riskScore, String serviceLib) {
        SlaUrgenceDTO urgence = new SlaUrgenceDTO();
        urgence.setRefRec(rec.getRefRec());
        urgence.setDatRec(rec.getDatRec());
        urgence.setDatLimite(datLimite);
        urgence.setJoursRestants(joursRestants);
        urgence.setSlaStatus(slaStatus);
        urgence.setRiskScore(riskScore);
        urgence.setServiceLib(serviceLib);
        return urgence;
    }

    // ==========================================================================
    // MÉTHODES PRIVÉES — Sécurité et filtrage par rôle
    // ==========================================================================

    /**
     * Retourne la liste des réclamations selon le rôle de l'utilisateur courant.
     * <ul>
     *   <li>ADMIN → toutes les réclamations (findAll)</li>
     *   <li>USER  → réclamations du service de l'utilisateur (findByObjetServiceCodSer)</li>
     *   <li>USER sans service → liste vide</li>
     * </ul>
     * <p>
     * Pattern identique à {@code ReclamationService.getReclamationsPage} (lignes 490–511).
     * </p>
     */
    private List<Reclamation> resolveReclamationsForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Users user = usersRepository.findByEmail(userDetails.getUsername()).orElse(null);

            if (user != null) {
                boolean isAdmin = user.getRoles().stream()
                        .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodrole())
                                    || "ADMIN".equals(r.getCodrole()));

                if (!isAdmin) {
                    // USER : filtrage strict par service
                    if (user.getService() != null) {
                        return reclamationRepository.findByObjetServiceCodSer(
                                user.getService().getCodSer());
                    }
                    return Collections.emptyList();
                }
            }
        }

        // ADMIN ou cas non identifié → toutes les réclamations
        return reclamationRepository.findAll();
    }

    // ==========================================================================
    // MÉTHODES PRIVÉES — Chargement des données IA contextuelles
    // ==========================================================================

    /**
     * Charge les délais moyens de traitement par service depuis la base.
     * Utilisé pour le malus IA (+20 si délai > 12 jours).
     *
     * @return Map [libSer → délaiMoyenJours]
     */
    private Map<String, Double> loadDelaiMoyenParService() {
        Map<String, Double> result = new HashMap<>();
        for (Object[] row : reclamationRepository.findAvgDelayByService()) {
            if (row[0] != null && row[1] != null) {
                result.put(row[0].toString(), ((Number) row[1]).doubleValue());
            }
        }
        return result;
    }

    /**
     * Charge le nombre de réclamations en cours par service depuis la base.
     * Utilisé pour le malus IA (+15 si charge > 30).
     *
     * @return Map [libSer → countEnCours]
     */
    private Map<String, Long> loadEnCoursByService() {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : reclamationRepository.countEnCoursByService()) {
            if (row[0] != null && row[1] != null) {
                result.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        return result;
    }
}
