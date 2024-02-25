# Actuator 함께 사용하기

## Actuator에서 제공해주는 Resilience4j 관련 정보

```properties
management.endpoints.web.exposure.include= health,circuitbreakers,circuitbreakerevents
management.endpoint.health.show-details=always
management.health.diskspace.enabled=false
management.health.circuitbreakers.enabled=true
management.metrics.tags.application=${spring.application.name}
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.distribution.percentiles-histogram.resilience4j.circuitbreaker.calls=true
```

### /actuator/circuitbreakers

등록된 써킷 브레이커를 확인하는 Endpoint

<img width="693" alt="image" src="https://github.com/yoon-youngjin/spring-study/assets/83503188/e91c090b-72e0-49ea-84a8-05dc5d38c386">

### /actuator/circuitbreakers

써킷의 이벤트를 확인할 수 있는 Endpoint

**최초 상태**

<img width="524" alt="image" src="https://github.com/yoon-youngjin/spring-study/assets/83503188/abc11ebb-6da3-433f-8c24-0eaa6a17dc40">

**이벤트가 추가된 상태**

<img width="692" alt="image" src="https://github.com/yoon-youngjin/spring-study/assets/83503188/cba37df3-b5db-4c7c-aaba-7200ba063475">

- EventConsumerBufferSize 만큼만 볼 수 있다.
- `/actuator/circuitbreakerevents/{써킷브레이커명}`: 해당 써킷의 이벤트만 확인할 수 있다.
- `/actuator/circuitbreakerevents/{써킷브레이커명}/{이벤트타입}`: 이벤트 타입만 필터링 할 수 있다.

### /actuator/circuitbreakers/{name}

<img width="680" alt="image" src="https://github.com/yoon-youngjin/spring-study/assets/83503188/4f45b039-cb8e-49d4-8397-ef801bd0f54c">

```text
2024-02-25T22:58:09.569+09:00  INFO 67291 --- [nio-8080-exec-9] d.y.r.c.CircuitBreakerDemoApplication    : 2024-02-25T22:58:09.569634+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' changed state from CLOSED to FORCED_OPEN
2024-02-25T22:58:09.570+09:00  INFO 67291 --- [nio-8080-exec-9] d.y.r.c.CircuitBreakerDemoApplication    : onStateTransition 2024-02-25T22:58:09.569634+09:00[Asia/Seoul]: CircuitBreaker 'simpleCircuitBreakerConfig' changed state from CLOSED to FORCED_OPEN
```
- FORCED_OPEN: 기본적으로는 OPEN과 동일하나 일정 시간이 지나도 HALF_OPEN으로 전이되지 않는 상태 
- DISABLED: 써킷을 사용하지 않는 상태

> Resilience4j 2.0.0 버전부터는 자바 17을 사용하라고 권장. 또한, 1.7.0과 기능 추가는 없음



