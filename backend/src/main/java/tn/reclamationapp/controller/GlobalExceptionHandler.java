package tn.reclamationapp.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.reclamationapp.exception.BusinessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized exception handler for the TSB Complaint Management API.
 *
 * Rules:
 *  - Validation errors   → HTTP 400 with a structured list of {field, message}
 *  - Business logic errors (IllegalArgumentException, RuntimeException) → HTTP 400
 *  - Access denied       → HTTP 403
 *  - Unexpected errors   → HTTP 500 (never for validation failures)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Bean Validation (@Valid on @RequestBody)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles failures of {@code @Valid} / {@code @Validated} on request bodies.
     * Returns HTTP 400 with a list of field-level error objects:
     * <pre>[{"field":"cin","message":"CIN doit contenir exactement 8 chiffres"}, ...]</pre>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> error = new HashMap<>();
            error.put("field", fieldError.getField());
            error.put("message", fieldError.getDefaultMessage());
            errors.add(error);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erreur de validation");
        body.put("errors", errors);

        logger.warn("Validation failed: {} field error(s)", errors.size());
        return ResponseEntity.badRequest().body(body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Constraint Violations (e.g. @RequestParam with @Validated on class)
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            Map<String, String> error = new HashMap<>();
            // Extract just the field name from the full property path
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.contains(".")
                    ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                    : propertyPath;
            error.put("field", field);
            error.put("message", violation.getMessage());
            errors.add(error);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erreur de contrainte");
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Missing required @RequestParam
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Paramètre manquant");
        body.put("message", "Le paramètre '" + ex.getParameterName() + "' est obligatoire");

        return ResponseEntity.badRequest().body(body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Business-rule violations (cross-field checks, entity-not-found, etc.)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Violations de règles métier TSB (modification interdite, accès hors service, etc.).
     * Retourne HTTP 400 avec un message lisible : { "message": "..." }
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());

        logger.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Règle métier non respectée");
        body.put("message", ex.getMessage());

        logger.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erreur de traitement");
        body.put("message", ex.getMessage());

        logger.warn("Runtime error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Access denied (403 — Spring Security)
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Accès refusé");
        body.put("message", "Vous n'avez pas les droits nécessaires pour cette opération");

        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Unexpected errors — only case that returns HTTP 500
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Erreur interne du serveur");
        body.put("message", "Une erreur inattendue s'est produite. Veuillez contacter l'administrateur.");

        logger.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}