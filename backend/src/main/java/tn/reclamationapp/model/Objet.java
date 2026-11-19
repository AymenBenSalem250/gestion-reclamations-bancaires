package tn.reclamationapp.model;

import jakarta.persistence.*;

@Entity
@Table(name="rec_objet")
public class Objet {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	

	@Column (name="lib_obj")
	private String libObj;
	
	@Column (name="cod_obj")
	private String codObj;
	
	@ManyToOne(optional = false)
	 @JoinColumn(name = "id_cod_ser",referencedColumnName = "cod_ser")
	private Service service;

	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
	}



	public String getLibObj() {
		return libObj;
	}



	public void setLibObj(String libObj) {
		this.libObj = libObj;
	}



	public String getCodObj() {
		return codObj;
	}



	public void setCodObj(String codObj) {
		this.codObj = codObj;
	}



	public Service getService() {
		return service;
	}



	public void setService(Service service) {
		this.service = service;
	}



	public Objet() {
		
	}
	
	
	
	
	
	
	
}
