package tn.reclamationapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="rec_statut")
public class Statut {
	
	@Id
	@Column (name="cod_sta")
	private String codSta;
	

	@Column (name="lib_sta")
	private String libSta;


    @NotBlank(message = "Message principal obligatoire")
    @Column(name = "msg_sta", nullable = false)
	private String msgSta;

    @NotBlank(message = "Message secondaire obligatoire")
    @Column(name = "msg2_sta", nullable = false)
	private String msg2Sta;

	public String getMsgSta() {
		return msgSta;
	}







	public void setMsgSta(String msgSta) {
		this.msgSta = msgSta;
	}







	public String getCodSta() {
		return codSta;
	}







	public void setCodSta(String codSta) {
		this.codSta = codSta;
	}







	public String getLibSta() {
		return libSta;
	}







	public void setLibSta(String libSta) {
		this.libSta = libSta;
	}







	public String getMsg2Sta() {
		return msg2Sta;
	}







	public void setMsg2Sta(String msg2Sta) {
		this.msg2Sta = msg2Sta;
	}







	public Statut() {
		
	}
	
	
	
	
}
