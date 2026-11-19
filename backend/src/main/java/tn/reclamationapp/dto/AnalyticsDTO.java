package tn.reclamationapp.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsDTO {
    
    private long total;
    private long ouvertes;
    private long cloturees;
    private long favorables;
    private long rejets;
    
    private Map<String, Long> repartitionNature;
    private Map<String, Long> repartitionCanal;
    private Map<String, Long> repartitionObjet;
    
    private List<Map<String, Object>> evolutionMensuelle;
    
    private Map<String, Map<String, Long>> repartitionGenreAge;
    
    // Statistiques de délais
    private double delaiMoyenJours;
    private long delaiMinJours;
    private long delaiMaxJours;

    public AnalyticsDTO() {}

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getOuvertes() {
        return ouvertes;
    }

    public void setOuvertes(long ouvertes) {
        this.ouvertes = ouvertes;
    }

    public long getCloturees() {
        return cloturees;
    }

    public void setCloturees(long cloturees) {
        this.cloturees = cloturees;
    }

    public long getFavorables() {
        return favorables;
    }

    public void setFavorables(long favorables) {
        this.favorables = favorables;
    }

    public long getRejets() {
        return rejets;
    }

    public void setRejets(long rejets) {
        this.rejets = rejets;
    }

    public Map<String, Long> getRepartitionNature() {
        return repartitionNature;
    }

    public void setRepartitionNature(Map<String, Long> repartitionNature) {
        this.repartitionNature = repartitionNature;
    }

    public Map<String, Long> getRepartitionCanal() {
        return repartitionCanal;
    }

    public void setRepartitionCanal(Map<String, Long> repartitionCanal) {
        this.repartitionCanal = repartitionCanal;
    }

    public Map<String, Long> getRepartitionObjet() {
        return repartitionObjet;
    }

    public void setRepartitionObjet(Map<String, Long> repartitionObjet) {
        this.repartitionObjet = repartitionObjet;
    }

    public List<Map<String, Object>> getEvolutionMensuelle() {
        return evolutionMensuelle;
    }

    public void setEvolutionMensuelle(List<Map<String, Object>> evolutionMensuelle) {
        this.evolutionMensuelle = evolutionMensuelle;
    }

    public Map<String, Map<String, Long>> getRepartitionGenreAge() {
        return repartitionGenreAge;
    }

    public void setRepartitionGenreAge(Map<String, Map<String, Long>> repartitionGenreAge) {
        this.repartitionGenreAge = repartitionGenreAge;
    }

    public double getDelaiMoyenJours() {
        return delaiMoyenJours;
    }

    public void setDelaiMoyenJours(double delaiMoyenJours) {
        this.delaiMoyenJours = delaiMoyenJours;
    }

    public long getDelaiMinJours() {
        return delaiMinJours;
    }

    public void setDelaiMinJours(long delaiMinJours) {
        this.delaiMinJours = delaiMinJours;
    }

    public long getDelaiMaxJours() {
        return delaiMaxJours;
    }

    public void setDelaiMaxJours(long delaiMaxJours) {
        this.delaiMaxJours = delaiMaxJours;
    }
}
