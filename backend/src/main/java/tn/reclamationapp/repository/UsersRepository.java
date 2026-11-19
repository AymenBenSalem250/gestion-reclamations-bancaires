package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Users;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    
    Optional<Users> findByEmail(String email);
    
    Optional<Users> findById(String id);
    
    boolean existsByEmail(String email);
    
    List<Users> findByDisabled(Boolean disabled);
    
    @Query("SELECT u FROM Users u JOIN u.roles r WHERE r.codrole = :roleCode")
    List<Users> findByRoleCode(@Param("roleCode") String roleCode);
    
    @Query("SELECT u FROM Users u WHERE u.defaultPassword = true")
    List<Users> findUsersWithDefaultPassword();
    
    @Query("SELECT u FROM Users u WHERE u.nbrAttemps >= :maxAttempts")
    List<Users> findUsersWithMaxAttempts(@Param("maxAttempts") int maxAttempts);
    
    @Query("SELECT u FROM Users u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Users> searchUsers(@Param("search") String search);

    @Query("SELECT u FROM Users u JOIN u.roles r WHERE (r.codrole = 'ROLE_ADMIN' OR r.codrole = 'ADMIN') AND u.service.codSer = :serviceCode")
    List<Users> findAdminsByServiceCode(@Param("serviceCode") String serviceCode);

    @Query("SELECT u FROM Users u WHERE u.service.codSer = :serviceCode")
    List<Users> findByServiceCodSer(@Param("serviceCode") String serviceCode);
}
