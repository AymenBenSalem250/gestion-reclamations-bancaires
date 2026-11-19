package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Objet;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjetRepository extends JpaRepository<Objet, Long> {
    
    Optional<Objet> findByCodObj(String codObj);
    
    List<Objet> findByService_CodSer(String codSer);
    
    List<Objet> findByLibObjContainingIgnoreCase(String libObj);
    
    boolean existsByCodObj(String codObj);
}
