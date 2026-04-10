# Schema 모듈

타입 시스템 도메인을 정의한다. 문서의 구조(스키마)를 버전 관리하고, 속성별 데이터 타입과 검증 규칙을 모델링한다.

## 도메인 구조

```
dev.sayaya.handbook.domain/
├── Type               # 문서 타입/스키마 (id+version 복합키, 불변 버전 관리)
├── TypeLayout         # 타입 캔버스 시각화 (타입 도메인과 분리)
├── Attribute          # 타입 속성 (값 객체)
├── AttributeType      # 속성 데이터 타입 (sealed interface)
├── Validator          # 유효성 검증 규칙 (sealed interface)
└── Compliance         # 문서-스키마 호환성 검증 결과
```

## 불변 이력 모델

Type은 변경 시 기존 버전을 수정하지 않고 **새 버전을 생성**한다.
각 버전은 `effectDateTime ~ expireDateTime`으로 유효 기간을 관리한다.

- `id + version` 복합키로 식별
- 부모 타입 참조는 ID 값으로 느슨한 결합 유지

## 정합성 검증 (Compliance)

문서는 특정 타입 버전에 고정되지 않는다. 검증 시 현재 유효한 타입 버전들과의 호환 여부를 판별한다.

```
타입 새 버전 생성 → ValidationTask(NEW) → 기존 문서 재검증 → Compliance 저장
                                                           ├─ 호환 → DONE
                                                           └─ 불일치 → FAILED + 사유 기록
```

## 테스트

```bash
./gradlew :schema:test
./gradlew :schema:koverVerify  # 커버리지 80% 이상 필수
```
