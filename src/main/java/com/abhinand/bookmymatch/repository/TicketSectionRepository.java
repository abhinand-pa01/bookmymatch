package com.abhinand.bookmymatch.repository;

import com.abhinand.bookmymatch.entity.TicketSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketSectionRepository extends JpaRepository<TicketSection, Long> {
    
    List<TicketSection> findByMatchId(Long matchId);
    
    List<TicketSection> findByMatchIdOrderByPricePerTicketAsc(Long matchId);
}
