package tn.reclamationapp.dto;

import java.util.Date;

public class NotificationDto {
    private Long id;
    private String message;
    private Boolean isRead;
    private Date createdAt;
    private String refRec;
    private String userId;

    public NotificationDto() {
    }

    public NotificationDto(Long id, String message, Boolean isRead, Date createdAt, String refRec, String userId) {
        this.id = id;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.refRec = refRec;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getRefRec() {
        return refRec;
    }

    public void setRefRec(String refRec) {
        this.refRec = refRec;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
