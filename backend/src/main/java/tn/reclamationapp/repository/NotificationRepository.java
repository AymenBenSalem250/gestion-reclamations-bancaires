package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.reclamationapp.model.Notification;
import tn.reclamationapp.model.Users;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(Users user);

    long countByUserAndIsReadFalse(Users user);

    List<Notification> findByUserAndIsReadFalse(Users user);
}
