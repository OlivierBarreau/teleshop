package eu.telecom.teleshop.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuration for payment retry logic
 * Implements exponential backoff with configurable max retry attempts
 */
@Configuration
@EnableRetry
public class RetryConfig {
    
    /**
     * Retry template for payment processing with exponential backoff
     * 
     * Configuration:
     * - Maximum retry attempts: 3
     * - Initial interval: 1000ms (1 second)
     * - Multiplier: 2.0 (doubles each retry)
     * - Max interval: 10000ms (10 seconds)
     * 
     * Retry sequence: 1s -> 2s -> 4s
     */
    @Bean
    public RetryTemplate paymentRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configure retry policy (max 3 attempts)
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Configure exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 second
        backOffPolicy.setMultiplier(2.0); // Double each time
        backOffPolicy.setMaxInterval(10000); // Max 10 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
}
