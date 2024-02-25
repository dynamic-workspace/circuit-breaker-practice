# CircuitBreaker

## CircuitBreaker 알아보기

**CircuitBreaker가 없는 상황에서 벌어지는 일**

<img width="863" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/930b8298-f352-4c33-befa-9b50c7950164">

### Circuit은 이걸 어떻게 처리하는가?

<img width="866" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/f36e14d5-f3bb-4a00-a869-1caf266b05f7">

- 써킷 브레이커은 3가지 상태를 가진다.
    - CLOSE: 정상적인 상태 (트래픽이 정상적으로 처리되는 상태)
    - OPEN: 차단된 상태
    - HALF_OPEN: 차단된 상태에서 정상적인 상태로 갈 수 있는지 점검하는 상태

> 통상 써킷 브레이커에 의해 관리되는 위와 같은 상태를 써킷이라고 한다.

### 왜 HALF_OPEN 상태가 필요한가?

**1번과 5번 과정의 변경이 서로 다른 기준에 의해 수행되어야 하기 때문이다**

서비스가 CLOSE 상태에서 OPEN 상태로 변경되는것은 일부 서비스를 포기한다는 의미한다.
안정적인 서비스 운영을 위해 써킷을 도입한 만큼 문제라고 인식되는 경우에만 OPEN으로 변경되어야 한다.

반면, 한번 써킷이 OPEN으로 변경된 다음에 서비스는 정상적인 상태가 되었다면 최대한 빨리 정상 상태(CLOSE)로 돌아가야 한다. (일부 기능을 포기했으므로)

## CircuitBreaker 적용하기

```properties
# CircuitBreaker
resilience4j.circuitbreaker.configs.default.sliding-window-type=COUNT_BASED
resilience4j.circuitbreaker.configs.default.minimum-number-of-calls=7
resilience4j.circuitbreaker.configs.default.sliding-window-size=10
resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.configs.default.failure-rate-threshold=40
resilience4j.circuitbreaker.configs.default.slow-call-duration-threshold=3000
resilience4j.circuitbreaker.configs.default.slow-call-rate-threshold=60
resilience4j.circuitbreaker.configs.default.permitted-number-of-calls-in-half-open-state=5
resilience4j.circuitbreaker.configs.default.automatic-transition-from-open-to-half-open-enabled=true
resilience4j.circuitbreaker.configs.default.event-consumer-buffer-size=10
resilience4j.circuitbreaker.configs.default.record-exceptions=dev.yoon.resilien4jpractice.exception.RecordException
resilience4j.circuitbreaker.configs.default.ignore-exceptions=dev.yoon.resilien4jpractice.exception.IgnoreException
resilience4j.retry.instances.simpleCircuitBreakerConfig.base-config=default
```

- sliding-window-type
  - COUNT_BASED: 실패 횟수 기반
  - TIME_BASED: 실패 시간 기반
- minimum-number-of-calls=7: 최소 7번까지는 무조건 CLOSE로 가정하고 호출
- sliding-window-size=10: minimum-number-of-calls 이후로는 10개의 요청을 기준으로 판단한다.
- wait-duration-in-open-state=10s: OPEN 상태에서 HALF_OPEN으로 가려면 얼마나 기다릴 것인가?
- failure-rate-threshold=40: sliding_window_size 중 몇%가 recordException이면 OPEN으로 만들 것인가?
- slow-call-duration-threshold=3000: 몇 ms 동안 요청이 처리되지 않으면 실패로 간주?
- slow-call-rate-threshold=60: sliding_window_size 중 몇%가 slowCall이면 OPEN으로 만들 것인가?
- permitted-number-of-calls-in-half-open-state=5: HALF_OPEN 상태에서 5번까지는 CLOSE로 가기위해 호출
- automatic-transition-from-open-to-half-open-enabled=true: OPEN 상태에서 자동으로 HALF_OPEN으로 갈 것인가?
- event-consumer-buffer-size=10: actuator를 위한 이벤트 버퍼 사이즈

## CircuitBreaker 동작 테스트 

### CLOSE 상태에서 OPEN 상태로 전환 테스트 

`http://localhost:8080/api-call?param=a`

```text
2024-02-25T14:50:57.965+09:00  INFO 44372 --- [nio-8080-exec-1] d.y.r.c.CircuitBreakerDemoApplication    : RegistryEventConsumer.onEntryAddedEvent
2024-02-25T14:50:57.969+09:00  INFO 44372 --- [nio-8080-exec-1] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:50:57.968801+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:50:57.970+09:00  INFO 44372 --- [nio-8080-exec-1] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:50:58.718+09:00  INFO 44372 --- [nio-8080-exec-3] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:50:58.716661+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:50:58.719+09:00  INFO 44372 --- [nio-8080-exec-3] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:50:59.616+09:00  INFO 44372 --- [nio-8080-exec-4] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:50:59.615749+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:50:59.619+09:00  INFO 44372 --- [nio-8080-exec-4] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:51:00.652+09:00  INFO 44372 --- [nio-8080-exec-5] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:51:00.651862+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:51:00.652+09:00  INFO 44372 --- [nio-8080-exec-5] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:51:01.596+09:00  INFO 44372 --- [nio-8080-exec-6] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:51:01.595875+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:51:01.597+09:00  INFO 44372 --- [nio-8080-exec-6] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:51:02.496+09:00  INFO 44372 --- [nio-8080-exec-7] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:51:02.495604+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:51:02.497+09:00  INFO 44372 --- [nio-8080-exec-7] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:51:04.100+09:00  INFO 44372 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:51:04.100022+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:51:04.102+09:00  INFO 44372 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:51:04.101800+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' exceeded failure rate threshold. Current failure rate: 100.0
2024-02-25T14:51:04.103+09:00  INFO 44372 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerDemoApplication    : FAILURE_RATE_EXCEEDED
2024-02-25T14:51:04.112+09:00  INFO 44372 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:51:04.112609+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' changed state from CLOSED to OPEN
2024-02-25T14:51:04.113+09:00  INFO 44372 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerService            : fallback! your request is a
```

최초(Sliding Window empty)에는 아래처럼 동작 

- sliding-window-size=10, failure-rate-threshold=40: 실패율이 40프로가 넘는 경우에 OPEN으로 변경, 현재 실패율 100%
- minimum-number-of-calls=7: 7번째 실패때 OPEN 상태로 전환

**이후 Sliding Window가 전부 채워진 상태에서는 앞 요청이 모두 성공한 뒤 4개의 요청을 연속으로 실패하면 OPEN 상태로 전환한다.**


### OPEN 상태에서 기능 실행이 차단

```text
2024-02-25T14:52:20.756+09:00  INFO 44895 --- [nio-8080-exec-9] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:52:20.756573+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a call which was not permitted.
2024-02-25T14:52:20.757+09:00  INFO 44895 --- [nio-8080-exec-9] d.y.r.c.CircuitBreakerService            : fallback! your request is a
```

- OPEN 상태에서 재호출 시 기능이 실행되지 않고 fallback이 호출

### OPEN 상태에서 HALF_OPEN 상태로 전환

```text
2024-02-25T14:52:29.732+09:00  INFO 44895 --- [ransitionThread] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:52:29.732244+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' changed state from OPEN to HALF_OPEN
```

- wait-duration-in-open-state=10s: 해당 설정에 의해서 10초 후에 HALF_OPEN 상태로 전환

### HALF_OPEN 상태에서 계속 성공시 CLOSE 상태로 전환

`http://localhost:8080/api-call?param=d`

```text
2024-02-25T14:53:58.089+09:00  INFO 44895 --- [nio-8080-exec-5] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:53:58.088267+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 0 ms
2024-02-25T14:53:58.712+09:00  INFO 44895 --- [nio-8080-exec-4] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:53:58.711895+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 0 ms
2024-02-25T14:53:59.633+09:00  INFO 44895 --- [nio-8080-exec-6] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:53:59.632983+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 0 ms
2024-02-25T14:54:00.347+09:00  INFO 44895 --- [nio-8080-exec-7] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:54:00.347111+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 0 ms
2024-02-25T14:54:00.348+09:00  INFO 44895 --- [nio-8080-exec-7] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:54:00.348271+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' changed state from HALF_OPEN to CLOSED
```
- permitted-number-of-calls-in-half-open-state=5: 해당 설정에 의해 5번 시도 성공 후 CLOSE 상태로 전환

### HALF_OPEN 상태에서 계속 실패시 OPEN 상태로 전환

`http://localhost:8080/api-call?param=a`

```text
2024-02-25T14:56:24.814+09:00  INFO 44895 --- [nio-8080-exec-4] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:56:24.813551+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:56:24.815+09:00  INFO 44895 --- [nio-8080-exec-4] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:56:25.393+09:00  INFO 44895 --- [nio-8080-exec-6] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:56:25.393218+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:56:25.394+09:00  INFO 44895 --- [nio-8080-exec-6] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:56:26.216+09:00  INFO 44895 --- [nio-8080-exec-7] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:56:26.216141+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:56:26.217+09:00  INFO 44895 --- [nio-8080-exec-7] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:56:27.112+09:00  INFO 44895 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:56:27.112206+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:56:27.120+09:00  INFO 44895 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerService            : fallback! your request is a
2024-02-25T14:56:28.199+09:00  INFO 44895 --- [nio-8080-exec-1] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:56:28.198811+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded an error: 'dev.yoon.resilien4jpractice.exception.RecordException: record Exception'. Elapsed time: 0 ms
2024-02-25T14:56:28.203+09:00  INFO 44895 --- [nio-8080-exec-1] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:56:28.203088+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' changed state from HALF_OPEN to OPEN
2024-02-25T14:56:28.204+09:00  INFO 44895 --- [nio-8080-exec-1] d.y.r.c.CircuitBreakerService            : fallback! your request is a
```
- permitted-number-of-calls-in-half-open-state=5: 해당 설정에 의해 5번 시도 실패 후 다시 OPEN으로 전환

### 실행 시간이 오래 걸린 것도 실패로 간주하여 OPEN으로 상태 전환

`http://localhost:8080/api-call?param=c`

```text
2024-02-25T14:57:26.366+09:00  INFO 44895 --- [nio-8080-exec-3] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:57:26.366091+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 4008 ms
2024-02-25T14:57:26.716+09:00  INFO 44895 --- [nio-8080-exec-4] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:57:26.716566+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 4000 ms
2024-02-25T14:57:27.084+09:00  INFO 44895 --- [nio-8080-exec-7] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:57:27.083921+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 4003 ms
2024-02-25T14:57:27.446+09:00  INFO 44895 --- [io-8080-exec-10] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:57:27.445887+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 4000 ms
2024-02-25T14:57:28.468+09:00  INFO 44895 --- [nio-8080-exec-9] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:57:28.468099+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' recorded a successful call. Elapsed time: 4005 ms
2024-02-25T14:57:28.469+09:00  INFO 44895 --- [nio-8080-exec-9] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:57:28.469087+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' exceeded slow call rate threshold. Current slow call rate: 71.42857
2024-02-25T14:57:28.469+09:00  INFO 44895 --- [nio-8080-exec-9] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T14:57:28.469273+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' changed state from CLOSED to OPEN
```

- slow-call-duration-threshold=3000: 해당 설정에 의해 3s가 넘게 걸리는 호출이 7번 요청되었을 때 OPEN 상태로 전환

## CircuitBreaker 주요 설정 알아보기

### CircuitBreaker 상태 변환 관련 주요 설정

<img width="860" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/b953a882-6f02-4b21-b321-ab390391c774">

**slidingWindowType**

- COUNT_BASED: 횟수를 기준으로 Sliding Window 지정
- TIME_BASED: 시간를 기준으로 Sliding Window 지정

**slidingWindowSize**

- 써킷 브레이커의 동작을 결정할때 바라볼 대상을 전체가 아닌 지정한 사이즈만큼만 제한 (현재 데이터를 기준으로
- TIME_BASED로 지정한 경우에는 windowSize를 몇 초로 지정할지가 된다.
  - 만약 100초로 지정한다면 100초 사이에 요청이 몇개가 오든 몇개가 실패했는지에 따라서 서킷의 상태가 결정된다.

**failureRateThreshold**

- slidingWindow의 몇%를 실패해야하지 서킷을 열리도록 만들지 결정하는 설정
- 만약 slidingWindowSize=10, failureRateThreshold=70 이라면 10개중 7개가 실패한 경우 상태를 OPEN으로 변경한다.

**minimumNumberOfCalls**

<img width="846" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/2a5f8495-b89c-4714-9036-cd5e0126d2db">

- 위와 같은 경우에 이미 failureRateThreshold를 넘었으므로 서킷 상태를 변경할 수 있다. 하지만 아직 windowSize를 가득차지 않았기 때문에 OPEN 상태로 변경하고 싶지 않을 수 있는데 이때 사용할 옵션이 minimumNumberOfCalls이다.
- minimumNumberOfCalls로 지정한 값을 넘어간 요청부터 실패율을 계산해서 상태 변경을 결정한다.
- 주의할 점은 Sliding Window보다 큰 값으로 지정하면 의미가 없다. (동일한 값이랑 다른게 없음)

**waitDurationInOpenState**

- 서킷이 OPEN이 된 다음 OPEN으로 얼마나 머무를지 결정하는 설정

**permittedNumberOfCallsInHalfOpenState**

- HALF_OPEN 상태에서 CLOSE 상태로 갈 수 있는지 몇번까지 확인하는 요청을 해볼 것인지 횟수를 지정하는 설정 
- 횟수만큼 시도후에 실패율이 임계치보다 높은 경우에는 OPEN 상태로 전이, 낮은 경우에는 CLOSE 상태로 전이

**automaticTransitionFromOpenToHalfOpenEnabled**

- OPEN 상태에서 waitDurationOpenState 만큼 대기후 자동으로 상태를 HALF_OPEN으로 전환할지 결정하는 설정
- 만약 false라면 만약 요청이 한번 들어올때 waitDurationInOpenState만큼 지났다면 그때 HALF_OPEN으로 변경한다. 
  - false로 지정하면 쓰레드가 모든 써킷 브레이커의 상태를 모니터링하지 않아도 되기에 성능상 이점이 있을 수도 미비할 수도 있다.

### CircuitBreaker 이벤트 활용하기

```kotlin
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
        logger.info("${event.stateTransition}")
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
```

<img width="854" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/dfb9cbe3-c0ff-4c49-b2bc-410fcb3f29a1">

- CLOSE, HALF_OPEN 상태에서는 Success, Error(RecordException), IgnoreError에 대해서 Event가 호출
- CLOSE -> OPEN, OPEN -> HALF_OPEN, HALF_OPEN -> OPEN or CLOSE 변경될 때에는 StateTransition Event가 발생
- CLOSE, HALF_OPEN 상태에서 RecordException이 임계치에 도달하거나 SlowCall이 임계치에 도달한 경우에는 SlowCallRateExceeded, FailureRateExceeded Evnet가 호출
- OPEN 상태에서 추가적인 요청이 들어와서 실제 로직이 실행되지 않은 경우에 CallNotPermitted Event 발생

**이러한 이벤트 어떻게 활용할 것인가?**

1. 이벤트 Logging -> 서비스 상태 확인, 모니터링
2. StateTransition에서 발생하는 이벤트를 다른 서버에 띄워진 애플리케이션에도 전파

<img width="895" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/8efd4930-259e-43b3-9a03-83b9ef27df28">

- 하나의 애플리케이션에서 StateTransition의 Event가 CLOSE -> OPEN으로 변경되었다면 이벤트 핸들러를 통해 다른 웹 애플리케이션도 해당 상태를 전파받을 수 있도록 구성할 수 있다.

<img width="904" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/03637eec-af1c-4c70-93f1-540ed44c3927">

- 반대로 HALF_OPEN에서 CLOSE로 변경되는 이벤트는 가급적 상태를 전파하지 않는게 더 좋다.
  - HALF_OPEN에 존재하던 써킷들이 강제로 CLOSE로 변경된다면 트래픽이 급격하게 올라갈 수 있다. 
  - 자동으로 변경될 수 있도록 나두는게 나을 수 있다.

### 어떤 예외를 RecordException으로 지정할까?

RecordException은 실패로 간주하여 시스템을 회복시키기위해 트래픽을 차단할 필요가 있는 상황에 던지지는 예외를 지정해야한다.
- RecordException을 잘못 지정한다면 의도하지 않은 상황에서도 서킷이 열린다던지 서킷이 꼭 열려야 하는 상황에서도 서킷이 열리지 않는 문제를 야기할 수 있다.

1. 유효성 검사나 NPE처럼 서킷이 열리는 것과 무관한 예외는 RecordException으로 등록하면 안된다.
2. Exception이나 RuntimeException처럼 너무 높은 수준의 예외 역시 RecordException으로 등록하면 안된다.

**라이브러리가 예외를 던져주지 않는다면?**

- 해당 예외를 잡아서 지정한 RecordException으로 매핑한 뒤 던져주자.

**SlowCall에만 의존한지 말기**

- SlowCall에만 의존하기 보다는 적절한 타임아웃을 지정해주는게 좋다.
- 부하를 너무 많이 받은 상황에서는 짧은 시간으로 타임아웃을 걸어주고 요청을 빠르게 끝내고 RecordException 카운트를 올려서 써킷을 OPEN 하는게 적절한 상황이 있다.

### fallback 활용하기

**fallback 메서드가 실행되는 경우**

1. RecordException이 발생한 경우
2. IgnoreException이 발생한 경우
3. 서킷이 OPEN되어 요청이 실행되지 않은 경우

```kotlin
private fun fallback(param: String, ex: Exception): String {
    // Retry에 전부 실패해야 fallback이 실행
    logger.info("fallback! your request is $param")
    return "Recovered: $ex"
}
```

**위 fallback 메서드는 모든 예외를 받아서 처리하고 있는데, 이를 특정 예외만 처리하도록 변경할 순 없을까?**

```kotlin
private fun fallback(param: String, ex: RecordException): String {
    // Retry에 전부 실패해야 fallback이 실행
    logger.info("fallback! your request is $param")
    return "Recovered: $ex"
}

private fun fallback(param: String, ex: IgnoreException): String {
    // Retry에 전부 실패해야 fallback이 실행
    logger.info("fallback! your request is $param")
    return "Recovered: $ex"
}

private fun fallback(param: String, ex: CallNotPermittedException): String {
    // Retry에 전부 실패해야 fallback이 실행
    logger.info("fallback! your request is $param")
    return "Recovered: $ex"
}
```

> 여기서 주의할 점은 CallNotPermittedException이 오버로딩된 fallback 메서드를 만들지 않은 경우에 서킷이 OPEN된 이후 실패 요청이 다시 들어오면 클라이언트에게 500에러가 전파된다.

**fallback으로 할 수 있는 일들**

- 실행한 메서드의 파라미터
- 추가적인 로직 실행 (recovery)
- 리턴값 (클라이언트에게 반환)
  - ex. 리뷰저장소에 문제가 발생한다면 단순 emptyList()로 반환하도록 처리
  - ex. 파라미터로 들어온 원래 데이터를 활용해서 실제 리뷰 저장소가 아닌 캐시되어 있던 리뷰 데이터를 조회하고 조회된 데이터를 반환

<img width="766" alt="image" src="https://github.com/dynamic-workspace/resilience4j-practice/assets/83503188/0b3b55c3-74b9-4896-a192-ad20fb98d584">
