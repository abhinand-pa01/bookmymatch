package com.abhinand.bookmymatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ticket_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @Column(nullable = false)
    private String sectionName; // e.g., "Dugout", "Lower Stand", "Upper Stand"
    
    @Column(nullable = false)
    private Integer totalSeats;
    
    @Column(nullable = false)
    private Integer availableSeats;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerTicket;
    
    @Column(length = 500)
    private String description;
    
    // Helper methods
    public boolean isSoldOut() {
        return availableSeats <= 0;
    }
    
    public boolean hasAvailableSeats(int requestedSeats) {
        return availableSeats >= requestedSeats;
    }
    
    public void bookSeats(int numberOfSeats) {
        if (!hasAvailableSeats(numberOfSeats)) {
            throw new IllegalStateException("Not enough seats available");
        }
        this.availableSeats -= numberOfSeats;
    }
    
    public void releaseSeats(int numberOfSeats) {
        this.availableSeats = Math.min(this.availableSeats + numberOfSeats, this.totalSeats);
    }
    
    public int getBookedSeats() {
        return totalSeats - availableSeats;
    }
}
