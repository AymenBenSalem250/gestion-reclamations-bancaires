package tn.reclamationapp.model;

import jakarta.persistence.*;

@Entity
@Table(name="rec_nature_client")
public class NatureReclamant {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	

	@Column (name="lib_nat")
	private String libNat;
	
	@Column (name="cod_nat")
	private String codNat;
	



	public NatureReclamant() {
		
	}








	public String getCodNat() {
		return codNat;
	}








	public void setCodNat(String codNat) {
		this.codNat = codNat;
	}








	public long getId() {
		return id;
	}








	public void setId(long id) {
		this.id = id;
	}








	public String getLibNat() {
		return libNat;
	}








	public void setLibNat(String libNat) {
		this.libNat = libNat;
	}

	
	
	
}
