package tn.reclamationapp.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO for creating / updating a Réclamation.
 * All banking validation constraints are enforced here so the entity layer
 * stays free of presentation-level rules.
 */
public class ReclamationRequest {

    // ─────────────────────────────────────────────────────────────────────────
    // Type de réclamant : "CLIENT" | "NON_CLIENT"
    // ─────────────────────────────────────────────────────────────────────────

    @NotBlank(message = "Le type de réclamant est obligatoire (CLIENT ou NON_CLIENT)")
    private String typeRec;

    // ─────────────────────────────────────────────────────────────────────────
    // Champs Client TSB (obligatoires si typeRec = "CLIENT")
    // ─────────────────────────────────────────────────────────────────────────

    private String codeClt;   // obligatoire si CLIENT
    private String numCpt;    // obligatoire si CLIENT

    // ─────────────────────────────────────────────────────────────────────────
    // Identité réclamant
    // ─────────────────────────────────────────────────────────────────────────


    @Size(min = 2, max = 100, message = "Nom doit contenir entre 2 et 100 caractères")
    private String nomRec;


    @Size(min = 2, max = 100, message = "Prénom doit contenir entre 2 et 100 caractères")
    private String prenRec;

    // ─────────────────────────────────────────────────────────────────────────
    // CIN (8 chiffres) — obligatoire si NON_CLIENT (sauf si matriculeFiscal fourni)
    // ─────────────────────────────────────────────────────────────────────────

    @Pattern(regexp = "^[0-9]{8}$", message = "CIN doit contenir exactement 8 chiffres")
    private String cin;

    // Matricule fiscal — alternatif au CIN pour NON_CLIENT personne morale
    private String matriculeFiscal;

    // ─────────────────────────────────────────────────────────────────────────
    // Coordonnées
    // ─────────────────────────────────────────────────────────────────────────

    @NotBlank(message = "Téléphone obligatoire")
    @Pattern(regexp = "^[0-9]{8}$", message = "Téléphone doit contenir exactement 8 chiffres")
    private String telephone;

    @NotBlank(message = "Email obligatoire")
    @Email(message = "Adresse email invalide")
    private String emailRec;

    // ─────────────────────────────────────────────────────────────────────────
    // Réclamation
    // ─────────────────────────────────────────────────────────────────────────

    @NotBlank(message = "Description obligatoire")
    @Size(min = 10, max = 2000, message = "Description doit contenir entre 10 et 2000 caractères")
    private String descRec;

    @NotNull(message = "Canal obligatoire")
    private Long canalId;

    @NotNull(message = "Objet obligatoire")
    private Long objetId;

    private List<Long> notificationTypeIds;

    // Nature réclamant — obligatoire si NON_CLIENT
    private Long natRecId;

    // ─────────────────────────────────────────────────────────────────────────
    // Champs complémentaires (optionnels)
    // ─────────────────────────────────────────────────────────────────────────

    private String raisocRec;       // Raison sociale (personne morale)
    private String fonctRec;        // Fonction
    private String genreRec;        // Genre
    private String typPidRec;       // Type pièce d'identité
    private String numPidRec;       // Numéro pièce d'identité
    private String obsRec;          // Observations internes
    private String numCptRec;       // Numéro compte (NON_CLIENT peut en fournir un)

    // ─────────────────────────────────────────────────────────────────────────
    // Validation métier avancée (cross-field)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valide les règles conditionnelles selon le type de réclamant.
     * Appelé explicitement dans le contrôleur via {@code validate()}.
     *
     * @throws IllegalArgumentException si une règle métier est violée
     */
    public void validate() {
        if ("CLIENT".equalsIgnoreCase(typeRec)) {
            if (codeClt == null || codeClt.isBlank()) {
                throw new IllegalArgumentException("Le code client (codeClt) est obligatoire pour un client TSB");
            }
            if (numCpt == null || numCpt.isBlank()) {
                throw new IllegalArgumentException("Le numéro de compte (numCpt) est obligatoire pour un client TSB");
            }
        } else if ("NON_CLIENT".equalsIgnoreCase(typeRec)) {
            if (nomRec == null || nomRec.isBlank()) {
                throw new IllegalArgumentException("Le nom est obligatoire pour un non-client");
            }
            if (natRecId == null) {
                throw new IllegalArgumentException("La nature du réclamant est obligatoire pour un non-client");
            }
            boolean hasCin = cin != null && !cin.isBlank();
            boolean hasMatricule = matriculeFiscal != null && !matriculeFiscal.isBlank();
            if (!hasCin && !hasMatricule) {
                throw new IllegalArgumentException("Le CIN ou le matricule fiscal est obligatoire pour un non-client");
            }
        } else {
            throw new IllegalArgumentException("Type de réclamant invalide : doit être CLIENT ou NON_CLIENT");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public String getTypeRec() { return typeRec; }
    public void setTypeRec(String typeRec) { this.typeRec = typeRec; }

    public String getCodeClt() { return codeClt; }
    public void setCodeClt(String codeClt) { this.codeClt = codeClt; }

    public String getNumCpt() { return numCpt; }
    public void setNumCpt(String numCpt) { this.numCpt = numCpt; }

    public String getNomRec() { return nomRec; }
    public void setNomRec(String nomRec) { this.nomRec = nomRec; }

    public String getPrenRec() { return prenRec; }
    public void setPrenRec(String prenRec) { this.prenRec = prenRec; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getMatriculeFiscal() { return matriculeFiscal; }
    public void setMatriculeFiscal(String matriculeFiscal) { this.matriculeFiscal = matriculeFiscal; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmailRec() { return emailRec; }
    public void setEmailRec(String emailRec) { this.emailRec = emailRec; }

    public String getDescRec() { return descRec; }
    public void setDescRec(String descRec) { this.descRec = descRec; }

    public Long getCanalId() { return canalId; }
    public void setCanalId(Long canalId) { this.canalId = canalId; }

    public Long getObjetId() { return objetId; }
    public void setObjetId(Long objetId) { this.objetId = objetId; }

    public List<Long> getNotificationTypeIds() { return notificationTypeIds; }
    public void setNotificationTypeIds(List<Long> notificationTypeIds) { this.notificationTypeIds = notificationTypeIds; }

    public Long getNatRecId() { return natRecId; }
    public void setNatRecId(Long natRecId) { this.natRecId = natRecId; }

    public String getRaisocRec() { return raisocRec; }
    public void setRaisocRec(String raisocRec) { this.raisocRec = raisocRec; }

    public String getFonctRec() { return fonctRec; }
    public void setFonctRec(String fonctRec) { this.fonctRec = fonctRec; }

    public String getGenreRec() { return genreRec; }
    public void setGenreRec(String genreRec) { this.genreRec = genreRec; }

    public String getTypPidRec() { return typPidRec; }
    public void setTypPidRec(String typPidRec) { this.typPidRec = typPidRec; }

    public String getNumPidRec() { return numPidRec; }
    public void setNumPidRec(String numPidRec) { this.numPidRec = numPidRec; }

    public String getObsRec() { return obsRec; }
    public void setObsRec(String obsRec) { this.obsRec = obsRec; }

    public String getNumCptRec() { return numCptRec; }
    public void setNumCptRec(String numCptRec) { this.numCptRec = numCptRec; }
}
