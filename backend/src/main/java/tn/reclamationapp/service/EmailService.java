package tn.reclamationapp.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.reclamationapp.model.Reclamation;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class EmailService implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // =========================================================
    // Logo TSB officiel intégré en tant que pièce jointe (CID)
    // =========================================================
    @Value("aymen.bensalem2002@gmail.com")
    private String senderEmail;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // =========================================================
    // NotificationSender — implementation interface Strategy
    // =========================================================

    @Override
    public String getType() {
        return "EMAIL";
    }

    @Override
    public void send(Reclamation rec) {
        try {
            boolean sent = sendReclamationDecisionEmail(rec);
            rec.setNotifiedEmail(sent);
        } catch (Exception e) {
            logger.error("[EMAIL] Erreur inattendue lors de l'envoi via NotificationSender : {}", e.getMessage());
            rec.setNotifiedEmail(false);
        }
    }

    public String resolveEmail(Reclamation rec) {
        if ("CLIENT".equalsIgnoreCase(rec.getTypeRec())
                && rec.getClient() != null
                && rec.getClient().getEmailPer() != null
                && !rec.getClient().getEmailPer().isBlank()) {
            return rec.getClient().getEmailPer();
        }
        return rec.getEmailRec();
    }

    // =========================================================
    // Remplacement automatique des variantes de reference
    // =========================================================

    private String replaceRefPlaceholders(String text, String refRec) {
        if (text == null) return "";
        if (refRec == null || refRec.isBlank()) return text;
        text = text.replace("[REF]", refRec);
        Pattern pattern = Pattern.compile(
                "(?i)(r[e\u00e9]clamation\\s+)?r[e\u00e9]f([e\u00e9]rence)?[.:\\s]*",
                Pattern.UNICODE_CASE
        );
        text = pattern.matcher(text).replaceAll("R\u00e9f\u00e9rence\u00a0: " + refRec + " ");
        return text.trim();
    }

    // =========================================================
    // Envoi HTML via MimeMessage
    // =========================================================

    private boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            // Add inline logo (CID)
            org.springframework.core.io.ClassPathResource imageResource = new org.springframework.core.io.ClassPathResource("static/images/tsb-logo.png");
            if (imageResource.exists()) {
                helper.addInline("tsb-logo", imageResource);
            } else {
                logger.warn("[EMAIL] Le logo TSB n'a pas été trouvé dans static/images/tsb-logo.png");
            }
            
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            logger.error("[EMAIL] Erreur lors de l'envoi HTML via SMTP: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================
    // Resolution du nom et civilite client
    // =========================================================

    private String resolveClientName(Reclamation rec) {
        if ("CLIENT".equalsIgnoreCase(rec.getTypeRec()) && rec.getClient() != null) {
            String nom  = rec.getClient().getNomClt()  != null ? rec.getClient().getNomClt()  : "";
            String pren = rec.getClient().getPrenClt() != null ? rec.getClient().getPrenClt() : "";
            String full = (nom + " " + pren).trim();
            return full.isBlank() ? "Client" : full;
        }
        String nom  = rec.getNomRec()  != null ? rec.getNomRec()  : "";
        String pren = rec.getPrenRec() != null ? rec.getPrenRec() : "";
        String full = (nom + " " + pren).trim();
        return full.isBlank() ? "Client" : full;
    }

    private String resolveCivilite(Reclamation rec) {
        String genre = null;
        if ("CLIENT".equalsIgnoreCase(rec.getTypeRec()) && rec.getClient() != null) {
            genre = rec.getClient().getSexClt();
        } else {
            genre = rec.getGenreRec();
        }
        if (genre == null) return "Monsieur\u00a0/ Madame";
        return switch (genre.toUpperCase()) {
            case "F", "FEMME", "MME" -> "Madame";
            case "M", "HOMME", "MR"  -> "Monsieur";
            default -> "Monsieur\u00a0/ Madame";
        };
    }

    private String[] resolveStatusStyle(Reclamation rec) {
        if (rec.getStatRec() == null) return new String[]{"#6c757d", "#ffffff", "Inconnu"};
        String cod = rec.getStatRec().getCodSta() != null ? rec.getStatRec().getCodSta().toUpperCase() : "";
        String lib = rec.getStatRec().getLibSta()  != null ? rec.getStatRec().getLibSta()  : cod;
        return switch (cod) {
            case "EC",   "EN_COURS"   -> new String[]{"#E67E22", "#ffffff", lib.isBlank() ? "En cours"          : lib};
            case "FAV",  "FAVORABLE"  -> new String[]{"#27AE60", "#ffffff", lib.isBlank() ? "Favorable"         : lib};
            case "REJ",  "REJETE"     -> new String[]{"#C0392B", "#ffffff", lib.isBlank() ? "Rejet\u00e9"       : lib};
            case "COMP", "COMPLEMENT" -> new String[]{"#2980B9", "#ffffff", lib.isBlank() ? "Compl\u00e9ment requis" : lib};
            default                   -> new String[]{"#3D5068", "#ffffff", lib.isBlank() ? cod                  : lib};
        };
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // =========================================================
    // Template HTML bancaire professionnel TSB
    // =========================================================

    private String buildHtmlTemplate(Reclamation rec, String message) {
        String clientName    = resolveClientName(rec);
        String civilite      = resolveCivilite(rec);
        String refRec        = rec.getRefRec() != null ? rec.getRefRec() : "\u2014";
        String[] statusStyle = resolveStatusStyle(rec);
        String statusBg      = statusStyle[0];
        String statusFg      = statusStyle[1];
        String statusLabel   = statusStyle[2];
        int    year          = java.time.Year.now().getValue();

        String messageHtml = escapeHtml(message).replace("\n", "<br>");

        return "<!DOCTYPE html>"
            + "<html lang=\"fr\">"
            + "<head>"
            + "<meta charset=\"UTF-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">"
            + "<title>Tunisian Saudi Bank \u2013 Suivi R\u00e9clamation</title>"
            + "</head>"
            + "<body style=\"margin:0;padding:0;background-color:#EEF1F5;font-family:'Segoe UI',Arial,Helvetica,sans-serif;\">"

            // OUTER WRAPPER
            + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background:#EEF1F5;padding:32px 12px;\">"
            + "<tr><td align=\"center\">"

            // EMAIL CARD
            + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"620\" style=\"max-width:620px;width:100%;border-radius:14px;overflow:hidden;box-shadow:0 6px 32px rgba(0,0,0,0.14);\">"

            // HEADER
            + "<tr><td style=\"background:linear-gradient(160deg,#0B2559 0%,#1B4FA8 55%,#1E6FBF 100%);padding:36px 40px 28px;text-align:center;\">"

            // Logo TSB officiel (CID)
            + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background:#ffffff; border-radius:14px 14px 0 0; padding: 20px 0;\"><tr><td align=\"center\">"
            + "<img src=\"cid:tsb-logo\""
            + " alt=\"Tunisian Saudi Bank\" width=\"200\""
            + " style=\"display:block;margin:0 auto;max-width:200px;height:auto;border:0;\">"
            + "</td></tr></table>"
            
            + "<tr><td style=\"background:linear-gradient(160deg,#0B2559 0%,#1B4FA8 55%,#1E6FBF 100%);padding:16px 40px 16px;text-align:center;\">"

            + "<p style=\"margin:0;font-size:11px;color:rgba(255,255,255,0.72);letter-spacing:2.5px;text-transform:uppercase;\">Complaint Management System</p>"
            + "<div style=\"margin:18px auto 0;width:50px;height:2px;background:linear-gradient(90deg,transparent,#FFD700,transparent);\"></div>"
            + "</td></tr>"

            // BODY
            + "<tr><td style=\"background:#ffffff;padding:40px 44px 36px;\">"

            // Salutation
            + "<p style=\"margin:0 0 20px;font-size:16px;color:#1a1a2e;font-weight:500;line-height:1.5;\">"
            + "Bonjour <strong>" + escapeHtml(civilite) + " " + escapeHtml(clientName) + "</strong>,</p>"

            + "<p style=\"margin:0 0 26px;font-size:14px;color:#555;line-height:1.7;\">"
            + "Nous vous informons de la mise \u00e0 jour de votre dossier de r\u00e9clamation aupr\u00e8s de "
            + "la <strong>Tunisian Saudi Bank</strong>. Veuillez trouver ci-dessous le d\u00e9tail de votre situation.</p>"

            // REFERENCE
            + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"margin-bottom:22px;\">"
            + "<tr><td style=\"background:linear-gradient(135deg,#EAF0FB,#F4F7FD);border-left:4px solid #1B4FA8;border-radius:8px;padding:16px 20px;\">"
            + "<p style=\"margin:0 0 5px;font-size:10px;font-weight:700;color:#1B4FA8;text-transform:uppercase;letter-spacing:1.5px;\">R\u00e9f\u00e9rence du dossier</p>"
            + "<p style=\"margin:0;font-size:22px;font-weight:700;color:#0D2B6B;letter-spacing:1px;font-family:'Courier New',monospace;\">" + escapeHtml(refRec) + "</p>"
            + "</td></tr></table>"

            // STATUT
            + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"margin-bottom:26px;\">"
            + "<tr><td>"
            + "<p style=\"margin:0 0 10px;font-size:10px;font-weight:700;color:#888;text-transform:uppercase;letter-spacing:1.5px;\">Statut de la r\u00e9clamation</p>"
            + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\"><tr>"
            + "<td style=\"background:" + statusBg + ";border-radius:24px;padding:9px 22px;\">"
            + "<span style=\"font-size:13px;font-weight:600;color:" + statusFg + ";white-space:nowrap;\">"
            + "&#9679;&nbsp;" + escapeHtml(statusLabel) + "</span>"
            + "</td></tr></table>"
            + "</td></tr></table>"

            // SEPARATEUR
            + "<hr style=\"border:none;border-top:1px solid #E8EDF4;margin:0 0 26px;\">"

            // MESSAGE
            + "<div style=\"background:#F8FAFD;border-radius:8px;padding:22px 24px;border:1px solid #DDE5F0;margin-bottom:28px;\">"
            + "<p style=\"margin:0 0 10px;font-size:10px;font-weight:700;color:#1B4FA8;text-transform:uppercase;letter-spacing:1.5px;\">Message</p>"
            + "<p style=\"margin:0;font-size:14px;color:#2c3e50;line-height:1.85;\">" + messageHtml + "</p>"
            + "</div>"

            // FORMULE
            + "<p style=\"margin:0 0 6px;font-size:14px;color:#444;line-height:1.7;\">"
            + "Nous restons \u00e0 votre disposition pour toute information compl\u00e9mentaire.</p>"
            + "<p style=\"margin:0 0 18px;font-size:14px;color:#444;\">Cordialement,</p>"

            // SIGNATURE
            + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\">"
            + "<tr><td style=\"border-left:3px solid #FFD700;padding-left:14px;\">"
            + "<p style=\"margin:0;font-size:14px;font-weight:700;color:#0D2B6B;\">Service Relation Client</p>"
            + "<p style=\"margin:3px 0 0;font-size:13px;color:#1B4FA8;font-weight:500;\">Tunisian Saudi Bank</p>"
            + "</td></tr></table>"
            + "</td></tr>"

            // FOOTER
            + "<tr><td style=\"background:#0B2559;padding:22px 40px;text-align:center;\">"
            + "<p style=\"margin:0 0 7px;font-size:11px;color:rgba(255,255,255,0.55);line-height:1.6;\">"
            + "Cet email est un message automatique \u2014 Merci de ne pas y r\u00e9pondre.</p>"
            + "<p style=\"margin:0 0 8px;font-size:10px;color:rgba(255,255,255,0.38);line-height:1.6;\">"
            + "Ce message est confidentiel et destin\u00e9 exclusivement au destinataire indiqu\u00e9. "
            + "Si vous n'\u00eates pas le destinataire vis\u00e9, veuillez supprimer ce message imm\u00e9diatement.</p>"
            + "<p style=\"margin:0;font-size:10px;color:rgba(255,255,255,0.45);\">"
            + "&copy; " + year + " Tunisian Saudi Bank \u2014 Tous droits r\u00e9serv\u00e9s</p>"
            + "</td></tr>"

            + "</table>"
            + "</td></tr></table>"
            + "</body></html>";
    }

    // =========================================================
    // Methodes publiques — logique metier INCHANGEE
    // =========================================================

    public boolean sendReclamationCreationEmail(Reclamation rec) {
        String destinataire = resolveEmail(rec);
        if (destinataire == null || destinataire.isBlank()) {
            logger.warn("[EMAIL] Aucun email resolu pour la reclamation {} - envoi ignore", rec.getRefRec());
            return false;
        }
        String subject = "Confirmation de votre reclamation - " + rec.getRefRec();
        String rawBody = (rec.getStatRec() != null && rec.getStatRec().getMsgSta() != null)
                ? rec.getStatRec().getMsgSta().replace("[REF]", rec.getRefRec())
                : "Votre reclamation a bien ete enregistree sous la reference " + rec.getRefRec() + ".";
        String body      = replaceRefPlaceholders(rawBody, rec.getRefRec());
        String htmlEmail = buildHtmlTemplate(rec, body);
        boolean success  = sendHtmlEmail(destinataire, subject, htmlEmail);
        if (success) logger.info("[EMAIL] Email de creation envoye a {} pour {}", destinataire, rec.getRefRec());
        return success;
    }

    public boolean sendReclamationDecisionEmail(Reclamation rec) {
        String destinataire = resolveEmail(rec);
        if (destinataire == null || destinataire.isBlank()) {
            logger.warn("[EMAIL] Aucun email resolu pour la reclamation {} - envoi ignore", rec.getRefRec());
            return false;
        }
        String subject = "Mise a jour de votre reclamation - " + rec.getRefRec();
        String msgSta  = (rec.getStatRec() != null && rec.getStatRec().getMsgSta()  != null) ? rec.getStatRec().getMsgSta()  : "";
        String msg2Sta = (rec.getStatRec() != null && rec.getStatRec().getMsg2Sta() != null) ? rec.getStatRec().getMsg2Sta() : "";
        String body    = (msgSta + "\n\n" + msg2Sta).replace("[REF]", rec.getRefRec());
        if (rec.getCompRec() != null && !rec.getCompRec().isBlank()) {
            body += "\n\nCommentaire de l'agent :\n" + rec.getCompRec();
        }
        body             = replaceRefPlaceholders(body.trim(), rec.getRefRec());
        String htmlEmail = buildHtmlTemplate(rec, body);
        boolean success  = sendHtmlEmail(destinataire, subject, htmlEmail);
        if (success) logger.info("[EMAIL] Email de decision envoye a {} pour {}", destinataire, rec.getRefRec());
        return success;
    }
}
