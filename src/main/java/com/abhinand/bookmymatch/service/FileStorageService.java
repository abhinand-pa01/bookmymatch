package com.abhinand.bookmymatch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final CloudinaryService cloudinaryService;

    private final Path uploadDir = Paths.get("src/main/resources/static/uploads").toAbsolutePath().normalize();

    public String storeFile(MultipartFile file, String folder) {
        try {
            // Clean filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;

            // Try Cloudinary first (for Render deployment)
            if (cloudinaryService.isEnabled()) {
                String cloudinaryUrl = cloudinaryService.uploadImage(file, folder);
                if (cloudinaryUrl != null) {
                    log.info("File stored in Cloudinary: {}", cloudinaryUrl);
                    return cloudinaryUrl; // Return full Cloudinary URL
                }
            }

            // Fallback to local storage (for local development)
            Path targetLocation = uploadDir.resolve(folder).resolve(filename);
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored locally: {}", filename);
            return "/uploads/" + folder + "/" + filename;

        } catch (IOException ex) {
            log.error("Could not store file: {}", ex.getMessage());
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Check if it's a Cloudinary URL
            if (fileUrl.startsWith("http") && fileUrl.contains("cloudinary")) {
                cloudinaryService.deleteImage(fileUrl);
            } else {
                // Local file - delete from filesystem
                String filename = fileUrl.replace("/uploads/", "");
                Path filePath = uploadDir.resolve(filename);
                Files.deleteIfExists(filePath);
                log.info("Local file deleted: {}", filename);
            }
        } catch (IOException ex) {
            log.error("Could not delete file: {}", ex.getMessage());
        }
    }
}
