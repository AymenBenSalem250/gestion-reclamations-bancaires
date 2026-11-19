package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.ReclamationMessage;

import java.util.List;

@Repository
public interface ReclamationMessageRepository extends JpaRepository<ReclamationMessage, Long> {
    List<ReclamationMessage> findByReclamationRefRecOrderByCreatedAtAsc(String refRec);
}
