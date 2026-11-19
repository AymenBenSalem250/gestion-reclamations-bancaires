package tn.reclamationapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import tn.reclamationapp.dto.SmsRequest;
import tn.reclamationapp.model.Reclamation;

import java.time.Duration;

@Service
public class SmsService implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final RestTemplate restTemplate;
    private final String smsApiUrl;

    public SmsService(@Value("${sms.api.url}") String smsApiUrl,
                      @Value("${sms.api.timeout:3000}") int timeout) {
        this.smsApiUrl = smsApiUrl;
        
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        
        this.restTemplate = new RestTemplate(requestFactory);
    }

    // =========================================================
    // ✅ NotificationSender — implémentation interface Strategy
    // =========================================================

    @Override
    public String getType() {
        return "SMS";
    }

    /**
     * Point d'entrée unique appelé par le {@link NotificationDispatcher}.
     * Construit le message SMS à partir du statut de la réclamation et l'envoie.
     */
    @Override
    public void send(Reclamation rec) {
        if (rec.getGsmRec() == null) {
            logger.warn("[SMS] Aucun numéro GSM pour la réclamation {} — envoi ignoré", rec.getRefRec());
            return;
        }
        try {
            String message = buildSmsMessage(rec);
            boolean sent = sendSms(String.valueOf(rec.getGsmRec()), message, rec.getRefRec());
            rec.setNotifiedSms(sent);
        } catch (Exception e) {
            logger.error("[SMS] Erreur inattendue lors de l'envoi via NotificationSender : {}", e.getMessage());
            rec.setNotifiedSms(false);
        }
    }

    /**
     * Construit le message SMS à partir du statut et du commentaire.
     * N'inclut jamais d'email, de téléphone ou de données sensibles.
     */
    private String buildSmsMessage(Reclamation rec) {
        if (rec.getStatRec() == null) return "";
        String msg1 = rec.getStatRec().getMsgSta()  != null ? rec.getStatRec().getMsgSta()  : "";
        String msg2 = rec.getStatRec().getMsg2Sta() != null ? rec.getStatRec().getMsg2Sta() : "";
        String message = (msg1 + " " + msg2).replace("[REF]", rec.getRefRec());
        if (rec.getCompRec() != null && !rec.getCompRec().isBlank()) {
            message += " | Commentaire : " + rec.getCompRec();
        }
        return message.trim();
    }

    public boolean sendSms(String numero, String message, String refRec) {
        if (numero == null || numero.isBlank()) {
            logger.warn("[SMS] Aucun numéro de téléphone fourni pour l'envoi du SMS.");
            return false;
        }

        try {
            SmsRequest request = new SmsRequest(message, numero, "GEST_REC", "REC");
            
            // Appel REST vers l'API SMS interne de la banque
            restTemplate.postForObject(smsApiUrl, request, String.class);
            
            logger.info("[SMS] Envoi réussi pour la réclamation {}", refRec);
            return true;
        } catch (Exception e) {
            logger.error("[SMS] Échec envoi SMS pour {} – serveur indisponible", refRec);
            return false;
        }
    }
}
