package tn.reclamationapp.dto;

import java.util.Date;
import java.util.List;
public class ReclamationResponse {

    private String refRec;
    private String typeRec;

    // ✅ Affichage dynamique (libellés)
    private String clientDisplay;   // Numéro compte ou CIN
    private String natureDisplay;   // Nature client ou nature réclamant
    private String canalDisplay;
    private String objetDisplay;
    private String statutDisplay;

    // ✅ IDs pour pré-remplir les selects en modification
    private Long canalId;
    private Long objetId;
    private Long natRecId;

    // ✅ Code statut pour logique UI (bloquer modification si "02"/"03")
    private String statutCode;
    
    private List<Long> notificationTypeIds;

    // ✅ Champs affichés en lecture seule (non modifiables)
    private String emailRec;
    private String telephone;

    private String descRec;
    private String compRec;

    private Date datRec;
    private Date datRep;

    // ===============================
    // GETTERS & SETTERS
    // ===============================

    public String getRefRec() {
        return refRec;
    }

    public void setRefRec(String refRec) {
        this.refRec = refRec;
    }

    public String getTypeRec() {
        return typeRec;
    }

    public void setTypeRec(String typeRec) {
        this.typeRec = typeRec;
    }

    public String getClientDisplay() {
        return clientDisplay;
    }

    public void setClientDisplay(String clientDisplay) {
        this.clientDisplay = clientDisplay;
    }

    public String getNatureDisplay() {
        return natureDisplay;
    }

    public void setNatureDisplay(String natureDisplay) {
        this.natureDisplay = natureDisplay;
    }

    public String getCanalDisplay() {
        return canalDisplay;
    }

    public void setCanalDisplay(String canalDisplay) {
        this.canalDisplay = canalDisplay;
    }

    public String getObjetDisplay() {
        return objetDisplay;
    }

    public void setObjetDisplay(String objetDisplay) {
        this.objetDisplay = objetDisplay;
    }

    public String getStatutDisplay() {
        return statutDisplay;
    }

    public void setStatutDisplay(String statutDisplay) {
        this.statutDisplay = statutDisplay;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public Long getObjetId() {
        return objetId;
    }

    public void setObjetId(Long objetId) {
        this.objetId = objetId;
    }

    public Long getNatRecId() {
        return natRecId;
    }

    public void setNatRecId(Long natRecId) {
        this.natRecId = natRecId;
    }

    public String getStatutCode() {
        return statutCode;
    }

    public void setStatutCode(String statutCode) {
        this.statutCode = statutCode;
    }

    public String getEmailRec() {
        return emailRec;
    }

    public void setEmailRec(String emailRec) {
        this.emailRec = emailRec;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDescRec() {
        return descRec;
    }

    public void setDescRec(String descRec) {
        this.descRec = descRec;
    }

    public String getCompRec() {
        return compRec;
    }

    public void setCompRec(String compRec) {
        this.compRec = compRec;
    }

    public Date getDatRec() {
        return datRec;
    }

    public void setDatRec(Date datRec) {
        this.datRec = datRec;
    }

    public Date getDatRep() {
        return datRep;
    }

    public void setDatRep(Date datRep) {
        this.datRep = datRep;
    }

    public List<Long> getNotificationTypeIds() {
        return notificationTypeIds;
    }

    public void setNotificationTypeIds(List<Long> notificationTypeIds) {
        this.notificationTypeIds = notificationTypeIds;
    }
}