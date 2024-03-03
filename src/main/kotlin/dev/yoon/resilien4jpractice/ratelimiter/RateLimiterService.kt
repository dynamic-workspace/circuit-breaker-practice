package dev.yoon.resilien4jpractice.ratelimiter

import io.github.resilience4j.ratelimiter.RateLimiter
import org.springframework.stereotype.Service

@Service
class RateLimiterService(
    private val defaultRateLimiter: RateLimiter,
) {
    fun process(param: String): String {
        val available =
            defaultRateLimiter.acquirePermission() // 권한을 얻기 위해서 timeoutDuration 를 기다리고 timeoutDuration를 기다려도 받지 못하면 false를 반환한다.
        return if (available) {
            param
        } else {
            "fail"
        }
    }

    fun changeLimitForPeriod(
        limitForPeriod: Int,
    ) {
        defaultRateLimiter.changeLimitForPeriod(limitForPeriod)
    }
}
