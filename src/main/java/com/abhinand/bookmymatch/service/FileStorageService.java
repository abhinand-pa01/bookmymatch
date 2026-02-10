package com.abhinand.bookmymatch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    
    private final Path uploadDir;
    
    public FileStorageService() {
        // Store uploads in src/main/resources/static/uploads
        this.uploadDir = Paths.get("src/main/resources/static/uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
            log.info("Upload directory created at: {}", this.uploadDir);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }
    
    public String storeFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }
        
        try {
            // Create folder-specific directory
            Path folderPath = this.uploadDir.resolve(folder);
            Files.createDirectories(folderPath);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Store file
            Path targetLocation = folderPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File stored successfully: {}", filename);
            
            // Return relative URL path
            return "/uploads/" + folder + "/" + filename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
    
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String folder = fileUrl.substring(fileUrl.indexOf("/uploads/") + 9, fileUrl.lastIndexOf("/"));
            
            Path filePath = this.uploadDir.resolve(folder).resolve(filename);
            Files.deleteIfExists(filePath);
            
            log.info("File deleted successfully: {}", filename);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", fileUrl, ex);
        }
    }
}
