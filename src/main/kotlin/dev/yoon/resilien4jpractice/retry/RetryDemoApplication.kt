package dev.yoon.resilien4jpractice.retry

import io.github.resilience4j.core.registry.EntryAddedEvent
import io.github.resilience4j.core.registry.EntryRemovedEvent
import io.github.resilience4j.core.registry.EntryReplacedEvent
import io.github.resilience4j.core.registry.RegistryEventConsumer
import io.github.resilience4j.retry.Retry
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

private val logger = LoggerFactory.getLogger(RetryDemoApplication::class.java)

@SpringBootApplication
class RetryDemoApplication {

    @Bean
    fun myRegistryEventConsumer(): RegistryEventConsumer<Retry> {
        return object : RegistryEventConsumer<Retry> {
            override fun onEntryAddedEvent(entryAddedEvent: EntryAddedEvent<Retry>) {
                logger.info("RegistryEventConsumer.onEntryAddedEvent")
                entryAddedEvent.addedEntry.eventPublisher.onEvent { event ->
                    logger.info(event.toString())
                }
            }

            override fun onEntryRemovedEvent(entryRemoveEvent: EntryRemovedEvent<Retry>) {
                logger.info("RegistryEventConsumer.onEntryRemovedEvent")
            }

            override fun onEntryReplacedEvent(entryReplacedEvent: EntryReplacedEvent<Retry>) {
                logger.info("RegistryEventConsumer.onEntryAddedEvent")
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<RetryDemoApplication>(*args)
}
