package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reclamationapp.model.NotificationType;

public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {
}
