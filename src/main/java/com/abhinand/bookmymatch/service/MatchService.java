package com.abhinand.bookmymatch.service;

import com.abhinand.bookmymatch.entity.Match;
import com.abhinand.bookmymatch.entity.Stadium;
import com.abhinand.bookmymatch.entity.Team;
import com.abhinand.bookmymatch.entity.TicketSection;
import com.abhinand.bookmymatch.repository.MatchRepository;
import com.abhinand.bookmymatch.repository.StadiumRepository;
import com.abhinand.bookmymatch.repository.TeamRepository;
import com.abhinand.bookmymatch.repository.TicketSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final StadiumRepository stadiumRepository;
    private final TeamRepository teamRepository;
    private final TicketSectionRepository ticketSectionRepository;

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public List<Match> getUpcomingMatches() {
        try {
            // Try the query method first
            List<Match> matches = matchRepository.findUpcomingMatches(LocalDateTime.now());
            log.info("Found {} upcoming matches using query", matches.size());
            return matches;
        } catch (Exception e) {
            // Fallback: get all matches and filter in Java
            log.warn("Query method failed, using fallback: {}", e.getMessage());
            return matchRepository.findAll().stream()
                    .filter(m -> m.getStatus() == Match.MatchStatus.UPCOMING)
                    .filter(m -> m.getMatchDateTime().isAfter(LocalDateTime.now()))
                    .sorted((m1, m2) -> m1.getMatchDateTime().compareTo(m2.getMatchDateTime()))
                    .toList();
        }
    }

    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    @Transactional
    public Match createMatch(Long homeTeamId, Long awayTeamId, Long stadiumId,
                             LocalDateTime matchDateTime, String competition, String description) {

        Team homeTeam = teamRepository.findById(homeTeamId)
                .orElseThrow(() -> new IllegalArgumentException("Home team not found"));
        Team awayTeam = teamRepository.findById(awayTeamId)
                .orElseThrow(() -> new IllegalArgumentException("Away team not found"));
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new IllegalArgumentException("Stadium not found"));

        if (homeTeamId.equals(awayTeamId)) {
            throw new IllegalArgumentException("Home team and away team cannot be the same");
        }

        Match match = Match.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .matchDateTime(matchDateTime)
                .status(Match.MatchStatus.UPCOMING)
                .competition(competition)
                .description(description)
                .build();

        Match savedMatch = matchRepository.save(match);
        log.info("Creating new match: {} vs {} at {}", homeTeam.getName(), awayTeam.getName(), stadium.getName());

        return savedMatch;
    }

    @Transactional
    public TicketSection addTicketSection(Long matchId, String sectionName,
                                          Integer totalSeats, BigDecimal pricePerTicket, String description) {
        Match match = getMatchById(matchId);

        TicketSection section = TicketSection.builder()
                .match(match)
                .sectionName(sectionName)
                .totalSeats(totalSeats)
                .availableSeats(totalSeats)
                .pricePerTicket(pricePerTicket)
                .description(description)
                .build();

        TicketSection savedSection = ticketSectionRepository.save(section);
        log.info("Added ticket section '{}' to match {}", sectionName, matchId);

        return savedSection;
    }

    public List<TicketSection> getTicketSectionsByMatch(Long matchId) {
        return ticketSectionRepository.findByMatchIdOrderByPricePerTicketAsc(matchId);
    }

    @Transactional
    public void deleteMatch(Long id) {
        Match match = getMatchById(id);

        if (!match.getBookings().isEmpty()) {
            throw new IllegalStateException("Cannot delete match with existing bookings");
        }

        log.info("Deleting match: {}", id);
        matchRepository.deleteById(id);
    }

    @Transactional
    public Match updateMatchStatus(Long id, Match.MatchStatus status) {
        Match match = getMatchById(id);
        match.setStatus(status);
        return matchRepository.save(match);
    }
}