package com.abhinand.bookmymatch.service;

import com.abhinand.bookmymatch.entity.Team;
import com.abhinand.bookmymatch.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final FileStorageService fileStorageService;
    
    @Transactional
    public Team createTeam(String name, String shortName, String city, 
                           String description, MultipartFile logo) {
        if (teamRepository.existsByName(name)) {
            throw new IllegalArgumentException("Team with this name already exists");
        }
        
        String logoUrl = null;
        if (logo != null && !logo.isEmpty()) {
            logoUrl = fileStorageService.storeFile(logo, "teams");
        }
        
        Team team = Team.builder()
                .name(name)
                .shortName(shortName)
                .city(city)
                .description(description)
                .logoUrl(logoUrl)
                .build();
        
        log.info("Creating new team: {}", name);
        return teamRepository.save(team);
    }
    
    @Transactional
    public Team updateTeam(Long id, String name, String shortName, String city, 
                           String description, MultipartFile logo) {
        Team team = getTeamById(id);
        
        team.setName(name);
        team.setShortName(shortName);
        team.setCity(city);
        team.setDescription(description);
        
        if (logo != null && !logo.isEmpty()) {
            // Delete old logo if exists
            if (team.getLogoUrl() != null) {
                fileStorageService.deleteFile(team.getLogoUrl());
            }
            team.setLogoUrl(fileStorageService.storeFile(logo, "teams"));
        }
        
        log.info("Updating team: {}", name);
        return teamRepository.save(team);
    }
    
    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
    }
    
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
    
    @Transactional
    public void deleteTeam(Long id) {
        Team team = getTeamById(id);
        
        if (team.getLogoUrl() != null) {
            fileStorageService.deleteFile(team.getLogoUrl());
        }
        
        log.info("Deleting team: {}", team.getName());
        teamRepository.deleteById(id);
    }
}
