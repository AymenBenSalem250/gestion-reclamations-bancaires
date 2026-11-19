package tn.reclamationapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Entity
@Table(
        name = "rec_client",
        indexes = {
                @Index(name = "idx_client_num_cpt", columnList = "num_cpt"),
                @Index(name = "idx_client_code_clt", columnList = "code_clt")
        }
)
public class Client {

    @Id
    @Column(name = "code_clt", length = 50)
    private String codeClt;

    @NotBlank
    @Column(name = "num_cpt", length = 50, nullable = false)
    private String numCpt;

    @Column(name = "lib_cpt", length = 150)
    private String libCpt;

    @Column(name = "ug_clt", length = 100)
    private String ugClt;

    @Column(name = "nat_clt", length = 50)
    private String natClt;

    @Column(name = "nom_clt", length = 100)
    private String nomClt;

    @Column(name = "pren_clt", length = 100)
    private String prenClt;

    @Column(name = "fnc_clt", length = 100)
    private String fncClt;

    @Temporal(TemporalType.DATE)
    @Column(name = "dat_nais")
    private Date datNais;

    @Column(name = "typ_pid", length = 20)
    private String typPid;

    @Column(name = "num_pid", length = 50)
    private String numPid;

    @Column(name = "sex_clt", length = 10)
    private String sexClt;

    @Column(name = "mob_per")
    private Long mobPer;

    @Email
    @Column(name = "email_per", length = 150)
    private String emailPer;

    @Column(name = "cod_typ_per", length = 50)
    private String codTypPer;

    // ===========================
    // Constructors
    // ===========================

    public Client() {}

    // ===========================
    // Getters & Setters
    // ===========================

    public String getCodeClt() {
        return codeClt;
    }

    public void setCodeClt(String codeClt) {
        this.codeClt = codeClt;
    }

    public String getNumCpt() {
        return numCpt;
    }

    public void setNumCpt(String numCpt) {
        this.numCpt = numCpt;
    }

    public String getLibCpt() {
        return libCpt;
    }

    public void setLibCpt(String libCpt) {
        this.libCpt = libCpt;
    }

    public String getUgClt() {
        return ugClt;
    }

    public void setUgClt(String ugClt) {
        this.ugClt = ugClt;
    }

    public String getNatClt() {
        return natClt;
    }

    public void setNatClt(String natClt) {
        this.natClt = natClt;
    }

    public String getNomClt() {
        return nomClt;
    }

    public void setNomClt(String nomClt) {
        this.nomClt = nomClt;
    }

    public String getPrenClt() {
        return prenClt;
    }

    public void setPrenClt(String prenClt) {
        this.prenClt = prenClt;
    }

    public String getFncClt() {
        return fncClt;
    }

    public void setFncClt(String fncClt) {
        this.fncClt = fncClt;
    }

    public Date getDatNais() {
        return datNais;
    }

    public void setDatNais(Date datNais) {
        this.datNais = datNais;
    }

    public String getTypPid() {
        return typPid;
    }

    public void setTypPid(String typPid) {
        this.typPid = typPid;
    }

    public String getNumPid() {
        return numPid;
    }

    public void setNumPid(String numPid) {
        this.numPid = numPid;
    }

    public String getSexClt() {
        return sexClt;
    }

    public void setSexClt(String sexClt) {
        this.sexClt = sexClt;
    }

    public Long getMobPer() {
        return mobPer;
    }

    public void setMobPer(Long mobPer) {
        this.mobPer = mobPer;
    }

    public String getEmailPer() {
        return emailPer;
    }

    public void setEmailPer(String emailPer) {
        this.emailPer = emailPer;
    }

    public String getCodTypPer() {
        return codTypPer;
    }

    public void setCodTypPer(String codTypPer) {
        this.codTypPer = codTypPer;
    }
}