package tn.reclamationapp.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public class ReclamationUpdateRequest {

    // ✅ Champs modifiables autorisés uniquement
    @Size(min = 10, max = 2000, message = "La description doit contenir entre 10 et 2000 caractères")
    private String descRec;

    private String compRec;

    private Long canalId;
    private Long objetId;
    private Long natRecId;
    private List<Long> notificationTypeIds;

    // ❌ telephone et emailRec supprimés — ces champs ne sont JAMAIS modifiables

    public String getDescRec() { return descRec; }
    public void setDescRec(String descRec) { this.descRec = descRec; }

    public String getCompRec() { return compRec; }
    public void setCompRec(String compRec) { this.compRec = compRec; }

    public Long getCanalId() { return canalId; }
    public void setCanalId(Long canalId) { this.canalId = canalId; }

    public Long getObjetId() { return objetId; }
    public void setObjetId(Long objetId) { this.objetId = objetId; }

    public Long getNatRecId() { return natRecId; }
    public void setNatRecId(Long natRecId) { this.natRecId = natRecId; }

    public List<Long> getNotificationTypeIds() { return notificationTypeIds; }
    public void setNotificationTypeIds(List<Long> notificationTypeIds) { this.notificationTypeIds = notificationTypeIds; }
}