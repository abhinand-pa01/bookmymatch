package com.abhinand.bookmymatch.repository;

import com.abhinand.bookmymatch.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    
    Optional<Stadium> findByName(String name);
    
    boolean existsByName(String name);
}
