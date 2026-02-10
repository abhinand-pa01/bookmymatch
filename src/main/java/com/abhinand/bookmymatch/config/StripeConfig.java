package com.abhinand.bookmymatch.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {
    
    @Value("${stripe.api.key}")
    private String apiKey;
    
    @Value("${stripe.test.mode:true}")
    private boolean testMode;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        
        if (testMode) {
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("   Stripe Payment Gateway Initialized (TEST MODE)");
            log.info("   Mode: DEMO - NO REAL MONEY WILL BE CHARGED");
            log.info("   Currency: INR (Indian Rupees)");
            log.info("   Test Cards Available for Demo");
            log.info("═══════════════════════════════════════════════════════════════");
        }
    }
}
