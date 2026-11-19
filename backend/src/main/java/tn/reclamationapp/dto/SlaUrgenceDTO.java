package tn.reclamationapp.dto;

import java.util.Date;

/**
 * DTO représentant une réclamation urgente dans le contexte du module SLA PREMIUM.
 * <p>
 * Retourné dans la liste {@code urgences} du {@link SlaDashboardDTO}.
 * Contient les informations nécessaires au traitement prioritaire :
 * référence, dates, classification SLA et score de risque IA.
 * </p>
 */
public class SlaUrgenceDTO {

    /** Référence unique de la réclamation (ex : REC-2025-000042). */
    private String refRec;

    /** Date de dépôt de la réclamation. */
    private Date datRec;

    /** Date limite SLA calculée (datRec + SLA_DAYS). */
    private Date datLimite;

    /**
     * Nombre de jours restants avant dépassement SLA.
     * Valeur négative si la date limite est dépassée.
     */
    private long joursRestants;

    /**
     * Classification colorielle SLA :
     * <ul>
     *   <li>GREEN  — plus de 5 jours restants</li>
     *   <li>ORANGE — 1 à 5 jours restants</li>
     *   <li>RED    — délai dépassé</li>
     * </ul>
     */
    private String slaStatus;

    /**
     * Score de risque IA heuristique [0–100].
     * Plus le score est élevé, plus le risque de non-conformité est critique.
     */
    private double riskScore;

    /** Libellé du service bancaire responsable de la réclamation. */
    private String serviceLib;

    // ===== Constructeur =====

    public SlaUrgenceDTO() {}

    // ===== Getters & Setters =====

    public String getRefRec() { return refRec; }
    public void setRefRec(String refRec) { this.refRec = refRec; }

    public Date getDatRec() { return datRec; }
    public void setDatRec(Date datRec) { this.datRec = datRec; }

    public Date getDatLimite() { return datLimite; }
    public void setDatLimite(Date datLimite) { this.datLimite = datLimite; }

    public long getJoursRestants() { return joursRestants; }
    public void setJoursRestants(long joursRestants) { this.joursRestants = joursRestants; }

    public String getSlaStatus() { return slaStatus; }
    public void setSlaStatus(String slaStatus) { this.slaStatus = slaStatus; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public String getServiceLib() { return serviceLib; }
    public void setServiceLib(String serviceLib) { this.serviceLib = serviceLib; }
}
