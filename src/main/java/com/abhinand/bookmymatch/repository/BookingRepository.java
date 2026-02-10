package com.abhinand.bookmymatch.repository;

import com.abhinand.bookmymatch.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingCode(String bookingCode);
    
    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);
    
    List<Booking> findByMatchIdOrderByBookedAtDesc(Long matchId);
    
    List<Booking> findAllByOrderByBookedAtDesc();
}
