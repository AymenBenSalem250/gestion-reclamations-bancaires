package tn.reclamationapp.service;

import tn.reclamationapp.model.Reclamation;

/**
 * Interface Strategy pour l'envoi de notifications.
 *
 * <p>Chaque implémentation représente un canal de notification distinct
 * (Email, SMS, WhatsApp, etc.) et est automatiquement découverte par le
 * {@link NotificationDispatcher} via l'injection Spring d'une liste de beans.</p>
 *
 * <p><b>Pour ajouter un nouveau canal :</b> créer une classe annotée {@code @Service}
 * implémentant cette interface. Aucune autre modification n'est nécessaire.</p>
 */
public interface NotificationSender {

    /**
     * Retourne le type de notification géré par cette implémentation.
     * Doit correspondre (en ignorant la casse) au {@code libelle} de l'entité
     * {@code NotificationType} en base de données.
     *
     * <p>Exemples : {@code "EMAIL"}, {@code "SMS"}, {@code "WHATSAPP"}</p>
     *
     * @return le libellé du canal, en majuscules par convention
     */
    String getType();

    /**
     * Envoie la notification appropriée pour la réclamation donnée.
     *
     * <p>Les implémentations doivent être résilientes : elles ne doivent
     * jamais propager d'exception afin de ne pas bloquer la transaction
     * principale.</p>
     *
     * @param rec la réclamation pour laquelle envoyer la notification
     */
    void send(Reclamation rec);
}
