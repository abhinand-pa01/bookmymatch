package com.abhinand.bookmymatch.service;

import com.abhinand.bookmymatch.entity.Booking;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${stripe.currency}")
    private String currency;

    @Value("${stripe.test.mode:true}")
    private boolean testMode;

    /**Create a Stripe Payment Intent for the booking*/

    public PaymentIntent createPaymentIntent(Booking booking) throws StripeException {

        // Convert amount to smallest currency unit (paise for INR)
        long amountInPaise = booking.getTotalPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        // Stripe metadata MUST be Map<String, String> with NON-NULL values
        Map<String, String> metadata = new HashMap<>();

        if (booking.getId() != null) {
            metadata.put("booking_id", booking.getId().toString());
        }

        if (booking.getBookingCode() != null) {
            metadata.put("booking_code", booking.getBookingCode());
        }

        if (booking.getUser() != null && booking.getUser().getId() != null) {
            metadata.put("user_id", booking.getUser().getId().toString());
        }

        if (booking.getMatch() != null && booking.getMatch().getId() != null) {
            metadata.put("match_id", booking.getMatch().getId().toString());
        }

        metadata.put("test_mode", Boolean.toString(testMode));

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPaise)
                .setCurrency(currency.toLowerCase())
                .setDescription("Match Ticket Booking - " + booking.getBookingCode())
                .putAllMetadata(metadata)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        log.info(
                "PaymentIntent created: {} | booking: {} | amount: {} {}",
                paymentIntent.getId(),
                booking.getBookingCode(),
                booking.getTotalPrice(),
                currency.toUpperCase()
        );

        return paymentIntent;
    }

    /**Retrieve a Payment Intent by ID*/

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    /**Cancel a Payment Intent*/

    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.cancel();
    }

    /**Check if payment is successful*/

    public boolean isPaymentSuccessful(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = retrievePaymentIntent(paymentIntentId);
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            log.error("Error checking payment status for {}", paymentIntentId, e);
            return false;
        }
    }

    /**Get test card information for demo*/

    public Map<String, String> getTestCards() {
        Map<String, String> testCards = new HashMap<>();
        testCards.put("success", "4242 4242 4242 4242");
        testCards.put("declined", "4000 0000 0000 0002");
        testCards.put("insufficient_funds", "4000 0000 0000 9995");
        testCards.put("authentication_required", "4000 0025 0000 3155");
        return testCards;
    }
}
