package com.abhinand.bookmymatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;
    
    @Column(nullable = false)
    private LocalDateTime matchDateTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.UPCOMING;
    
    private String competition; // e.g., "Premier League", "Champions League"
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<TicketSection> ticketSections = new ArrayList<>();
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum MatchStatus {
        UPCOMING,
        LIVE,
        COMPLETED,
        CANCELLED
    }
    
    // Helper method to check if match is bookable
    public boolean isBookable() {
        return status == MatchStatus.UPCOMING && 
               matchDateTime.isAfter(LocalDateTime.now());
    }
}
