package tn.reclamationapp.dto;

/**
 * DTO pour le traitement d'une réclamation.
 * Reçoit le nouveau code statut et le commentaire agent (obligatoire si rejet "03").
 */
public class StatusUpdateRequest {

    private String codSta;

    // Obligatoire si codSta == "03" (Rejeté)
    private String commentaire;

    public String getCodSta() { return codSta; }
    public void setCodSta(String codSta) { this.codSta = codSta; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
