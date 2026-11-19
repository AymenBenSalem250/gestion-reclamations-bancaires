package tn.reclamationapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rec_canal")
public class Canal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(name = "lib_can")
	private String libCan;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLibCan() {
		return libCan;
	}

	public void setLibCan(String libCan) {
		this.libCan = libCan;
	}

	public Canal() {

	}

}
