package tn.reclamationapp.model;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name="hab_users",uniqueConstraints = @UniqueConstraint(columnNames = "email"))

public class Users {
	
	@Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
	private String password;
	private String Apassword;
	private String firstName;
	private String lastName;
	private String email;

    private Boolean disabled;

    @Column(name = "dpwd")
    private Boolean defaultPassword;

    @Column(name = "nbcnx")
    private int nbrAttemps;
	
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "hab_users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    public Users(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    
    public Users() {
		
	}

	public Users(String password, String apassword, String firstName, String lastName, String email, Boolean disabled,
                 Boolean defaultPassword, int nbrAttemps, Collection<Role> roles, Collection<Service> services) {
		super();
		this.password = password;
		Apassword = apassword;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.disabled = disabled;
		this.defaultPassword = defaultPassword;
		this.nbrAttemps = nbrAttemps;
		this.roles = roles;
		
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    

    public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getApassword() {

		return Apassword;
	}

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(Boolean defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public int getNbrAttemps() {
        return nbrAttemps;
    }

    public void setNbrAttemps(int nbrAttemps) {
        this.nbrAttemps = nbrAttemps;
    }

    public void setApassword(String Apassword) {
		this.Apassword = Apassword;
	}

	public void setRoles(List<Role> asList) {
		this.roles = asList;
		
	}

	public void setRoles(Collection<Role> roles) {
		this.roles = roles;
	}

	
	
	@Override
	public String toString() {
		return "Users [id=" + id + ", password=" + password + ", Apassword=" + Apassword + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", email=" + email + ", disabled=" + disabled + ", defaultPassword="
				+ defaultPassword + ", nbrAttemps=" + nbrAttemps + ", roles=" + roles + "]";
	}


	public String getRoleNames() {
        return roles.stream()
                     .map(Role::getLibrole)
                     .collect(Collectors.joining(", "));
    }



}