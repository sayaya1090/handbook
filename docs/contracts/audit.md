# Audit Trail 계약

에이전트·외부 AI·사용자 행동의 감사 추적 규약. `AuditEntry` 구조와 `caller_type` 분류.

## 공급자 (Providers)

- **assistant** — 내부 에이전트 실행 전체를 `AuditEntry` 로 기록
  - `usecase/AuditService.kt`, `domain/AuditEntry.kt`
  - `interfaces/api/AuditController.kt` → `GET /assistant/audit`
- **gateway** — 외부 AI 에이전트 호출 시 `caller_type=external_agent` 로 감사 엔트리 생성
- **(후속) mcp-server** — `caller_type=mcp_client` 로 감사 엔트리 생성
- **persist-*** — (예정) 사용자 직접 액션도 감사 추적 확대 (§6.9)

## 소비자 (Consumers)

- **dashboard-ui** — 감사 이력 조회 UI (타임라인, 필터)
- **워크스페이스 관리자** — `/workspace/{ws}/audit-logs` 조회
- **외부 감사 시스템** — (향후) 이벤트 export

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| `AuditEntry` 필드 추가 | DB 마이그레이션 + 모든 발행자 매핑 + 조회 UI |
| `caller_type` 값 추가 | 감사 로그 필터 UI + rate limiting 규칙 |
| 저장소 변경 | 불변성 보장 (감사 로그 수정·삭제 불가) |

---

## `AuditEntry` 구조

```kotlin
data class AuditEntry(
    val id: UUID,
    val workspace: UUID,
    val callerType: CallerType,       // 호출자 분류
    val callerId: String,             // user UUID 또는 token ID
    val callerName: String?,
    val timestamp: Instant,
    val intent: String,               // 원본 자연어 요청 또는 작업 설명
    val executionId: UUID?,           // 에이전트 실행 식별자 (있는 경우)
    val commands: List<AgentCommand>?, // 실행된 커맨드 목록
    val plan: ExecutionPlan?,         // 사용자가 확인한 실행 계획
    val artifact: Artifact?,          // 결과 아티팩트
    val status: ExecutionStatus,      // SUCCESS / FAILED / ABORTED
    val error: String?
)

enum class CallerType {
    USER,             // 사용자 직접 액션
    INTERNAL_AGENT,   // Handbook 내부 assistant
    EXTERNAL_AGENT,   // 외부 AI 에이전트 (OpenAPI function calling)
    MCP_CLIENT        // MCP 프로토콜 클라이언트 (후속)
}
```

## 불변성 보장

- `audit_logs` 테이블은 **INSERT-only**
- UPDATE / DELETE 거부 (애플리케이션 및 DB 레벨)
- 보존 정책: 무기한 (또는 워크스페이스별 정책)

## 핵심 추적 항목

에이전트 (내부/외부) 의 행동은 모두 근거가 있어야 하며, 사후 감사가 가능해야 한다.

- **의도 근거**: 원본 자연어 요청 + LLM 해석 결과(intent, confidence)
- **커맨드별 사유**: 각 `AGENT_COMMAND` 의 `description` 필드 ([agent-commands.md](agent-commands.md))
- **실행 계획**: 사용자가 확인한 단계 목록 — "왜 이 변경이 발생했는가" 추적
- **이벤트 불변 로그**: Kafka 에 영구 보존

## 조회 API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/assistant/audit?workspace={id}` | 에이전트 감사 추적 |
| GET | `/workspace/{ws}/audit-logs` | 전체 감사 로그 (사용자 + 에이전트) |

## 대시보드 / 조회 UI

필터 지원:
- 기간 (from/to)
- 사용자 (intent 부분 일치 검색)
- `callerType` (USER / INTERNAL_AGENT / EXTERNAL_AGENT / MCP_CLIENT)
- 실행 상태 (SUCCESS / FAILED / ABORTED)

## Rate Limiting 연관

외부 에이전트 호출은 토큰 단위 Rate Limiting 적용 (§7.1):
- `audit_logs.caller_id = token_id` 로 집계
- 분당 한도 초과 시 429 반환 + 감사 로그에 실패 기록
