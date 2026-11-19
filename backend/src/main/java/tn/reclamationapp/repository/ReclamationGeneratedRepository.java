package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.ReclamationGenerated;

import java.util.Date;
import java.util.List;

@Repository
public interface ReclamationGeneratedRepository extends JpaRepository<ReclamationGenerated, String> {
    
    List<ReclamationGenerated> findByDatGenXmlBetween(Date startDate, Date endDate);
}
