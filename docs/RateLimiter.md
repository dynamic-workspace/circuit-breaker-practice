# RateLimiter

RateLimiter는 서비스의 고가용성과 안정성을 확립하기 위해선 반드시 필요한 기술이다.
게다가 제한치를 넘어간 것을 감지했을 때의 동작이나 제한할 요청 타입과 관련된 광범위한 옵션을 함께 제공한다.
간단히 제한치를 넘어선 요청을 거부하거나, 큐를 만들어 나중에 실행할 수도 있고, 어떤 방식으로든 두 정책을 조합해도 된다.

## RateLimiterRegistry

CircuitBreaker와 마찬가지로 RateLimiter 인스턴스를 관리(생성/조회)할 수 있는 인 메모리 RateLimiterRegistry 제공

```kotlin
val rateLimiterRegistry = RateLimiterRegistry.ofDefaults()
```

## RateLimiterConfig

```kotlin
val rateLimiterConfig = RateLimiterConfig.custom()
    .limitRefreshPeriod(Duration.ofMillis(1))
    .limitForPeriod(10)
    .timeoutDuration(Duration.ofMillis(25))
    .build()
```

- timeoutDuration: 스레드가 권한 획득을 기다리는 시간 (default: 5s)
  - 해당 시간이 지나면 
- limitRefreshPeriod: 제한을 갱신할 주기. rate limiter는 지정한 시간이 지날 때마다 권한 수를 limitForPeriod 값으로 되돌린다. (default: 500ns)
- limitForPeriod: 제한을 갱신한 후 재갱신 전까지 사용할 수 있는 권한 수 (default: 50)

limitRefreshPeriod를 1초로 잡고 limitForPeriod를 50으로 설정하면 1초당 50개의 요청을 처리할 수 있음 -> 50tps

