package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.startsWith("image/") &&
                        !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Type de fichier non autorisé.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nom de fichier manquant.");
        }

        String cleanFileName = StringUtils.cleanPath(originalFilename);
        if (cleanFileName.contains("..")) {
            throw new IllegalArgumentException("Nom de fichier invalide (Path Traversal attempt).");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String fileName = java.util.UUID.randomUUID() + "_" + cleanFileName;
        Path targetLocation = uploadPath.resolve(fileName);

        // Retrait de REPLACE_EXISTING pour éviter l'écrasement silencieux (rare avec UUID, mais bonne pratique)
        Files.copy(file.getInputStream(), targetLocation);

        return fileName;
    }
}