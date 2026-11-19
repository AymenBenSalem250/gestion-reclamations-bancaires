package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Canal;

import java.util.List;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Long> {
    
    List<Canal> findByLibCanContainingIgnoreCase(String libCan);
}
