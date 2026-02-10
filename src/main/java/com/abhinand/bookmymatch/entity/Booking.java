package com.abhinand.bookmymatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, updatable = false)
    private String bookingCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_section_id", nullable = false)
    private TicketSection ticketSection;
    
    @Column(nullable = false)
    private Integer numberOfTickets;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column
    private String paymentIntentId;
    
    @Column
    private String paymentMethod;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime bookedAt;
    
    @Column
    private LocalDateTime paidAt;
    
    @PrePersist
    protected void onCreate() {
        if (bookingCode == null) {
            bookingCode = UUID.randomUUID().toString().toUpperCase();
        }
        bookedAt = LocalDateTime.now();
    }
    
    public enum BookingStatus {
        PENDING_PAYMENT,
        CONFIRMED,
        CANCELLED,
        REFUNDED
    }
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
