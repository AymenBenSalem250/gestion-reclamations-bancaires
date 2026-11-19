package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Module;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, String> {
    
    Optional<Module> findByCodMod(String codMod);
    
    List<Module> findByDomaine_CodDom(String codDom);
    
    List<Module> findByStaMod(String staMod);
}
