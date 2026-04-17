# Handbook 계약 카탈로그

여러 모듈/도메인에 걸쳐 있는 **공유 인터페이스의 단일 출처(Single Source of Truth)**.
각 계약은 공급자(Providers) · 소비자(Consumers) · 변경 시 영향 범위를 명시한다.

**도메인 문서(`docs/requirements/`)가 아닌 여기를 공통 계약 기준으로 삼는다** —
계약 변경 시 관련된 모든 에이전트가 이 파일을 읽고 영향을 검토해야 한다.

---

## 계약 목록

| 계약 | 파일 | 요약 |
|------|------|------|
| Menu 집계 프로토콜 | [menus.md](menus.md) | `/menus` 엔드포인트 집계 규약, `MenuSupplier` 인터페이스, `Menu` 도메인 필드 |
| 도메인 이벤트 | [events.md](events.md) | Kafka `handbook-events` 토픽 이벤트 타입, 페이로드 스키마, DLQ |
| Agent Command 프로토콜 | [agent-commands.md](agent-commands.md) | 에이전트→UI 10종 커맨드 (navigate/mutate/preview/await_confirm 등) |
| RBAC Permission | [permissions.md](permissions.md) | `리소스:동작` 형식, 와일드카드 규칙, Role 계층 |
| REST API 엔드포인트 | [api.md](api.md) | 공개 REST 경로 카탈로그 (인증 스코프 포함) |
| Audit Trail | [audit.md](audit.md) | `AuditEntry` 구조, `caller_type` (user/internal_agent/external_agent/mcp_client) |
| 낙관적 잠금 / 버전 전파 | [versioning.md](versioning.md) | `@Version rev` 필드의 도메인→API→프론트엔드 전파 규약 |
| SSE 스트림 시맨틱스 | [sse.md](sse.md) | `/workspace/{id}/messages` 이벤트 포맷, keep-alive, 재연결 |
| MD3 디자인 토큰 | [design-tokens.md](design-tokens.md) | 색상·타이포·엘리베이션·셰이프·모션 공용 토큰 |

---

## 계약 ↔ 에이전트 매트릭스

각 에이전트의 역할:

- **OWNER** — 계약 문서 업데이트의 1차 책임
- **O** — 공급자 또는 소비자 (변경 시 영향 수신 필수)
- **W** — 감시자 (구조·일관성 검증)

```
              Menu  Event  Command  Permission  API  Audit  Version  SSE  Design
auth           .      .      .        OWNER      .     O      .        .     .
schema         .      O      .        O          O     .      O        .     .
document       .      O      .        O          O     .      O        .     .
workspace      .      O      .        O          O     .      O        .     .
assistant      .      O      OWNER    O          O     OWNER  .        .     .
landing        O      .      .        .          O     .      .        .     O
events         .      OWNER  .        .          .     .      .        OWNER .
ui-platform    O      .      O        O          O     .      .        O     OWNER
cluster-ops    .      .      .        .          .     .      .        .     .
docs-keeper    W      W      W        W          W     W      W        W     W
```

---

## 변경 절차 (에이전트 & 메인 Claude)

### 계약 변경을 요청받았을 때

1. **계약 문서 먼저 읽기** — 현재 공급자/소비자 목록 확인
2. **매트릭스에서 OWNER/소비자 식별** — 관련 에이전트 목록 작성
3. **관련 에이전트 병렬 호출** — 각 에이전트에게 "이 변경이 당신 영역에 미치는 영향" 질의
4. **합성하여 사용자에게 보고** — 변경 범위·위험·작업 항목 제시
5. **승인 시 계약 문서 + 각 영역 문서 동시 갱신**

### 공급자/소비자 인벤토리 표준

모든 계약 문서 상단에 다음 섹션 필수:

```markdown
## 공급자 (Providers)
- <모듈> — 공급 내용 (클래스명·파일 경로 명시)

## 소비자 (Consumers)
- <모듈> — 소비 지점 (클래스명·파일 경로 명시)

## 변경 시 체크 대상
- <변경 유형> → <체크 항목>
```

---

## 규칙

- **계약 전담 에이전트를 만들지 않는다** — 공동 소유 원칙. 떠넘기기 방지
- **도메인 에이전트는 자신의 모듈이 OWNER/소비자인 계약을 알아야 한다** — 시스템 프롬프트에 계약 목록 명시
- **docs-keeper 가 주기적으로 공급자/소비자 목록의 정합성 검증** — 실제 import/호출과 일치하는지
- **계약 파일이 없다면 먼저 만들고 내용 작성** — 빈 파일이라도 존재가 중요
