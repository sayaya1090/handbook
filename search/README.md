# Search 모듈

공유 검색 라이브러리. 페이징, 정렬, 필터링 파라미터를 표준화하여 검색 API에서 공통으로 사용한다.

## 구조

```
├── domain/
│   └── Search.kt          페이징·정렬·필터 파라미터 도메인 객체
└── interfaces/
    └── api/
        └── SearchArgumentResolver.kt   쿼리스트링 → Search 자동 변환 리졸버
```

## 주요 클래스

### Search

| 필드 | 타입 | 설명 |
|------|------|------|
| page | Int | 페이지 번호 (0-based, 필수) |
| limit | Int | 페이지당 항목 수 (필수) |
| sortBy | String? | 정렬 기준 필드명 |
| asc | Boolean? | 오름차순 여부 (sortBy가 있어야 유효) |
| filters | List<Pair<String, Any?>> | 추가 필터 조건 목록 |

### SearchArgumentResolver

Spring WebFlux `HandlerMethodArgumentResolver` 구현체.
컨트롤러 메서드 파라미터에 `Search` 타입을 선언하면, 쿼리스트링(`page`, `limit`, `sort_by`, `asc` + 나머지 필터)을 자동으로 파싱한다.

## 사용 예시

```kotlin
@GetMapping("/workspaces/{id}/types")
fun search(@PathVariable id: UUID, search: Search): Flux<Type> = ...
```

## 의존성

- Spring WebFlux

## 에이전트 연동
**에이전트 연동: 없음 (서버 사이드 라이브러리).**
검색 파라미터 표준화 기능을 제공하며, 에이전트는 본 라이브러리를 사용하는 `document-query` 등의 API를 통해 간접적으로 연동된다.

## 실행

```bash
./gradlew :search:test
```
