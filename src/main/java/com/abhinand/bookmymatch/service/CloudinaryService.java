package com.abhinand.bookmymatch.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@Service
@Slf4j
public class CloudinaryService{

    private final Cloudinary cloudinary;
    private final boolean enabled;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${cloudinary.enabled:false}") boolean enabled) {

        this.enabled = enabled;

        if (enabled && !cloudName.isEmpty() && !apiKey.isEmpty() && !apiSecret.isEmpty()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            log.info("Cloudinary service initialized successfully");
        } else {
            this.cloudinary = null;
            if (enabled) {
                log.warn("Cloudinary is enabled but configuration is missing");
            } else {
                log.info("Cloudinary is disabled - using local storage");
            }
        }
    }


    public String uploadImage(MultipartFile file, String folder) {
        if (!enabled || cloudinary == null) {
            log.debug("Cloudinary disabled, skipping upload");
            return null;
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "bookmymatch/" + folder,
                            "resource_type", "image",
                            "quality", "auto:good",
                            "fetch_format", "auto"
                    ));

            String url = (String) uploadResult.get("secure_url");
            log.info("Image uploaded to Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Failed to upload to Cloudinary: {}", e.getMessage());
            return null;
        }
    }

    public void deleteImage(String imageUrl) {
        if (!enabled || cloudinary == null || imageUrl == null || !imageUrl.contains("cloudinary")) {
            return;
        }

        try {
            // Extract public ID from URL
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted from Cloudinary: {}", publicId);
            }
        } catch (IOException e) {
            log.error("Failed to delete from Cloudinary: {}", e.getMessage());
        }
    }

    private String extractPublicId(String url) {

        try {
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                // Remove version number if present
                if (path.startsWith("v")) {
                    path = path.substring(path.indexOf('/') + 1);
                }
                // Remove file extension
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex > 0) {
                    path = path.substring(0, dotIndex);
                }
                return path;
            }
        } catch (Exception e) {
            log.error("Failed to extract public ID from URL: {}", url);
        }
        return null;
    }

    public boolean isEnabled() {
        return enabled && cloudinary != null;
    }
}