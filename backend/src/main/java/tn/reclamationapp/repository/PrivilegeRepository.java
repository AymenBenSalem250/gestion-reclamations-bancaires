package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Privilege;

import java.util.List;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    
    List<Privilege> findByRole_Id(Long roleId);
    
    List<Privilege> findByMenu_Codmenu(String codMenu);
    
    @Query("SELECT p FROM Privilege p WHERE p.role.id = :roleId AND p.menu.codmenu = :codMenu")
    Privilege findByRoleAndMenu(@Param("roleId") Long roleId, @Param("codMenu") String codMenu);
    
    void deleteByRole_Id(Long roleId);
}
