package tn.reclamationapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="hab_module")
public class Module {
	
	@Id
	@Column (name="cod_mod")
	private String codMod;
	

	@Column (name="lib_mod")
	private String libMod;
	
	@Column (name="sta_mod")
	private String staMod;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cod_dom",referencedColumnName = "cod_dom")
	private Domaine domaine;
	
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
	private List<Menu> menus;
	
	
	public Domaine getDomaine() {
		return domaine;
	}


	public String getStaMod() {
		return staMod;
	}


	public void setStaMod(String staMod) {
		this.staMod = staMod;
	}


	public void setDomaine(Domaine domaine) {
		this.domaine = domaine;
	}


	public String getCodMod() {
		return codMod;
	}


	public void setCodMod(String codMod) {
		this.codMod = codMod;
	}


	public String getLibMod() {
		return libMod;
	}


	public void setLibMod(String libMod) {
		this.libMod = libMod;
	}


	public List<Menu> getMenus() {
		return menus;
	}


	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}
}
