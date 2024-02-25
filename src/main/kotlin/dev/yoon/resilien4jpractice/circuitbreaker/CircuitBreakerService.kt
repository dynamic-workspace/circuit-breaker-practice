package dev.yoon.resilien4jpractice.circuitbreaker

import dev.yoon.resilien4jpractice.exception.IgnoreException
import dev.yoon.resilien4jpractice.exception.RecordException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(CircuitBreakerService::class.java)

@Service
class CircuitBreakerService {

    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONFIG, fallbackMethod = "fallback")
    fun process(param: String): String {
        return callAnotherService(param)
    }

    private fun callAnotherService(param: String): String {
        when (param) {
            "a" -> {
                throw RecordException("record Exception")
            }

            "b" -> {
                throw IgnoreException("record Exception")
            }

            "c" -> { // 3초 이상 걸리는 경우도 실패로 간주
                Thread.sleep(4000)
            }
        }
        return param
    }

//    private fun fallback(param: String, ex: Exception): String {
//        // Retry에 전부 실패해야 fallback이 실행
//        logger.info("fallback! your request is $param")
//        return "Recovered: $ex"
//    }

    private fun fallback(param: String, ex: RecordException): String {
        // Retry에 전부 실패해야 fallback이 실행
        logger.info("RecordException fallback! your request is $param")
        return "Recovered: $ex"
    }

    private fun fallback(param: String, ex: IgnoreException): String {
        // Retry에 전부 실패해야 fallback이 실행
        logger.info("IgnoreException fallback! your request is $param")
        return "Recovered: $ex"
    }

    private fun fallback(param: String, ex: CallNotPermittedException): String {
        // Retry에 전부 실패해야 fallback이 실행
        logger.info("CallNotPermittedException fallback! your request is $param")
        return "Recovered: $ex"
    }

    companion object {
        private const val SIMPLE_CIRCUIT_BREAKER_CONFIG = "simpleCircuitBreakerConfig"
    }
}