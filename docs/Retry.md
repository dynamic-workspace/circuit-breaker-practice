# Retry

```kotlin
@Service
class RetryService {

    @Retry(name = SIMPLE_RETRY_CONFIG, fallbackMethod = "fallback")
    fun process(param: String) {
        return callAnotherService(param)
    }

    private fun callAnotherService(param: String) {
        throw RetryException("retry exception")
    }

    private fun fallback(param: String, ex: Exception): String {
        logger.info("fallback! your request is $param")
        return "Recovered: $ex"
    }

    companion object {
        private const val SIMPLE_RETRY_CONFIG = "simpleRetryConfig"
    }
}
```

```yaml
resilience4j.retry:
    configs:
      default:
        max-attempts: 3 # 최대 몇번 시도?
        wait-duration: 1000 # 각 시도 사이에 wait 시간
        retry-exceptions:
          - dev.yoon.resilien4jpractice.exception.RetryException
        ignore-exceptions:
          - dev.yoon.resilien4jpractice.exception.IgnoreException
    instances:
      simpleRetryConfig:
        base-config: default # resilience4j.retry.configs.default:..
```
- 위 설정을 정리해보면 최대 3번 Retry를 시도하고, 각 재시도 사이에 1초를 쉰다.
- RetryException이 발생하면 Retry를 시도하고, IgnoreException이 발생하면 Retry를 시도하지 않는다.

```kotlin
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
```

### Resilience4j의 instance

<img width="679" alt="image" src="https://github.com/dynamic-workspace/playground/assets/83503188/5ba140c0-f6f2-48a8-bba9-cb551f6792aa">

Resilience4j 설정을 instance화하여 사용하는 개념
- 동일한 Resilience4j 설정을 여러 메서드에서 공유할 수 있는 개념처럼 동작한다.
- 왜냐하면 Resilience4j 설정은 해당 인스턴스에 기록이 되고, 만약 여러 메서드에 동일한 Resilience4j 인스턴스를 사용한다면 상태를 공유하게 된다.
- 단, Retry에서는 상태가 별도로 존재하지 않는다. (써킷 브레이커에는 존재)


**실행**

```text
2024-02-25T13:16:15.259+09:00  INFO 6778 --- [nio-8080-exec-1] d.y.r.retry.RetryDemoApplication         : 2024-02-25T13:16:15.257891+09:00[Asia/Seoul]: Retry 'simpleRetryConfig', waiting PT1S until attempt '1'. Last attempt failed with exception 'dev.yoon.resilien4jpractice.exception.RetryException: retry exception'.
2024-02-25T13:16:16.270+09:00  INFO 6778 --- [nio-8080-exec-1] d.y.r.retry.RetryDemoApplication         : 2024-02-25T13:16:16.267654+09:00[Asia/Seoul]: Retry 'simpleRetryConfig', waiting PT1S until attempt '2'. Last attempt failed with exception 'dev.yoon.resilien4jpractice.exception.RetryException: retry exception'.
2024-02-25T13:16:17.279+09:00  INFO 6778 --- [nio-8080-exec-1] d.y.r.retry.RetryDemoApplication         : 2024-02-25T13:16:17.279466+09:00[Asia/Seoul]: Retry 'simpleRetryConfig' recorded a failed retry attempt. Number of retry attempts: '3'. Giving up. Last exception was: 'dev.yoon.resilien4jpractice.exception.RetryException: retry exception'.
2024-02-25T13:16:17.283+09:00  INFO 6778 --- [nio-8080-exec-1] d.y.r.retry.RetryService                 : fallback! your request is abcd
```
- 첫번째 시도에 실패
- 두번째 시도에 실패
- 세번째 시도까지 실패하여 fallbakc을 호출

> 만약 IgnoreException을 throw하는 경우에는 Retry 하지않고 바로 fallback이 호출된다. 

### RegistryEventConsumer

```kotlin
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
```

- onEntryAddedEvent
  - 레지스트리에 Retry 객체가 추가될때마다 수행하는 메서드 
  - publisher를 통해 Event 발생할때마다 Logging
- onEntryRemovedEvent
  - 레지스트리에서 Retry 객체가 삭제될때마다 수행하는 메서드
- onEntryReplacedEvent
  - 레지스트리에서 Retry 객체가 다른 Retry 객체로 교환 될때마다 수행하는 메서드
