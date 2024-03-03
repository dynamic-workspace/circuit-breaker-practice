package dev.yoon.resilien4jpractice.ratelimiter

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RateLimiterController(
    val rateLimiterService: RateLimiterService,
) {
    @GetMapping("/api-call")
    fun apiCall(@RequestParam param: String): String {
        return rateLimiterService.process(param)
    }

    @GetMapping("/change")
    fun changeLimitForPeriod(
        @RequestParam limitForPeriod: Int,
    ) {
        rateLimiterService.changeLimitForPeriod(limitForPeriod)
    }
}
