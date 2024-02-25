# Resilience4j소개

**Resilience4j 핵심적인 기능들**

- CircuitBreaker
- Bulkhead
- RateLimiter
- Retry
- TimeLimiter
- Cache

**Circuit Breaker**

서버가 너무 많은 트래픽을 받아서 회복을 위해 잠시 트래픽을 차단시켜야 하는 경우에 활용해볼 수 있다. (Retry로직은 상대 서버에 더 많은 부하를 야기할 수 있음)
- 원래 기능을 대체할 수 있는 fallback을 수행할 수 있음

<img width="656" alt="image" src="https://github.com/dynamic-workspace/circuit-breaker-practice/assets/83503188/40f485eb-2c1c-4b97-a68d-6f1ac6b7a00e">

## Resilience4j를 활용하기 적절한 상황들

### Retry

작업이 일시적으로 실패했지만 금방 다시 복구될 상황에서 적용하기 적절하다.

**고려할 요소**

- 몇번까지 재시도?
- 재시도 간격은 얼마나 길게?
  - 간격을 너무 짧게 잡으면 재시도 호출이 의미가 없어질 수 있고, 상대 서버에 부하를 주는 행동이 될 수도 있다.
- 어떤 상황을 호출 실패로 간주?
  - 일반적으로 NetworkException의 경우 재시도를 고려해볼 수 있지만 NullPointerException에 재시도를 호출하면 안 된다.

### Circuit Breaker

<img width="688" alt="image" src="https://github.com/dynamic-workspace/circuit-breaker-practice/assets/83503188/56ce8029-008c-4742-b378-fe09bd3071f7">

리뷰 저장소에 문제가 생겨서 조회가 실패하거나 조회 속도가 느려지는 상황이 발생

- 이러한 상황에 별다른 방어로직이 없다면 상품 저장소에서 상품은 정상으로 불러오더라도 전체 응답은 에러 응답으로 전달
- 이러한 상황에 Circuit을 추가한다면 사용자에게 리뷰만 제외하고 상품은 정상적으로 전달할 수 있게 된다. (리뷰는 부가적인 내용이기 때문에 필수 내용은 아님)

<img width="743" alt="image" src="https://github.com/dynamic-workspace/circuit-breaker-practice/assets/83503188/450f529d-fe39-4c13-92c0-cd3ce829dff6">

---

https://d2.naver.com/helloworld/6070967