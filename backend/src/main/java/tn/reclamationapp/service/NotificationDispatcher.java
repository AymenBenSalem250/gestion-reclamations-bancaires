package tn.reclamationapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.reclamationapp.model.NotificationType;
import tn.reclamationapp.model.Reclamation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatcher central des notifications.
 *
 * <p>Collecte automatiquement tous les beans Spring implémentant
 * {@link NotificationSender} et les indexe par type (clé = libellé en majuscules).
 * Aucune modification du Dispatcher n'est nécessaire pour ajouter un nouveau canal :
 * il suffit de créer un nouveau {@code @Service} implémentant {@link NotificationSender}.</p>
 *
 * <p><b>Cycle de vie</b> : les senders sont injectés une seule fois à la construction,
 * garantissant une Map immuable pendant l'exécution.</p>
 */
@Service
public class NotificationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);

    /** Map indexée par libellé en MAJUSCULES → ex. "EMAIL", "SMS", "WHATSAPP" */
    private final Map<String, NotificationSender> senders = new HashMap<>();

    /**
     * Construit le dispatcher en indexant tous les {@link NotificationSender}
     * découverts par Spring dans le contexte de l'application.
     *
     * @param senderList liste injectée par Spring de tous les beans NotificationSender
     */
    @Autowired
    public NotificationDispatcher(List<NotificationSender> senderList) {
        for (NotificationSender sender : senderList) {
            String key = sender.getType().toUpperCase();
            senders.put(key, sender);
            logger.info("[DISPATCHER] Canal enregistré : {}", key);
        }
    }

    /**
     * Dispatch une notification vers le sender correspondant au type fourni.
     *
     * <p>Règles de gestion :</p>
     * <ul>
     *   <li>Si le type est inactif ({@code active = false}), la notification est silencieusement ignorée.</li>
     *   <li>Si aucun sender ne correspond au libellé, un warning est loggué et rien n'est envoyé.</li>
     *   <li>La transaction principale n'est jamais bloquée — les exceptions sont gérées dans les senders.</li>
     * </ul>
     *
     * @param type le {@link NotificationType} frais (rechargé depuis la base)
     * @param rec  la réclamation pour laquelle envoyer la notification
     */
    public void dispatch(NotificationType type, Reclamation rec) {

        // Garde 1 — type inactif : on passe sans notification
        if (!Boolean.TRUE.equals(type.getActive())) {
            logger.debug("[DISPATCHER] Type {} inactif — notification ignorée pour {}",
                    type.getLibelle(), rec.getRefRec());
            return;
        }

        String key = type.getLibelle().toUpperCase();
        NotificationSender sender = senders.get(key);

        if (sender != null) {
            logger.info("[DISPATCHER] Envoi {} pour la réclamation {}", key, rec.getRefRec());
            sender.send(rec);
        } else {
            // Aucun sender enregistré pour ce type → extensibilité future sans erreur
            logger.warn("[DISPATCHER] Aucun sender enregistré pour le type '{}' — notification ignorée pour {}",
                    key, rec.getRefRec());
        }
    }
}
