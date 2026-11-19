package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Statut;

import java.util.Optional;

@Repository
public interface StatutRepository extends JpaRepository<Statut, String> {
    
    Optional<Statut> findByCodSta(String codSta);
    
    boolean existsByCodSta(String codSta);
}
