package tn.reclamationapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.reclamationapp.dto.ReclamationMessageResponse;
import tn.reclamationapp.service.ReclamationMessageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.util.StringUtils;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reclamations/{refRec}/messages")
public class ReclamationMessageController {

    @Autowired
    private ReclamationMessageService reclamationMessageService;

    @GetMapping
    public ResponseEntity<List<ReclamationMessageResponse>> getMessages(@PathVariable String refRec) {
        List<ReclamationMessageResponse> messages =
                reclamationMessageService.getMessages(refRec);
        return ResponseEntity.ok(messages);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReclamationMessageResponse> addMessage(
            @PathVariable String refRec,
            @RequestParam("message") String message,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide");
        }

        ReclamationMessageResponse response =
                reclamationMessageService.addMessage(refRec, message, file);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {

        String cleanFileName = StringUtils.cleanPath(fileName);
        if (cleanFileName.contains("..")) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        Path filePath = Paths.get("uploads/messages").resolve(cleanFileName).normalize();
        
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}