package dev.yoon.resilien4jpractice.retry

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RetryController(
    val retryService: RetryService,
) {
    @GetMapping("/api-call")
    fun apiCall(@RequestParam param: String): String {
        return retryService.process(param)
    }
}
