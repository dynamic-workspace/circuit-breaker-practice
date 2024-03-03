package dev.yoon.resilien4jpractice.ratelimiter

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val logger = LoggerFactory.getLogger(RateLimiterDemoApplication::class.java)

@SpringBootApplication
class RateLimiterDemoApplication

fun main(args: Array<String>) {
    runApplication<RateLimiterDemoApplication>(*args)
}
