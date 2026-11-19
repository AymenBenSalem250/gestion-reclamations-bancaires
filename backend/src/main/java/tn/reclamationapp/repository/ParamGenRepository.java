package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.ParamGen;

import java.util.Optional;

@Repository
public interface ParamGenRepository extends JpaRepository<ParamGen, Long> {
    
    Optional<ParamGen> findByLibParam(String libParam);
}
