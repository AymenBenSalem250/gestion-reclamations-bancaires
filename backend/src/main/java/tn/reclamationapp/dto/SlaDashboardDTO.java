package tn.reclamationapp.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO principal du module SLA PREMIUM.
 * <p>
 * Retourné par {@code GET /api/reporting/sla-dashboard} et construit
 * dynamiquement par {@link tn.reclamationapp.service.SlaAnalyticsService}.
 * </p>
 *
 * <h3>Règles de filtrage par rôle :</h3>
 * <ul>
 *   <li>ADMIN → données globales toutes réclamations confondues</li>
 *   <li>USER  → données filtrées sur le service de l'utilisateur uniquement</li>
 * </ul>
 *
 * <h3>Indicateurs SLA :</h3>
 * <ul>
 *   <li>GREEN  — plus de 5 jours restants avant limite</li>
 *   <li>ORANGE — 1 à 5 jours restants (zone de vigilance)</li>
 *   <li>RED    — délai SLA dépassé (non-conformité)</li>
 * </ul>
 */
public class SlaDashboardDTO {

    // ===== Métriques de volume =====

    /** Nombre total de réclamations dans le périmètre (global ou service). */
    private long total;

    /** Nombre de réclamations actuellement en cours (non clôturées). */
    private long enCours;

    /** Nombre de réclamations clôturées. */
    private long cloturees;

    // ===== Métriques SLA =====

    /** Nombre de réclamations en cours ayant respecté le délai SLA. */
    private long respectSlaCount;

    /** Nombre de réclamations ayant dépassé le délai SLA. */
    private long exceededCount;

    /**
     * Pourcentage de respect du SLA parmi les réclamations en cours.
     * Formule : (respectSlaCount / enCours) * 100. Retourne 100.0 si aucune réclamation.
     */
    private double respectPercentage;

    /**
     * Score de risque IA moyen global [0–100].
     * Moyenne arithmétique des scores individuels de toutes les réclamations en cours.
     */
    private double risqueMoyenGlobal;

    /**
     * Nombre de jours SLA configuré dynamiquement depuis ParamGen (lib_param = "SLA_DAYS").
     * Exposé dans la réponse pour permettre au front de contextualiser les indicateurs.
     */
    private int slaDays;

    // ===== Performance par service =====

    /**
     * Performance SLA par service bancaire.
     * Clé = libellé du service (lib_ser), Valeur = pourcentage de respect SLA [0–100].
     */
    private Map<String, Double> performanceParService;

    // ===== Liste des urgences =====

    /**
     * Réclamations en zones ORANGE et RED, triées par score de risque décroissant.
     * Permet d'afficher immédiatement les cas critiques dans le dashboard.
     */
    private List<SlaUrgenceDTO> urgences;

    // ===== Constructeur =====

    public SlaDashboardDTO() {}

    // ===== Getters & Setters =====

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getEnCours() { return enCours; }
    public void setEnCours(long enCours) { this.enCours = enCours; }

    public long getCloturees() { return cloturees; }
    public void setCloturees(long cloturees) { this.cloturees = cloturees; }

    public long getRespectSlaCount() { return respectSlaCount; }
    public void setRespectSlaCount(long respectSlaCount) { this.respectSlaCount = respectSlaCount; }

    public long getExceededCount() { return exceededCount; }
    public void setExceededCount(long exceededCount) { this.exceededCount = exceededCount; }

    public double getRespectPercentage() { return respectPercentage; }
    public void setRespectPercentage(double respectPercentage) { this.respectPercentage = respectPercentage; }

    public double getRisqueMoyenGlobal() { return risqueMoyenGlobal; }
    public void setRisqueMoyenGlobal(double risqueMoyenGlobal) { this.risqueMoyenGlobal = risqueMoyenGlobal; }

    public int getSlaDays() { return slaDays; }
    public void setSlaDays(int slaDays) { this.slaDays = slaDays; }

    public Map<String, Double> getPerformanceParService() { return performanceParService; }
    public void setPerformanceParService(Map<String, Double> performanceParService) {
        this.performanceParService = performanceParService;
    }

    public List<SlaUrgenceDTO> getUrgences() { return urgences; }
    public void setUrgences(List<SlaUrgenceDTO> urgences) { this.urgences = urgences; }
}
