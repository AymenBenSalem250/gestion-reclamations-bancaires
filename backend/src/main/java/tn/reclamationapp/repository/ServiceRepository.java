package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Service;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    
    Optional<Service> findByCodSer(String codSer);
    
    List<Service> findByLibSerContainingIgnoreCase(String libSer);
    
    boolean existsByCodSer(String codSer);
}
