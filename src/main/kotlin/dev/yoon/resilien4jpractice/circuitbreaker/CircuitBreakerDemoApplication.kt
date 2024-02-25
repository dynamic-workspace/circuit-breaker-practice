package dev.yoon.resilien4jpractice.circuitbreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.core.registry.EntryAddedEvent
import io.github.resilience4j.core.registry.EntryRemovedEvent
import io.github.resilience4j.core.registry.EntryReplacedEvent
import io.github.resilience4j.core.registry.RegistryEventConsumer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

private val logger = LoggerFactory.getLogger(CircuitBreakerDemoApplication::class.java)

@SpringBootApplication
class CircuitBreakerDemoApplication {

    @Bean
    fun myRegistryEventConsumer(): RegistryEventConsumer<CircuitBreaker> {
        return object : RegistryEventConsumer<CircuitBreaker> {
            override fun onEntryAddedEvent(entryAddedEvent: EntryAddedEvent<CircuitBreaker>) {
                logger.info("RegistryEventConsumer.onEntryAddedEvent")

                val eventPublisher = entryAddedEvent.addedEntry.eventPublisher

                eventPublisher.onEvent { event -> // 발생하는 모든 이벤트를 처리
                    logger.info(event.toString())
                }
                eventPublisher.onSuccess { event -> // 성공 시
                    logger.info("onSunccess $event")
                }
                eventPublisher.onCallNotPermitted { event -> // 서킷이 OPEN이 되어서 차단된 요청이 발생한 경우
                    logger.info("onCallNotPermitted $event")
                }
                eventPublisher.onError { event -> // RecordException이 발생한 경우
                    logger.info("onError $event")
                }
                eventPublisher.onIgnoredError { event -> // IgnoreException이 발생한 경우
                    logger.info("onIgnoredError $event")
                }
                eventPublisher.onStateTransition { event -> // 서킷의 상태가 변경된 경우
                    logger.info("${event.stateTransition.toState}")
                    logger.info("${event.stateTransition.fromState}")
                    logger.info("onStateTransition $event")
                }
                eventPublisher.onSlowCallRateExceeded { event -> // slowCall이 임계치에 도달한 경우
                    logger.info("onSlowCallRateExceeded $event")
                }
                eventPublisher.onFailureRateExceeded { event -> // RecordException이 임계치에 도달한 경우
                    logger.info("onFailureRateExceeded $event")
                }
            }

            override fun onEntryRemovedEvent(entryRemoveEvent: EntryRemovedEvent<CircuitBreaker>) {
                logger.info("RegistryEventConsumer.onEntryRemovedEvent")
            }

            override fun onEntryReplacedEvent(entryReplacedEvent: EntryReplacedEvent<CircuitBreaker>) {
                logger.info("RegistryEventConsumer.onEntryAddedEvent")
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CircuitBreakerDemoApplication>(*args)
}
