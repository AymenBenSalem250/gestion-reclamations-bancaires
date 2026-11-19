package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reclamationapp.dto.NotificationDto;
import tn.reclamationapp.model.Notification;
import tn.reclamationapp.model.Reclamation;
import tn.reclamationapp.model.Users;
import tn.reclamationapp.repository.NotificationRepository;
import tn.reclamationapp.repository.UsersRepository;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void createAndSendNotification(Users targetUser, Reclamation reclamation, String messageContent) {
        if (targetUser == null || targetUser.getId() == null) {
            log.error("Impossible d'envoyer la notification : targetUser ou targetUser.getId() est null");
            return;
        }

        if (messageContent == null) {
            messageContent = "";
        }

        Notification notification = new Notification(messageContent, targetUser, reclamation);
        Notification savedNotification = notificationRepository.save(notification);
        
        log.info("Notification saved with ID = {}", savedNotification.getId());

        NotificationDto dto = mapToDto(savedNotification);

        // Send via WebSocket
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + targetUser.getId(),
                    dto
            );
            log.info("Notification envoyée via WebSocket au topic: /topic/notifications/{}", targetUser.getId());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification WebSocket pour l'utilisateur {}", targetUser.getId(), e);
        }
    }

    public List<NotificationDto> getMyNotifications() {
        Users currentUser = getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public long getMyUnreadCount() {
        Users currentUser = getCurrentUser();
        return notificationRepository.countByUserAndIsReadFalse(currentUser);
    }

    public void markAsRead(Long notificationId) {
        Users currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        Users currentUser = getCurrentUser();
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(currentUser);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            return usersRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }

    private NotificationDto mapToDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getReclamation() != null ? notification.getReclamation().getRefRec() : null,
                notification.getUser() != null ? notification.getUser().getId() : null
        );
    }
}
