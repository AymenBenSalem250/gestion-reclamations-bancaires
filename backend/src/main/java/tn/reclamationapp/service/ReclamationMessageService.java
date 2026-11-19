package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.reclamationapp.dto.ReclamationMessageRequest;
import tn.reclamationapp.dto.ReclamationMessageResponse;
import tn.reclamationapp.model.Reclamation;
import tn.reclamationapp.model.ReclamationMessage;
import tn.reclamationapp.model.Users;
import tn.reclamationapp.repository.ReclamationMessageRepository;
import tn.reclamationapp.repository.ReclamationRepository;
import tn.reclamationapp.repository.UsersRepository;

import java.io.IOException;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@Transactional
public class ReclamationMessageService {

    private static final Logger log = LoggerFactory.getLogger(ReclamationMessageService.class);

    @Autowired
    private ReclamationMessageRepository reclamationMessageRepository;

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    public List<ReclamationMessageResponse> getMessages(String refRec) {
        Reclamation reclamation = reclamationRepository.findByRefRec(refRec)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable : " + refRec));

        checkAccess(reclamation);

        List<ReclamationMessage> messages = reclamationMessageRepository.findByReclamationRefRecOrderByCreatedAtAsc(refRec);

        return messages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ReclamationMessageResponse addMessage(
            String refRec,
            String messageText,
            MultipartFile file) {

        Reclamation reclamation = reclamationRepository.findByRefRec(refRec)
                .orElseThrow(() -> new IllegalArgumentException("Réclamation introuvable"));

        Users currentUser = getCurrentUser();
        checkAccess(reclamation, currentUser);

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodrole())
                        || "ADMIN".equals(r.getCodrole()));

        ReclamationMessage message = new ReclamationMessage();
        message.setMessage(messageText);
        message.setReclamation(reclamation);
        message.setUser(currentUser);
        message.setIsAdmin(isAdmin);
        message.setCreatedAt(new Date());

        if (file != null && !file.isEmpty()) {
            try {
                String storedFileName = fileStorageService.storeFile(file);
                message.setFileName(storedFileName);
                message.setFileType(file.getContentType());
                message.setFileSize(file.getSize());
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
            }
        }

        ReclamationMessage saved = reclamationMessageRepository.save(message);

        ReclamationMessageResponse response = mapToResponse(saved);

        // Broadcast real-time message to the specific reclamation chat channel
        messagingTemplate.convertAndSend(
                "/topic/reclamation/" + refRec,
                response
        );

        // Determine recipient and create notification
        if (reclamation.getObjet() != null && reclamation.getObjet().getService() != null) {
            String serviceCode = reclamation.getObjet().getService().getCodSer();

            if (isAdmin) {
                List<Users> users = usersRepository.findByServiceCodSer(serviceCode);
                for (Users user : users) {
                    if (!user.getId().equals(currentUser.getId())) {
                        log.info("Notification envoyée à userId = {}", user.getId());
                        notificationService.createAndSendNotification(
                                user,
                                reclamation,
                                "Nouveau message de l'assistance sur la réclamation " + refRec
                        );
                    }
                }
            } else {
                List<Users> admins = usersRepository.findByRoleCode("ROLE_ADMIN");
                for (Users admin : admins) {
                    if (!admin.getId().equals(currentUser.getId())) {
                        log.info("Notification envoyée à userId = {}", admin.getId());
                        notificationService.createAndSendNotification(
                                admin,
                                reclamation,
                                "Nouveau message du client sur la réclamation " + refRec
                        );
                    }
                }
            }
        }

        return response;
    }

    private void checkAccess(Reclamation reclamation) {
        Users currentUser = getCurrentUser();
        checkAccess(reclamation, currentUser);
    }

    private void checkAccess(Reclamation reclamation, Users user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getCodrole()) || "ADMIN".equals(r.getCodrole()));

        if (!isAdmin) {
            if (user.getService() == null || reclamation.getObjet() == null ||
                    reclamation.getObjet().getService() == null ||
                    !user.getService().getCodSer().equals(reclamation.getObjet().getService().getCodSer())) {
                throw new AccessDeniedException("Accès refusé. Vous n'êtes pas autorisé à accéder à cette réclamation.");
            }
        }
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

    private ReclamationMessageResponse mapToResponse(ReclamationMessage message) {
        return new ReclamationMessageResponse(
                message.getId(),
                message.getMessage(),
                message.getCreatedAt(),
                message.getIsAdmin(),
                message.getUser().getId(),
                message.getUser().getFirstName() + " " + message.getUser().getLastName(),
                message.getFileName(),
                message.getFileType(),
                message.getFileSize()
        );
    }
}
