package tn.reclamationapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="hab_domaine")
public class Domaine {
	
	@Id
	@Column (name="cod_dom")
	private String codDom;
	

	@Column (name="lib_mod")
	private String libDom;


	public String getCodDom() {
		return codDom;
	}


	public void setCodDom(String codDom) {
		this.codDom = codDom;
	}


	public String getLibDom() {
		return libDom;
	}


	public void setLibDom(String libDom) {
		this.libDom = libDom;
	}


	

	
}
