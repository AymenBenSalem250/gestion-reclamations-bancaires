package tn.reclamationapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="rec_service")
public class Service {
	
	@Id

	@Column (name="cod_ser")
	private String codSer;
	
	@Column (name="lib_ser")
	private String libSer;
	
	@Column (name="email1_ser")
	private String email1Ser;
	
	@Column (name="email2_ser")
	private String email2Ser;
	

	
	public String getCodSer() {
		return codSer;
	}



	public void setCodSer(String codSer) {
		this.codSer = codSer;
	}



	public String getLibSer() {
		return libSer;
	}



	public void setLibSer(String libSer) {
		this.libSer = libSer;
	}



	public String getEmail1Ser() {
		return email1Ser;
	}



	public void setEmail1Ser(String email1Ser) {
		this.email1Ser = email1Ser;
	}



	public String getEmail2Ser() {
		return email2Ser;
	}



	public void setEmail2Ser(String email2Ser) {
		this.email2Ser = email2Ser;
	}



	public Service() {
		
	}
	
	
	
	
	
	
	
}
