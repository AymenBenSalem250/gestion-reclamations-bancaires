package tn.reclamationapp.model;

import jakarta.persistence.*;
import java.util.Date;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "rec_reclamation_message")
public class ReclamationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", length = 1000, nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin;

    // =======================
    // FILE INFORMATION
    // =======================

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    // =======================
    // RELATIONS
    // =======================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ref_rec", referencedColumnName = "ref_rec", nullable = false)
    private Reclamation reclamation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private Users user;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ReclamationMessage() {
        this.createdAt = new Date();
    }

    public ReclamationMessage(String message, Reclamation reclamation, Users user, Boolean isAdmin) {
        this.message = message;
        this.reclamation = reclamation;
        this.user = user;
        this.isAdmin = isAdmin;
        this.createdAt = new Date();
    }

    public ReclamationMessage(String message, Reclamation reclamation, Users user, Boolean isAdmin,
                              String fileName, String filePath, String fileType, Long fileSize) {
        this.message = message;
        this.reclamation = reclamation;
        this.user = user;
        this.isAdmin = isAdmin;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = new Date();
    }

    // =======================
    // GETTERS & SETTERS
    // =======================

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}