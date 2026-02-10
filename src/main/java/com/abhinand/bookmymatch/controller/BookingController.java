package com.abhinand.bookmymatch.controller;

import com.abhinand.bookmymatch.entity.Booking;
import com.abhinand.bookmymatch.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping("/create")
    public String createBooking(@RequestParam Long matchId,
                               @RequestParam Long ticketSectionId,
                               @RequestParam Integer numberOfTickets,
                               RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.createBooking(matchId, ticketSectionId, numberOfTickets);
            redirectAttributes.addFlashAttribute("message", "Booking created! Please complete payment.");
            // Redirect to payment page instead of ticket
            return "redirect:/payment/" + booking.getId();
        } catch (Exception e) {
            log.error("Booking failed", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/matches/" + matchId;
        }
    }
    
    @GetMapping("/ticket/{bookingCode}")
    public String viewTicket(@PathVariable String bookingCode, Model model) {
        Booking booking = bookingService.getBookingByCode(bookingCode);
        model.addAttribute("booking", booking);
        return "booking/ticket";
    }
    
    @GetMapping("/my-tickets")
    public String myTickets(Model model) {
        List<Booking> bookings = bookingService.getUserBookings();
        model.addAttribute("bookings", bookings);
        return "booking/my-tickets";
    }
    
    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("message", "Booking cancelled successfully");
        } catch (Exception e) {
            log.error("Cancellation failed", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/booking/my-tickets";
    }
}
