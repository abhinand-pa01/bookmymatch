package com.abhinand.bookmymatch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@Slf4j
public class ImageServeController {

    private final Path uploadsRoot = Paths.get("src/main/resources/static/uploads").toAbsolutePath().normalize();

    @GetMapping("/uploads/stadiums/{filename:.+}")
    public ResponseEntity<Resource> serveStadiumImage(@PathVariable String filename) {
        log.info("Serving stadium image: {}", filename);
        return serveImageFile("stadiums", filename);
    }

    @GetMapping("/uploads/teams/{filename:.+}")
    public ResponseEntity<Resource> serveTeamImage(@PathVariable String filename) {
        log.info("Serving team image: {}", filename);
        return serveImageFile("teams", filename);
    }

    private ResponseEntity<Resource> serveImageFile(String folder, String filename) {
        try {
            // Construct file path
            Path filePath = uploadsRoot.resolve(folder).resolve(filename).normalize();
            File file = filePath.toFile();

            log.info("Looking for file at: {}", filePath.toString());

            if (!file.exists() || !file.canRead()) {
                log.error("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // Create resource
            Resource resource = new FileSystemResource(file);

            // Determine content type
            String contentType = getContentType(filename);

            log.info("Serving file: {} with type: {}", filename, contentType);

            // Return with proper headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving image: {}/{} - {}", folder, filename, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }
}