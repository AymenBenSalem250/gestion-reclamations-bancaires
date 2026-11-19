package tn.reclamationapp.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "hab_role")
public class Role {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (name="lib_role")
    private String librole;

    @Column (name="cod_role")
    private String codrole;

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Privilege> privileges;

    public Role() {
    }

    public Role(String name) {
        this.librole = name;
        this.codrole = name;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLibrole() {
        return librole;
    }

    public void setLibrole(String librole) {
        this.librole = librole;
    }

    public String getCodrole() {
        return codrole;
    }

    public void setCodrole(String codrole) {
        this.codrole = codrole;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }


    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", librole='" + librole + '\'' +
                ", codrole='" + codrole + '\'' +
                ", privileges=" + privileges +
                '}';
    }
}
