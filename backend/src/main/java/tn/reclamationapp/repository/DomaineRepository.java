package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Domaine;

import java.util.Optional;

@Repository
public interface DomaineRepository extends JpaRepository<Domaine, String> {
    
    Optional<Domaine> findByCodDom(String codDom);
}
