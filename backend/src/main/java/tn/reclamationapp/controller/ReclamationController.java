package tn.reclamationapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.reclamationapp.dto.ReclamationRequest;
import tn.reclamationapp.dto.ReclamationResponse;
import tn.reclamationapp.dto.ReclamationUpdateRequest;
import tn.reclamationapp.dto.StatusUpdateRequest;
import tn.reclamationapp.dto.MessageResponse;
import tn.reclamationapp.exception.BusinessException;
import tn.reclamationapp.service.ReclamationService;

@RestController
@RequestMapping("/api/reclamations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReclamationController {

    @Autowired
    private ReclamationService reclamationService;

    // ✅ GET BY ID (DTO)
    @GetMapping("/{refRec}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<?> getReclamationById(@PathVariable String refRec) {
        return reclamationService.getReclamationById(refRec)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ CREATE
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<?> createReclamation(
            @RequestBody ReclamationRequest request) {

        try {
            request.validate();
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(reclamationService.createReclamation(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // ✅ UPDATE — Modification (champs autorisés, contrôle service + statut dans la Service Layer)
    @PutMapping("/{refRec}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateReclamation(
            @PathVariable String refRec,
            @RequestBody ReclamationUpdateRequest request) {

        try {
            return ResponseEntity.ok(
                    reclamationService.updateReclamation(refRec, request)
            );
        } catch (BusinessException e) {
            // Règle métier : modification interdite (service différent, statut ≠ EN_COURS)
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // ✅ UPDATE STATUS — Traitement
    // Body JSON : { "codSta": "02", "commentaire": "..." }
    // commentaire obligatoire si codSta == "03"
    @PutMapping("/{refRec}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateStatus(
            @PathVariable String refRec,
            @RequestBody StatusUpdateRequest request) {

        try {
            return ResponseEntity.ok(
                    reclamationService.updateStatus(
                            refRec,
                            request.getCodSta(),
                            request.getCommentaire()
                    )
            );
        } catch (IllegalArgumentException e) {
            // Validation métier : commentaire manquant pour rejet
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // ✅ GET ALL (Pagination)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getAllReclamations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String codSta) {

        return ResponseEntity.ok(
                reclamationService.getReclamationsPage(page, size, search, codSta)
        );
    }
}