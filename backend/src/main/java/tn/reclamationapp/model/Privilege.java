package tn.reclamationapp.model;

import jakarta.persistence.*;

@Entity
@Table(name="hab_privilege")
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cod_role",referencedColumnName = "id")
    private Role role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cod_menu",referencedColumnName = "cod_menu")
    private Menu menu;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
