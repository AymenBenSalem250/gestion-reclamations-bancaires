package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.NatureReclamant;

import java.util.List;
import java.util.Optional;

@Repository
public interface NatureReclamantRepository extends JpaRepository<NatureReclamant, Long> {
    
    Optional<NatureReclamant> findByCodNat(String codNat);
    
    List<NatureReclamant> findByLibNatContainingIgnoreCase(String libNat);
    
    boolean existsByCodNat(String codNat);
}
