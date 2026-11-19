package tn.reclamationapp.model;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "ReclamationDt")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReclamationDt {

    @XmlElement(name = "ReclamationsByNat")
    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, Long> reclamationsByNat = new HashMap<>();

    @XmlElement(name = "ReclamationsByGenreAndAge")
    @XmlJavaTypeAdapter(NestedMapAdapter.class) // ✅ Utilisation du nouvel adaptateur
    private Map<String, Map<String, Long>> reclamationsByGenreAndAge = new HashMap<>();

    @XmlElement(name = "ReclamationsByCanal")
    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, Long> reclamationsByCanal = new HashMap<>();

    @XmlElement(name = "Statistiques")
    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, Long> statistiques = new HashMap<>();

    @XmlElement(name = "AgeJeune")
    private int ageJeune;

    @XmlElement(name = "AgeIntermediate")
    private int ageIntermediate;

    @XmlElement(name = "AgeMiddle")
    private int ageMiddle;

    @XmlElement(name = "DateAnnexe")
    private Date dateAnnexe;

	public Map<String, Long> getReclamationsByNat() {
		return reclamationsByNat;
	}

	public void setReclamationsByNat(Map<String, Long> reclamationsByNat) {
		this.reclamationsByNat = reclamationsByNat;
	}

	public Map<String, Map<String, Long>> getReclamationsByGenreAndAge() {
		return reclamationsByGenreAndAge;
	}

	public void setReclamationsByGenreAndAge(Map<String, Map<String, Long>> reclamationsByGenreAndAge) {
		this.reclamationsByGenreAndAge = reclamationsByGenreAndAge;
	}

	public Map<String, Long> getReclamationsByCanal() {
		return reclamationsByCanal;
	}

	public void setReclamationsByCanal(Map<String, Long> reclamationsByCanal) {
		this.reclamationsByCanal = reclamationsByCanal;
	}

	public Map<String, Long> getStatistiques() {
		return statistiques;
	}

	public void setStatistiques(Map<String, Long> statistiques) {
		this.statistiques = statistiques;
	}

	public int getAgeJeune() {
		return ageJeune;
	}

	public void setAgeJeune(int ageJeune) {
		this.ageJeune = ageJeune;
	}

	public int getAgeIntermediate() {
		return ageIntermediate;
	}

	public void setAgeIntermediate(int ageIntermediate) {
		this.ageIntermediate = ageIntermediate;
	}

	public int getAgeMiddle() {
		return ageMiddle;
	}

	public void setAgeMiddle(int ageMiddle) {
		this.ageMiddle = ageMiddle;
	}

	public Date getDateAnnexe() {
		return dateAnnexe;
	}

	public void setDateAnnexe(Date dateAnnexe) {
		this.dateAnnexe = dateAnnexe;
	}

    // Getters et setters
    
    
}
