package com.abhinand.bookmymatch.repository;

import com.abhinand.bookmymatch.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.status = 'UPCOMING' AND m.matchDateTime > :now ORDER BY m.matchDateTime ASC")
    List<Match> findUpcomingMatches(LocalDateTime now);

    List<Match> findByStadiumIdOrderByMatchDateTimeDesc(Long stadiumId);

    @Query("SELECT m FROM Match m WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId ORDER BY m.matchDateTime DESC")
    List<Match> findByTeamId(Long teamId);
}