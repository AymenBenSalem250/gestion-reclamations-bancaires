package tn.reclamationapp.model;

import jakarta.persistence.*;

@Entity
@Table(name="rec_param_gen")
public class ParamGen {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	

	@Column (name="lib_param")
	private String libParam;
	
	@Column (name="val_param")
	private String valParam;

	public ParamGen() {
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLibParam() {
		return libParam;
	}

	public void setLibParam(String libParam) {
		this.libParam = libParam;
	}

	public String getValParam() {
		return valParam;
	}

	public void setValParam(String valParam) {
		this.valParam = valParam;
	}
	
	
	
	
	
	
	
}
