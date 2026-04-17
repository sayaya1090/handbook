# 낙관적 잠금 / 버전 전파 계약

`@Version rev` 필드의 도메인 → API → 프론트엔드 전파 규약. 패치 기반 저장의 충돌 감지 기반.

## 공급자 (Providers)

- **schema / document / workspace domain** — 엔티티 `@Version rev` 필드
- **persist-type / persist-document / persist-workspace** — R2DBC 엔티티 → 도메인 변환 시 `rev` 유지
- **search-type / search-document** — 조회 응답에 `rev` 포함

## 소비자 (Consumers)

- **persist-*** 의 저장 핸들러 — 요청 본문의 `rev` 로 낙관적 잠금 검증
- **프론트엔드 ChangeTracker / Action** — 로컬 상태에 `rev` 보관, 저장 시 서버 전송
- **프론트엔드 EventHandler** — 이벤트 수신 시 `rev` 갱신

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 새 엔티티 도입 | `@Version rev: Long?` 필드 + `fromDomain()` 에서 `rev` 전달 |
| rev 타입 변경 | 전체 경로 (DB → R2DBC → Domain → DTO → 프론트엔드) |
| 병합 전략 변경 (PATCH JSONB 머지) | 백엔드 `||` 연산자 + 필드 수준 충돌 기준 재검토 |

---

## 불변원칙

- **모든 엔티티는 `@Version rev: Long?` 을 가진다** (schema 등 공유 도메인 포함)
- **`@Version` 있는 엔티티의 `fromDomain()` 은 반드시 `rev` 를 전달** — 누락 시 `DuplicateKeyException` on save (INSERT 시도)
- **`rev` 는 INSERT 시 null**, UPDATE 시 Spring Data R2DBC 가 자동 증가

## 전파 경로

```
DB (rev 컬럼)
  ↓
R2dbcXxxEntity (@Version rev)
  ↓ toDomain()
Xxx 도메인 객체 (rev 필드)
  ↓ DTO 변환 (Jackson)
API 응답 JSON ("rev": 3)
  ↓ FetchApi
프론트엔드 (ChangeTracker / Action 에 rev 보관)
  ↓ PATCH 요청 시 rev 포함
API 수신
  ↓ Domain 변환 (rev 유지)
  ↓ Repository.save (rev 자동 검증)
충돌 시 ConcurrentModificationException → 409 Conflict
```

## Kotlin 엔티티 예시

```kotlin
@Table("documents")
data class R2dbcDocumentEntity(
    @Id val id: UUID? = null,
    val workspace: UUID,
    val type: UUID,
    val serial: String,
    val data: Json,  // io.r2dbc.postgresql.codec.Json (JSONB)
    @Version val rev: Long? = null,
    val effectDateTime: Instant,
    val expireDateTime: Instant
) {
    fun toDomain(): Document = Document(
        id = id!!, workspace, type, serial,
        data = parseJsonb(data),
        rev = rev,  // ← 반드시 전파
        effectDateTime, expireDateTime
    )

    companion object {
        fun fromDomain(d: Document) = R2dbcDocumentEntity(
            id = d.id, workspace = d.workspace, type = d.type,
            serial = d.serial, data = toJsonb(d.data),
            rev = d.rev,  // ← 반드시 전파
            effectDateTime = d.effectDateTime,
            expireDateTime = d.expireDateTime
        )
    }
}
```

## 패치 기반 저장 (§3.6)

- 변경된 필드만 PATCH 로 전송
- 서버는 JSONB `||` 연산자로 머지 (document) 또는 속성 upsert (type)
- `@Version rev` 로 엔티티 레벨 동시성 보장
- 필드 수준 비충돌 병합 — 두 사용자가 다른 필드 수정 시 둘 다 성공

## 충돌 처리

- 저장 시 rev 불일치 → `OptimisticLockingFailureException`
- gateway → 409 Conflict 응답
- 프론트엔드는 최신 rev 조회 후 사용자에게 재시도/병합 UI 표시

## 이벤트 발행과의 관계

- 저장 성공 시 `rev` 증가된 최신 엔티티를 이벤트 payload 에 포함
- 구독자는 자신이 보유한 rev 보다 작거나 같으면 무시 (이미 반영됨)

## 주의 (자주 나는 함정)

- `R2DBC JSONB 컬럼은 String 이 아니라 io.r2dbc.postgresql.codec.Json 타입 사용` (CLAUDE.md)
- `@Id` 누락 시 "no Identifier. Update not possible" — 엔티티에 반드시 `@Id`
- `switchIfEmpty` 인자는 `Mono.defer { }` 감싸기 (eager evaluation 방지)
