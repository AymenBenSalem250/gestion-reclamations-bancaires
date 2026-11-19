package tn.reclamationapp.dto;

import java.util.Date;

public class ReclamationMessageResponse {

    private Long id;
    private String message;
    private Date createdAt;
    private Boolean isAdmin;
    private String userId;
    private String userFullName;

    // ======================
    // FILE INFORMATION
    // ======================

    private String fileName;
    private String fileType;
    private Long fileSize;

    public ReclamationMessageResponse() {
    }

    public ReclamationMessageResponse(Long id,
                                      String message,
                                      Date createdAt,
                                      Boolean isAdmin,
                                      String userId,
                                      String userFullName,
                                      String fileName,
                                      String fileType,
                                      Long fileSize) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
        this.isAdmin = isAdmin;
        this.userId = userId;
        this.userFullName = userFullName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    // ======================
    // GETTERS & SETTERS
    // ======================

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}