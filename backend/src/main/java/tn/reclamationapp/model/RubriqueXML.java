package tn.reclamationapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="rec_rubrique_xml")
public class RubriqueXML {
	
	@Id
	@Column (name="cod_rub")
	private String codRub;
	
	@Column (name="lib_rub")
	private String libRub;
	
	@Column (name="abrv_rub")
	private String abrvRub;
	

	public String getCodRub() {
		return codRub;
	}



	public void setCodRub(String codRub) {
		this.codRub = codRub;
	}



	public String getLibRub() {
		return libRub;
	}



	public String getAbrvRub() {
		return abrvRub;
	}



	public void setAbrvRub(String abrvRub) {
		this.abrvRub = abrvRub;
	}



	public void setLibRub(String libRub) {
		this.libRub = libRub;
	}



	public RubriqueXML() {
		
	}
	
	
	
	
	
	
	
}
