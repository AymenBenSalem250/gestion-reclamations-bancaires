package tn.reclamationapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.reclamationapp.dto.AnalyticsDTO;
import tn.reclamationapp.dto.SlaDashboardDTO;
import tn.reclamationapp.service.ReclamationAnalyticsService;
import tn.reclamationapp.service.SlaAnalyticsService;
import tn.reclamationapp.service.XmlROG760Service;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    @Autowired
    private ReclamationAnalyticsService analyticsService;

    @Autowired
    private XmlROG760Service xmlROG760Service;

    /**
     * Service SLA PREMIUM — injecté sans modifier les services existants.
     * Gère la logique de calcul SLA + scoring IA heuristique.
     */
    @Autowired
    private SlaAnalyticsService slaAnalyticsService;

    @GetMapping("/analytics/{year}")
    public ResponseEntity<AnalyticsDTO> getAnalyticsByYear(@PathVariable int year) {
        try {
            AnalyticsDTO dto = analyticsService.generateAnalytics(year);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/xml/{year}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> generateXmlBCT760(@PathVariable int year) {
        try {
            String xmlContent = xmlROG760Service.generateBCT760Xml(year);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BCT_760_" + year + ".xml");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlContent);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("<error>Erreur lors de la génération du fichier XML: " + e.getMessage() + "</error>");
        }
    }

    // =========================================================================
    // MODULE SLA PREMIUM — Endpoint dédié (nouveau, indépendant)
    // =========================================================================

    /**
     * Retourne le dashboard SLA complet pour l'utilisateur authentifié.
     *
     * <h4>Filtrage par rôle (même logique que ReclamationService.getReclamationsPage) :</h4>
     * <ul>
     *   <li>ADMIN → données globales (toutes réclamations)</li>
     *   <li>USER  → données filtrées sur le service de l'utilisateur</li>
     * </ul>
     *
     * <h4>Indicateurs retournés :</h4>
     * <ul>
     *   <li>Volumes : total, enCours, cloturees</li>
     *   <li>SLA     : respectSlaCount, exceededCount, respectPercentage</li>
     *   <li>IA      : risqueMoyenGlobal, performanceParService</li>
     *   <li>Urgences : liste des réclamations RED et ORANGE triées par risque décroissant</li>
     * </ul>
     *
     * @return {@link SlaDashboardDTO} sérialisé en JSON
     */
    @GetMapping("/sla-dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<SlaDashboardDTO> getSlaDashboard() {
        try {
            SlaDashboardDTO dto = slaAnalyticsService.buildDashboard();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
