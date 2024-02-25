package dev.yoon.resilien4jpractice.retry

import dev.yoon.resilien4jpractice.exception.IgnoreException
import dev.yoon.resilien4jpractice.exception.RetryException
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(RetryService::class.java)

@Service
class RetryService {

    @Retry(name = SIMPLE_RETRY_CONFIG, fallbackMethod = "fallback")
    fun process(param: String): String {
        return callAnotherService(param)
    }

    private fun callAnotherService(param: String): String {
        throw RetryException("retry exception")
//        throw IgnoreException("ignore exception")
    }

    private fun fallback(param: String, ex: Exception): String {
        // Retry에 전부 실패해야 fallback이 실행
        logger.info("fallback! your request is $param")
        return "Recovered: $ex"
    }

    companion object {
        private const val SIMPLE_RETRY_CONFIG = "simpleRetryConfig"
    }
}
