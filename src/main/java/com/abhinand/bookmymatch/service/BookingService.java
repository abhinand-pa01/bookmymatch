package com.abhinand.bookmymatch.service;

import com.abhinand.bookmymatch.entity.Booking;
import com.abhinand.bookmymatch.entity.Match;
import com.abhinand.bookmymatch.entity.TicketSection;
import com.abhinand.bookmymatch.entity.User;
import com.abhinand.bookmymatch.repository.BookingRepository;
import com.abhinand.bookmymatch.repository.TicketSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketSectionRepository ticketSectionRepository;
    private final MatchService matchService;
    private final UserService userService;

    @Transactional
    public Booking createBooking(Long matchId, Long ticketSectionId, Integer numberOfTickets) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User must be authenticated to book tickets");
        }

        Match match = matchService.getMatchById(matchId);
        TicketSection ticketSection = ticketSectionRepository.findById(ticketSectionId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket section not found"));

        // Validate booking
        if (!match.isBookable()) {
            throw new IllegalStateException("This match is not available for booking");
        }

        if (ticketSection.isSoldOut()) {
            throw new IllegalStateException("This section is sold out");
        }

        if (!ticketSection.hasAvailableSeats(numberOfTickets)) {
            throw new IllegalStateException(
                    String.format("Only %d seats available in this section", ticketSection.getAvailableSeats()));
        }

        if (numberOfTickets <= 0) {
            throw new IllegalArgumentException("Number of tickets must be greater than 0");
        }

        // Calculate total price
        BigDecimal totalPrice = ticketSection.getPricePerTicket()
                .multiply(BigDecimal.valueOf(numberOfTickets));

        // Update available seats
        ticketSection.bookSeats(numberOfTickets);
        ticketSectionRepository.save(ticketSection);

        // Create booking
        Booking booking = Booking.builder()
                .user(currentUser)
                .match(match)
                .ticketSection(ticketSection)
                .numberOfTickets(numberOfTickets)
                .totalPrice(totalPrice)
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: {} for user: {}", savedBooking.getBookingCode(), currentUser.getUsername());

        return savedBooking;
    }

    public Booking getBookingByCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    public List<Booking> getUserBookings() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User must be authenticated");
        }
        return bookingRepository.findByUserIdOrderByBookedAtDesc(currentUser.getId());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByBookedAtDesc();
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        // seats back to ticket section
        TicketSection ticketSection = booking.getTicketSection();
        ticketSection.releaseSeats(booking.getNumberOfTickets());
        ticketSectionRepository.save(ticketSection);

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking cancelled: {}", booking.getBookingCode());
    }
}
