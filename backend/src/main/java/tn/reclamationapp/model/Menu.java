package tn.reclamationapp.model;

import jakarta.persistence.*;

@Entity
@Table(name="hab_menu")
public class Menu {
	
	@Id
	@Column (name="cod_menu")
	private String codmenu;
	

	@Column (name="lib_menu")
	private String libmenu;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "codmod",referencedColumnName = "cod_mod")
	private tn.reclamationapp.model.Module module;

	public String getCodmenu() {
		return codmenu;
	}

	public void setCodmenu(String codmenu) {
		this.codmenu = codmenu;
	}

	public String getLibmenu() {
		return libmenu;
	}

	public void setLibmenu(String libmenu) {
		this.libmenu = libmenu;
	}

	

	public tn.reclamationapp.model.Module getModule() {
		return module;
	}

	public void setModule(tn.reclamationapp.model.Module module) {
		this.module = module;
	}

	public Menu() {
		
	}
	
	
	
	
	
	
	
	
	
}
