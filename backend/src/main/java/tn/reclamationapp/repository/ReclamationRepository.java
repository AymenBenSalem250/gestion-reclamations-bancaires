package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Reclamation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, String>, JpaSpecificationExecutor<Reclamation> {
    
    List<Reclamation> findByNomRecContainingIgnoreCaseOrPrenRecContainingIgnoreCase(String nom, String prenom);
    
    List<Reclamation> findByStatRec_CodSta(String codSta);
    
    List<Reclamation> findByDatRecBetween(Date startDate, Date endDate);
    
    List<Reclamation> findByObjet_CodObj(String codObj);
    
    List<Reclamation> findByCanal_Id(Long canalId);
    
    List<Reclamation> findByNatRec_Id(Long natRecId);
    
    List<Reclamation> findByXmlGenerated(boolean xmlGenerated);
    
    Optional<Reclamation> findByRefRec(String refRec);
    
    Page<Reclamation> findByObjetServiceCodSer(String codSer, Pageable pageable);
    
    List<Reclamation> findByObjetServiceCodSer(String codSer);
    @Query("SELECT r FROM Reclamation r WHERE r.emailRec = :email")
    List<Reclamation> findByEmail(@Param("email") String email);
    
    @Query("SELECT r FROM Reclamation r WHERE r.numCptRec = :numCpt")
    List<Reclamation> findByNumCompte(@Param("numCpt") String numCpt);
    
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.statRec.codSta = :codSta")
    Long countByStatut(@Param("codSta") String codSta);
    
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.datRec BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT r FROM Reclamation r WHERE r.notifiedEmail = false AND r.statRec.codSta IN ('TRAITE', 'CLOTURE')")
    List<Reclamation> findUnnotifiedReclamations();
    
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE YEAR(r.datRec) = :year")
    long countByYear(@Param("year") int year);

    // ==========================================
    // Requêtes pour le Reporting BCT 760
    // ==========================================

    /**
     * Compte les réclamations par nature (libellé) pour une année donnée.
     * ✅ Couvre les deux cas :
     *   - NON_CLIENT : nat via rec_reclamation.nat_rec → rec_nature_client.lib_nat
     *   - CLIENT TSB  : nat via rec_client.nat_clt (code) → résolu en lib_nat via rec_nature_client
     * Regroupe toujours par libellé (lib_nat), jamais par code.
     */
    @Query(value = """
        SELECT lib_nat, SUM(cnt) AS total
        FROM (
            -- NON_CLIENT : nat_rec pointé directement sur rec_nature_client
            SELECT n.lib_nat, COUNT(*) AS cnt
            FROM rec_reclamation r
            JOIN rec_nature_client n ON r.nat_rec = n.id
            WHERE YEAR(r.dat_rec) = :year
              AND r.typ_rec <> 'CLIENT'
            GROUP BY n.lib_nat

            UNION ALL

            -- CLIENT TSB : nat_clt (code) résolu via rec_nature_client.cod_nat
            SELECT n.lib_nat, COUNT(*) AS cnt
            FROM rec_reclamation r
            JOIN rec_client c ON r.code_clt = c.code_clt
            JOIN rec_nature_client n ON c.nat_clt = n.cod_nat
            WHERE YEAR(r.dat_rec) = :year
              AND r.typ_rec = 'CLIENT'
            GROUP BY n.lib_nat
        ) AS combined
        GROUP BY lib_nat
        """, nativeQuery = true)
    List<Object[]> countByNatureReclamant(@Param("year") int year);

    @Query("SELECT r.canal.libCan, COUNT(r) FROM Reclamation r WHERE YEAR(r.datRec) = :year GROUP BY r.canal.libCan")
    List<Object[]> countByCanal(@Param("year") int year);

    @Query("SELECT r.objet.libObj, COUNT(r) FROM Reclamation r WHERE YEAR(r.datRec) = :year GROUP BY r.objet.libObj")
    List<Object[]> countByObjet(@Param("year") int year);

    @Query("SELECT r.statRec.codSta, COUNT(r) FROM Reclamation r WHERE YEAR(r.datRec) = :year GROUP BY r.statRec.codSta")
    List<Object[]> countByStatut(@Param("year") int year);

    @Query("SELECT MONTH(r.datRec), COUNT(r) FROM Reclamation r WHERE YEAR(r.datRec) = :year GROUP BY MONTH(r.datRec)")
    List<Object[]> countMonthlyEvolution(@Param("year") int year);

    @Query("SELECT r.datRec, r.datRep FROM Reclamation r WHERE YEAR(r.datRec) = :year AND r.datRep IS NOT NULL")
    List<Object[]> findDelaysByYear(@Param("year") int year);

    @Query("SELECT r.datRec, r.datNaisRec, r.genreRec, c.datNais, c.sexClt " +
           "FROM Reclamation r LEFT JOIN r.client c " +
           "WHERE YEAR(r.datRec) = :year")
    List<Object[]> findDemographicsByYear(@Param("year") int year);

    // ==========================================
    // Requêtes pour le Module SLA PREMIUM
    // ==========================================

    /**
     * Calcule le délai moyen de traitement (en jours) par service bancaire.
     * <p>
     * Utilisé par le moteur IA heuristique de {@code SlaAnalyticsService} pour appliquer
     * un malus de +20 points si le délai moyen du service dépasse 12 jours.
     * Seules les réclamations clôturées (datRep non nulle) sont prises en compte.
     * </p>
     *
     * @return Liste de [libSer (String), délaiMoyenJours (Double)]
     */
    @Query("SELECT r.objet.service.libSer, AVG(DATEDIFF(r.datRep, r.datRec)) " +
           "FROM Reclamation r " +
           "WHERE r.datRep IS NOT NULL " +
             "AND r.objet IS NOT NULL " +
             "AND r.objet.service IS NOT NULL " +
           "GROUP BY r.objet.service.libSer")
    List<Object[]> findAvgDelayByService();

    /**
     * Compte le nombre de réclamations actuellement en cours par service bancaire.
     * <p>
     * Utilisé par le moteur IA heuristique de {@code SlaAnalyticsService} pour appliquer
     * un malus de +15 points si la charge du service dépasse 30 réclamations en cours.
     * Le statut "en cours" correspond à {@code codSta = '01'} (cohérent avec ReclamationService).
     * </p>
     *
     * @return Liste de [libSer (String), count (Long)]
     */
    @Query("SELECT r.objet.service.libSer, COUNT(r) " +
           "FROM Reclamation r " +
           "WHERE r.statRec.codSta = '01' " +
             "AND r.objet IS NOT NULL " +
             "AND r.objet.service IS NOT NULL " +
           "GROUP BY r.objet.service.libSer")
    List<Object[]> countEnCoursByService();
}
