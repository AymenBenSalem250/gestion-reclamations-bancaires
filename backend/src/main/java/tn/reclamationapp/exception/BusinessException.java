package tn.reclamationapp.exception;

/**
 * Exception sémantique pour les violations de règles métier TSB.
 *
 * Utilisée pour signaler les cas où une opération est techniquement valide
 * mais interdite par la logique bancaire (ex: modification d'une réclamation
 * déjà traitée, accès à une réclamation hors service, etc.).
 *
 * Capturée par {@link tn.reclamationapp.controller.GlobalExceptionHandler}
 * et retournée au client avec HTTP 400 et un message lisible.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
