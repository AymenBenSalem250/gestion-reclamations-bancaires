package tn.reclamationapp.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.reclamationapp.dto.MessageResponse;
import tn.reclamationapp.model.*;
import tn.reclamationapp.service.ReferenceDataService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reference")
public class ReferenceDataController {
    
    @Autowired
    private ReferenceDataService referenceDataService;
    
    // Statut endpoints
    @GetMapping("/statuts")
    public ResponseEntity<Page<Statut>> getAllStatuts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(referenceDataService.getStatutsPage(page, size));
    }
    
    @GetMapping("/statuts/{codSta}")
    public ResponseEntity<?> getStatutByCode(@PathVariable String codSta) {
        return referenceDataService.getStatutByCode(codSta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/statuts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createStatut(@RequestBody Statut statut) {
        Statut created = referenceDataService.createStatut(statut);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/statuts/{codSta}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatut(@PathVariable String codSta, @RequestBody Statut statut) {
        try {
            Statut updated = referenceDataService.updateStatut(codSta, statut);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/statuts/{codSta}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStatut(@PathVariable String codSta) {
        try {
            referenceDataService.deleteStatut(codSta);
            return ResponseEntity.ok(new MessageResponse("Statut deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Service endpoints
    @GetMapping("/services")
    public ResponseEntity<Page<Service>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(referenceDataService.getServicesPage(page, size));
    }
    
    @GetMapping("/services/{codSer}")
    public ResponseEntity<?> getServiceByCode(@PathVariable String codSer) {
        return referenceDataService.getServiceByCode(codSer)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/services")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createService(@RequestBody Service service) {
        Service created = referenceDataService.createService(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/services/{codSer}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateService(@PathVariable String codSer, @RequestBody Service service) {
        try {
            Service updated = referenceDataService.updateService(codSer, service);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/services/{codSer}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteService(@PathVariable String codSer) {
        try {
            referenceDataService.deleteService(codSer);
            return ResponseEntity.ok(new MessageResponse("Service deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Objet endpoints
    @GetMapping("/objets")
    public ResponseEntity<Page<Objet>> getAllObjets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(referenceDataService.getObjetsPage(page, size));
    }
    
    @GetMapping("/objets/{codObj}")
    public ResponseEntity<?> getObjetByCode(@PathVariable String codObj) {
        return referenceDataService.getObjetByCode(codObj)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/objets/service/{codSer}")
    public ResponseEntity<List<Objet>> getObjetsByService(@PathVariable String codSer) {
        return ResponseEntity.ok(referenceDataService.getObjetsByService(codSer));
    }
    
    @PostMapping("/objets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createObjet(@RequestBody Objet objet) {
        Objet created = referenceDataService.createObjet(objet);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/objets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateObjet(@PathVariable Long id, @RequestBody Objet objet) {
        try {
            Objet updated = referenceDataService.updateObjet(id, objet);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/objets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteObjet(@PathVariable Long id) {
        try {
            referenceDataService.deleteObjet(id);
            return ResponseEntity.ok(new MessageResponse("Objet deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Canal endpoints
    @GetMapping("/canals")
    public ResponseEntity<Page<Canal>> getAllCanals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(referenceDataService.getCanalsPage(page, size));
    }
    
    @GetMapping("/canals/{id}")
    public ResponseEntity<?> getCanalById(@PathVariable Long id) {
        return referenceDataService.getCanalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/canals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCanal(@RequestBody Canal canal) {
        Canal created = referenceDataService.createCanal(canal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/canals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCanal(@PathVariable Long id, @RequestBody Canal canal) {
        try {
            Canal updated = referenceDataService.updateCanal(id, canal);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/canals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCanal(@PathVariable Long id) {
        try {
            referenceDataService.deleteCanal(id);
            return ResponseEntity.ok(new MessageResponse("Canal deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // NatureReclamant endpoints
    @GetMapping("/nature-reclamants")
    public ResponseEntity<Page<NatureReclamant>> getAllNatureReclamants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(referenceDataService.getNatureRelamantsPage(page, size));
    }
    
    @GetMapping("/nature-reclamants/{id}")
    public ResponseEntity<?> getNatureReclamantById(@PathVariable Long id) {
        return referenceDataService.getNatureReclamantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/nature-reclamants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNatureReclamant(@RequestBody NatureReclamant nature) {
        NatureReclamant created = referenceDataService.createNatureReclamant(nature);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/nature-reclamants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateNatureReclamant(@PathVariable Long id, @RequestBody NatureReclamant nature) {
        try {
            NatureReclamant updated = referenceDataService.updateNatureReclamant(id, nature);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/nature-reclamants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNatureReclamant(@PathVariable Long id) {
        try {
            referenceDataService.deleteNatureReclamant(id);
            return ResponseEntity.ok(new MessageResponse("NatureReclamant deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ParamGen endpoints
    @GetMapping("/params")
    public ResponseEntity<List<ParamGen>> getAllParams() {
        return ResponseEntity.ok(referenceDataService.getAllParams());
    }
    
    @GetMapping("/params/name")
    public ResponseEntity<?> getParamByName(@RequestParam String libParam) {
        return referenceDataService.getParamByName(libParam)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/params")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createParam(@RequestBody ParamGen param) {
        ParamGen created = referenceDataService.createParam(param);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/params/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateParam(@PathVariable Long id, @RequestBody ParamGen param) {
        try {
            ParamGen updated = referenceDataService.updateParam(id, param);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/params/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteParam(@PathVariable Long id) {
        try {
            referenceDataService.deleteParam(id);
            return ResponseEntity.ok(new MessageResponse("Parameter deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // NotificationType endpoints
    @GetMapping("/notification-types")
    public ResponseEntity<Page<NotificationType>> getAllNotificationTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(referenceDataService.getNotificationTypesPage(page, size));
    }
    
    @GetMapping("/notification-types/{id}")
    public ResponseEntity<?> getNotificationTypeById(@PathVariable Long id) {
        return referenceDataService.getNotificationTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/notification-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNotificationType(@Valid @RequestBody NotificationType type) {
        NotificationType created = referenceDataService.createNotificationType(type);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/notification-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateNotificationType(@PathVariable Long id, @RequestBody NotificationType type) {
        try {
            NotificationType updated = referenceDataService.updateNotificationType(id, type);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/notification-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNotificationType(@PathVariable Long id) {
        try {
            referenceDataService.deleteNotificationType(id);
            return ResponseEntity.ok(new MessageResponse("NotificationType deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
