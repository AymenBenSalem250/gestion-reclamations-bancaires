package tn.reclamationapp.model;


import jakarta.persistence.*;

import java.lang.Module;


@Entity
@Table(name = "sed_cci_path")
public class Path {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (name="path")
    private String path;

    @Column (name="typ_path")
    private String typPath;

    @ManyToOne(optional = false)
     @JoinColumn(name = "id_user",referencedColumnName = "id")
    private Users user;

    @ManyToOne(optional = false)
     @JoinColumn(name = "cod_mod",referencedColumnName = "cod_mod")
    private tn.reclamationapp.model.Module module;


    public Path() {}


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getTypPath() {
        return typPath;
    }


    public void setTypPath(String typPath) {
        this.typPath = typPath;
    }


    public Users getUser() {
        return user;
    }


    public void setUser(Users user) {
        this.user = user;
    }


    public tn.reclamationapp.model.Module getModule() {
        return module;
    }


    public void setModule(tn.reclamationapp.model.Module module) {
        this.module = module;
    }









}

	

	

