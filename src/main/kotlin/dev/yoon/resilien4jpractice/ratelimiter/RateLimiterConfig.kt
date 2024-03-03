package dev.yoon.resilien4jpractice.ratelimiter

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = LoggerFactory.getLogger(RateLimiterConfig::class.java)

@Configuration
class RateLimiterConfig {
    @Bean
    fun defaultRateLimiter(): RateLimiter {
        val config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(1))
            .limitForPeriod(3)
            .build()

        return RateLimiterRegistry.of(config).rateLimiter("default")
            .apply {
                eventPublisher
                    .onSuccess { event ->
                        logger.info("onSunccess $event")
                    }
                    .onFailure { event ->
                        logger.info("onFailure $event")
                    }
            }
    }
}
