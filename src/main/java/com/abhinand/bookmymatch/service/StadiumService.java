package com.abhinand.bookmymatch.service;

import com.abhinand.bookmymatch.entity.Stadium;
import com.abhinand.bookmymatch.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StadiumService {
    
    private final StadiumRepository stadiumRepository;
    private final FileStorageService fileStorageService;
    
    @Transactional
    public Stadium createStadium(String name, String city, String country, Integer capacity, 
                                  String description, MultipartFile image) {
        if (stadiumRepository.existsByName(name)) {
            throw new IllegalArgumentException("Stadium with this name already exists");
        }
        
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.storeFile(image, "stadiums");
        }
        
        Stadium stadium = Stadium.builder()
                .name(name)
                .city(city)
                .country(country)
                .capacity(capacity)
                .description(description)
                .imageUrl(imageUrl)
                .build();
        
        log.info("Creating new stadium: {}", name);
        return stadiumRepository.save(stadium);
    }
    
    @Transactional
    public Stadium updateStadium(Long id, String name, String city, String country, 
                                  Integer capacity, String description, MultipartFile image) {
        Stadium stadium = getStadiumById(id);
        
        stadium.setName(name);
        stadium.setCity(city);
        stadium.setCountry(country);
        stadium.setCapacity(capacity);
        stadium.setDescription(description);
        
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (stadium.getImageUrl() != null) {
                fileStorageService.deleteFile(stadium.getImageUrl());
            }
            stadium.setImageUrl(fileStorageService.storeFile(image, "stadiums"));
        }
        
        log.info("Updating stadium: {}", name);
        return stadiumRepository.save(stadium);
    }
    
    public Stadium getStadiumById(Long id) {
        return stadiumRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stadium not found"));
    }
    
    public List<Stadium> getAllStadiums() {
        return stadiumRepository.findAll();
    }
    
    @Transactional
    public void deleteStadium(Long id) {
        Stadium stadium = getStadiumById(id);
        
        if (!stadium.getMatches().isEmpty()) {
            throw new IllegalStateException("Cannot delete stadium with existing matches");
        }
        
        if (stadium.getImageUrl() != null) {
            fileStorageService.deleteFile(stadium.getImageUrl());
        }
        
        log.info("Deleting stadium: {}", stadium.getName());
        stadiumRepository.deleteById(id);
    }
}
