# Document 모듈

문서 생명주기 도메인을 정의한다.

## 도메인 구조

```
dev.sayaya.handbook.domain/
├── Document           # 문서 (UUID 식별, 영속화 전 id=null 허용)
└── ValidationTask     # 검증 워크플로우 상태 추적 (NEW → PROCESSING → DONE/FAILED)
```

## 불변 이력 모델

Document는 변경 시 기존 버전을 수정하지 않고 **새 버전을 생성**한다.
각 버전은 `effectDateTime ~ expireDateTime`으로 유효 기간을 관리한다.

- `UUID`로 식별, 영속화 전에는 `id = null`
- 타입 참조는 `type: String`으로 느슨한 결합 유지

## 테스트

```bash
./gradlew :document:test
./gradlew :document:koverVerify  # 커버리지 80% 이상 필수
```
