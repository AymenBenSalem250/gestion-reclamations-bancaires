package tn.reclamationapp.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "rec_reclamation")
public class Reclamation {

    @Id
    @Column(name = "ref_rec")
    private String refRec;

    @Column(name = "typ_rec")
    private String typeRec;

    // ✅ LIAISON CLIENT
    @ManyToOne
    @JoinColumn(name = "code_clt", referencedColumnName = "code_clt")
    private Client client;

    @ManyToOne(optional = true)
    @JoinColumn(name = "nat_rec", referencedColumnName = "id")
    private NatureReclamant natRec;

    @Column(name = "nom_rec")
    private String nomRec;

    @Column(name = "pren_rec")
    private String prenRec;

    @Column(name = "raisoc_rec")
    private String raisocRec;

    @Column(name = "fonct_rec")
    private String fonctRec;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "datnais_rec")
    private Date datNaisRec;

    @Column(name = "genre_rec")
    private String genreRec;

    @Column(name = "typPid_rec")
    private String typPidRec;

    @Column(name = "numpid_rec")
    private String numPidRec;

    @Column(name = "numcpt_rec")
    private String numCptRec;

    @Column(name = "lib_cpt_rec")
    private String libCptRec;

    @Column(name = "email_rec")
    private String emailRec;

    @Column(name = "gsm_rec")
    private Long gsmRec;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dat_rec")
    private Date datRec;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_objRec", referencedColumnName = "id")
    private Objet objet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_canal", referencedColumnName = "id")
    private Canal canal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sta_rec", referencedColumnName = "cod_sta")
    private Statut statRec;

    @ManyToMany
    @JoinTable(
        name = "rec_reclamation_notification",
        joinColumns = @JoinColumn(name = "ref_rec"),
        inverseJoinColumns = @JoinColumn(name = "notification_type_id")
    )
    private List<NotificationType> notificationTypes = new ArrayList<>();

    @Column(name = "desc_rec")
    private String descRec;

    @Column(name = "notified_email")
    private boolean notifiedEmail;

    @Column(name = "notified_sms")
    private boolean notifiedSms;

    @Column(name = "user_cre")
    private Long userCre;

    @Column(name = "user_mod")
    private Long userMod;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dat_cre")
    private Date datCre;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dat_mod")
    private Date datMod;

    @Column(name = "obs_rec")
    private String obsRec;

    @Column(name = "comp_rec")
    private String compRec;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dat_rep")
    private Date datRep;

    @Column(name = "xml_gen")
    private boolean xmlGenerated = false;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dat_gen_xml")
    private Date datGenXml;

    @Column(name = "user_gen_xml")
    private String userGenXml;

    // =========================================================
    // ✅ MODULE SLA PREMIUM — Champs additionnels (non intrusifs)
    // Ces colonnes doivent exister dans rec_reclamation :
    //   ALTER TABLE rec_reclamation
    //     ADD COLUMN dat_limite     DATE         NULL,
    //     ADD COLUMN sla_exceeded   TINYINT(1)   DEFAULT 0,
    //     ADD COLUMN sla_status     VARCHAR(10)  NULL,
    //     ADD COLUMN sla_risk_score DOUBLE       NULL;
    // =========================================================

    /** Date limite SLA calculée dynamiquement (datRec + SLA_DAYS de ParamGen). */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dat_limite")
    private Date datLimite;

    /** Indicateur booléen : true si le délai SLA a été dépassé. */
    @Column(name = "sla_exceeded")
    private Boolean slaExceeded = false;

    /**
     * Classification colorielle du risque SLA :
     * <ul>
     *   <li>GREEN  — plus de 5 jours restants</li>
     *   <li>ORANGE — 1 à 5 jours restants</li>
     *   <li>RED    — délai dépassé</li>
     * </ul>
     */
    @Column(name = "sla_status")
    private String slaStatus;

    /**
     * Score de risque IA heuristique [0–100].
     * Calculé par SlaAnalyticsService selon la formule :
     * baseRisk = (1 - joursRestants / SLA_DAYS) * 50
     * + malus délai moyen service + malus charge service.
     */
    @Column(name = "sla_risk_score")
    private Double slaRiskScore;

    public Reclamation() {}

    // ✅ GETTERS & SETTERS

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getRefRec() { return refRec; }
    public void setRefRec(String refRec) { this.refRec = refRec; }

    public String getTypeRec() { return typeRec; }
    public void setTypeRec(String typeRec) { this.typeRec = typeRec; }

    public NatureReclamant getNatRec() { return natRec; }
    public void setNatRec(NatureReclamant natRec) { this.natRec = natRec; }

    public String getNomRec() { return nomRec; }
    public void setNomRec(String nomRec) { this.nomRec = nomRec; }

    public String getPrenRec() { return prenRec; }
    public void setPrenRec(String prenRec) { this.prenRec = prenRec; }

    public String getRaisocRec() { return raisocRec; }
    public void setRaisocRec(String raisocRec) { this.raisocRec = raisocRec; }

    public String getFonctRec() { return fonctRec; }
    public void setFonctRec(String fonctRec) { this.fonctRec = fonctRec; }

    public Date getDatNaisRec() { return datNaisRec; }
    public void setDatNaisRec(Date datNaisRec) { this.datNaisRec = datNaisRec; }

    public String getGenreRec() { return genreRec; }
    public void setGenreRec(String genreRec) { this.genreRec = genreRec; }

    public String getTypPidRec() { return typPidRec; }
    public void setTypPidRec(String typPidRec) { this.typPidRec = typPidRec; }

    public String getNumPidRec() { return numPidRec; }
    public void setNumPidRec(String numPidRec) { this.numPidRec = numPidRec; }

    public String getNumCptRec() { return numCptRec; }
    public void setNumCptRec(String numCptRec) { this.numCptRec = numCptRec; }

    public String getLibCptRec() { return libCptRec; }
    public void setLibCptRec(String libCptRec) { this.libCptRec = libCptRec; }

    public String getEmailRec() { return emailRec; }
    public void setEmailRec(String emailRec) { this.emailRec = emailRec; }

    public Long getGsmRec() { return gsmRec; }
    public void setGsmRec(Long gsmRec) { this.gsmRec = gsmRec; }

    public Date getDatRec() { return datRec; }
    public void setDatRec(Date datRec) { this.datRec = datRec; }

    public Objet getObjet() { return objet; }
    public void setObjet(Objet objet) { this.objet = objet; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public Statut getStatRec() { return statRec; }
    public void setStatRec(Statut statRec) { this.statRec = statRec; }

    public List<NotificationType> getNotificationTypes() { return notificationTypes; }
    public void setNotificationTypes(List<NotificationType> notificationTypes) { this.notificationTypes = notificationTypes; }

    public String getDescRec() { return descRec; }
    public void setDescRec(String descRec) { this.descRec = descRec; }

    public boolean isNotifiedEmail() { return notifiedEmail; }
    public void setNotifiedEmail(boolean notifiedEmail) { this.notifiedEmail = notifiedEmail; }

    public boolean isNotifiedSms() { return notifiedSms; }
    public void setNotifiedSms(boolean notifiedSms) { this.notifiedSms = notifiedSms; }

    public Long getUserCre() { return userCre; }
    public void setUserCre(Long userCre) { this.userCre = userCre; }

    public Long getUserMod() { return userMod; }
    public void setUserMod(Long userMod) { this.userMod = userMod; }

    public Date getDatCre() { return datCre; }
    public void setDatCre(Date datCre) { this.datCre = datCre; }

    public Date getDatMod() { return datMod; }
    public void setDatMod(Date datMod) { this.datMod = datMod; }

    public String getObsRec() { return obsRec; }
    public void setObsRec(String obsRec) { this.obsRec = obsRec; }

    public String getCompRec() { return compRec; }
    public void setCompRec(String compRec) { this.compRec = compRec; }

    public Date getDatRep() { return datRep; }
    public void setDatRep(Date datRep) { this.datRep = datRep; }

    public boolean isXmlGenerated() { return xmlGenerated; }
    public void setXmlGenerated(boolean xmlGenerated) { this.xmlGenerated = xmlGenerated; }

    public Date getDatGenXml() { return datGenXml; }
    public void setDatGenXml(Date datGenXml) { this.datGenXml = datGenXml; }

    public String getUserGenXml() { return userGenXml; }
    public void setUserGenXml(String userGenXml) { this.userGenXml = userGenXml; }

    // ===== Getters & Setters SLA =====

    public Date getDatLimite() { return datLimite; }
    public void setDatLimite(Date datLimite) { this.datLimite = datLimite; }

    public Boolean getSlaExceeded() { return slaExceeded; }
    public void setSlaExceeded(Boolean slaExceeded) { this.slaExceeded = slaExceeded; }

    public String getSlaStatus() { return slaStatus; }
    public void setSlaStatus(String slaStatus) { this.slaStatus = slaStatus; }

    public Double getSlaRiskScore() { return slaRiskScore; }
    public void setSlaRiskScore(Double slaRiskScore) { this.slaRiskScore = slaRiskScore; }
}