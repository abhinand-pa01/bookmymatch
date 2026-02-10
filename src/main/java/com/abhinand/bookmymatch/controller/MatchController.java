package com.abhinand.bookmymatch.controller;

import com.abhinand.bookmymatch.entity.Match;
import com.abhinand.bookmymatch.entity.TicketSection;
import com.abhinand.bookmymatch.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {
    
    private final MatchService matchService;
    
    @GetMapping
    public String listMatches(Model model) {
        List<Match> matches = matchService.getUpcomingMatches();
        model.addAttribute("matches", matches);
        return "matches/list";
    }
    
    @GetMapping("/{id}")
    public String matchDetails(@PathVariable Long id, Model model) {
        Match match = matchService.getMatchById(id);
        List<TicketSection> ticketSections = matchService.getTicketSectionsByMatch(id);
        
        model.addAttribute("match", match);
        model.addAttribute("ticketSections", ticketSections);
        
        return "matches/details";
    }
}
