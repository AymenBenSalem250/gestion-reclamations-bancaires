package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByCodrole(String codrole);
    
    Optional<Role> findByLibrole(String librole);
    
    boolean existsByCodrole(String codrole);
}
