package tn.reclamationapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.reclamationapp.model.Client;
import tn.reclamationapp.service.ClientService;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String numCpt,
            @RequestParam(required = false) String codeClt) {

        if (numCpt != null) {
            return clientService.findByNumCompte(numCpt)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        if (codeClt != null) {
            return clientService.findByCodeClt(codeClt)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest().build();
    }
}
