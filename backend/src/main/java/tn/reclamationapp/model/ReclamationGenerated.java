package tn.reclamationapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name="rec_reclamation_gen")
public class ReclamationGenerated {
	
	@Id
	@Column (name="ref_rec")
	private String refRec;
	

	@DateTimeFormat(pattern="dd/MM/yyyy")
    @Column (name="dat_gen_xml")
    private Date datGenXml;

	

	public String getRefRec() {
		return refRec;
	}

	public void setRefRec(String refRec) {
		this.refRec = refRec;
	}

	public Date getDatGenXml() {
		return datGenXml;
	}

	public void setDatGenXml(Date datGenXml) {
		this.datGenXml = datGenXml;
	}

	public ReclamationGenerated() {
		
	}
	
	
	
	
	
	
	
}
