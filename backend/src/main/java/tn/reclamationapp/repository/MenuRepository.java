package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Menu;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, String> {
    
    Optional<Menu> findByCodmenu(String codmenu);
}
