package com.abhinand.bookmymatch.controller;

import com.abhinand.bookmymatch.entity.Match;
import com.abhinand.bookmymatch.entity.Stadium;
import com.abhinand.bookmymatch.entity.Team;
import com.abhinand.bookmymatch.entity.TicketSection;
import com.abhinand.bookmymatch.entity.TicketSection;
import com.abhinand.bookmymatch.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final StadiumService stadiumService;
    private final TeamService teamService;
    private final MatchService matchService;
    private final BookingService bookingService;
    
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalMatches", matchService.getAllMatches().size());
        model.addAttribute("totalBookings", bookingService.getAllBookings().size());
        model.addAttribute("totalStadiums", stadiumService.getAllStadiums().size());
        model.addAttribute("totalTeams", teamService.getAllTeams().size());
        return "admin/dashboard";
    }
    
    // STADIUM MANAGEMENT
    
    @GetMapping("/stadiums")
    public String listStadiums(Model model) {
        model.addAttribute("stadiums", stadiumService.getAllStadiums());
        return "admin/stadiums";
    }
    
    @GetMapping("/stadiums/new")
    public String newStadiumForm() {
        return "admin/new-stadium";
    }
    
    @PostMapping("/stadiums/create")
    public String createStadium(@RequestParam String name,
                                @RequestParam String city,
                                @RequestParam String country,
                                @RequestParam Integer capacity,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) MultipartFile image,
                                RedirectAttributes redirectAttributes) {
        try {
            stadiumService.createStadium(name, city, country, capacity, description, image);
            redirectAttributes.addFlashAttribute("message", "Stadium created successfully");
        } catch (Exception e) {
            log.error("Failed to create stadium", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/stadiums";
    }
    
    @GetMapping("/stadiums/edit/{id}")
    public String editStadiumForm(@PathVariable Long id, Model model) {
        model.addAttribute("stadium", stadiumService.getStadiumById(id));
        return "admin/edit-stadium";
    }
    
    @PostMapping("/stadiums/update/{id}")
    public String updateStadium(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String city,
                                @RequestParam String country,
                                @RequestParam Integer capacity,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) MultipartFile image,
                                RedirectAttributes redirectAttributes) {
        try {
            stadiumService.updateStadium(id, name, city, country, capacity, description, image);
            redirectAttributes.addFlashAttribute("message", "Stadium updated successfully");
        } catch (Exception e) {
            log.error("Failed to update stadium", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/stadiums";
    }
    
    @PostMapping("/stadiums/delete/{id}")
    public String deleteStadium(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            stadiumService.deleteStadium(id);
            redirectAttributes.addFlashAttribute("message", "Stadium deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete stadium", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/stadiums";
    }
    
    //TEAM MANAGEMENT
    
    @GetMapping("/teams")
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.getAllTeams());
        return "admin/teams";
    }
    
    @GetMapping("/teams/new")
    public String newTeamForm() {
        return "admin/new-team";
    }
    
    @PostMapping("/teams/create")
    public String createTeam(@RequestParam String name,
                            @RequestParam String shortName,
                            @RequestParam String city,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) MultipartFile logo,
                            RedirectAttributes redirectAttributes) {
        try {
            teamService.createTeam(name, shortName, city, description, logo);
            redirectAttributes.addFlashAttribute("message", "Team created successfully");
        } catch (Exception e) {
            log.error("Failed to create team", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
    
    @GetMapping("/teams/edit/{id}")
    public String editTeamForm(@PathVariable Long id, Model model) {
        model.addAttribute("team", teamService.getTeamById(id));
        return "admin/edit-team";
    }
    
    @PostMapping("/teams/update/{id}")
    public String updateTeam(@PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam String shortName,
                            @RequestParam String city,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) MultipartFile logo,
                            RedirectAttributes redirectAttributes) {
        try {
            teamService.updateTeam(id, name, shortName, city, description, logo);
            redirectAttributes.addFlashAttribute("message", "Team updated successfully");
        } catch (Exception e) {
            log.error("Failed to update team", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
    
    @PostMapping("/teams/delete/{id}")
    public String deleteTeam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teamService.deleteTeam(id);
            redirectAttributes.addFlashAttribute("message", "Team deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete team", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
    
    //MATCH MANAGEMENT
    
    @GetMapping("/matches")
    public String listMatches(Model model) {
        model.addAttribute("matches", matchService.getAllMatches());
        return "admin/matches";
    }
    
    @GetMapping("/matches/new")
    public String newMatchForm(Model model) {
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("stadiums", stadiumService.getAllStadiums());
        return "admin/new-match";
    }
    
    @PostMapping("/matches/create")
    public String createMatch(@RequestParam Long homeTeamId,
                             @RequestParam Long awayTeamId,
                             @RequestParam Long stadiumId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime matchDateTime,
                             @RequestParam(required = false) String competition,
                             @RequestParam(required = false) String description,
                             RedirectAttributes redirectAttributes) {
        try {
            Match match = matchService.createMatch(homeTeamId, awayTeamId, stadiumId, 
                                                   matchDateTime, competition, description);
            redirectAttributes.addFlashAttribute("message", "Match created successfully");
            return "redirect:/admin/matches/" + match.getId() + "/sections";
        } catch (Exception e) {
            log.error("Failed to create match", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/matches/new";
        }
    }
    
    @GetMapping("/matches/{id}/sections")
    public String manageTicketSections(@PathVariable Long id, Model model) {
        Match match = matchService.getMatchById(id);
        List<TicketSection> sections = matchService.getTicketSectionsByMatch(id);
        
        model.addAttribute("match", match);
        model.addAttribute("sections", sections);
        return "admin/ticket-sections";
    }
    
    @PostMapping("/matches/{matchId}/sections/add")
    public String addTicketSection(@PathVariable Long matchId,
                                  @RequestParam String sectionName,
                                  @RequestParam Integer totalSeats,
                                  @RequestParam BigDecimal pricePerTicket,
                                  @RequestParam(required = false) String description,
                                  RedirectAttributes redirectAttributes) {
        try {
            matchService.addTicketSection(matchId, sectionName, totalSeats, pricePerTicket, description);
            redirectAttributes.addFlashAttribute("message", "Ticket section added successfully");
        } catch (Exception e) {
            log.error("Failed to add ticket section", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/matches/" + matchId + "/sections";
    }
    
    @PostMapping("/matches/delete/{id}")
    public String deleteMatch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            matchService.deleteMatch(id);
            redirectAttributes.addFlashAttribute("message", "Match deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete match", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/matches";
    }
    
    //BOOKING MANAGEMENT
    
    @GetMapping("/bookings")
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin/bookings";
    }
}
