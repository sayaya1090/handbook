# Handbook 에이전트 설계

이 문서는 Handbook 프로젝트에서 Claude 가 사용할 서브에이전트 세트의 설계 배경과 구성을 기록한다.
개별 에이전트 정의는 `.claude/agents/<name>.md` 에 있다.

---

## 1. 설계 동기

### 1.1 문제 인식

- `docs/requirements.md` 1500+ 라인, `docs/architecture.md` 1400+ 라인, `docs/usecases.md` 1800+ 라인
- Read 단일 호출 한도 초과, 편집 시 `old_string` 매칭 범위가 넓어 실수 위험
- 글로벌 문서와 모듈 문서(README/DESIGN/USECASE/CLASS-DIAGRAM) 간 설명 중복
- 크로스체크(doc-structure.md 매트릭스)가 수동이라 누락 위험

### 1.2 해결 방향

**문서를 도메인별로 분할 + 업무별 서브에이전트로 분담**

- 메인 대화는 사용자와 설계 반복 + 통합 작업 유지
- 컨텍스트 많이 읽어야 하는 국소 작업은 서브에이전트에 위임 (scope 제한, 요약 반환)
- 사용자는 서브에이전트 존재를 인식할 필요 없음 — Claude 내부 최적화

### 1.3 사용자 vs Claude 관점

| 관점 | 내용 |
|------|------|
| 사용자 | 메인 대화만 경험. 속도·정확도 개선, 컨텍스트 초과로 인한 저하 감소 |
| Claude | 메인 컨텍스트 가볍게 유지, 큰 문서 읽기 대행, 도메인별 누적 지식 활용 |

---

## 2. 경계 설계 시 고려한 리스크

### 2.1 모듈 경계에서의 "공 떠넘기기"

에이전트를 모듈/도메인으로만 나누면:
- 계약(interface)이 여러 에이전트에 걸쳐 있으면 **"그건 저쪽 몫"** 로 서로 회피
- 인터페이스 변경 시 전체 영향도가 누구의 책임도 아님 → 사일런트 회귀

### 2.2 해결 전략

**계약을 모듈에 소속시키지 않고 공유 문서로 승격.** 도메인 에이전트는 모듈 내부 로직에 집중, 경계(계약)는 `docs/contracts/` 의 문서로 단일 출처화.

---

## 3. 에이전트 세트 (10개)

### 3.1 도메인 전문가 (6)

| 에이전트 | 스코프 파일 | 전형적 작업 |
|---------|-------------|-------------|
| **auth-expert** | `login/`, `login-ui/`, `authentication/`, `docs/requirements/auth.md` | PAT 도입 영향, OAuth 제공자 추가, RBAC 확장 |
| **schema-expert** | `schema/`, `type-ui/`, `persist-type/`, `search-type/`, `docs/requirements/schema.md` | 속성 타입 추가 절차, 타입 버전 생성 흐름 |
| **document-expert** | `document/`, `document-ui/`, `persist-document/`, `search-document/`, `docs/requirements/document.md` | 더티 트래킹 규칙, JSONB 머지, import/export |
| **workspace-expert** | `workspace/`, `workspace-ui/`, `persist-workspace/`, `docs/requirements/workspace.md` | 조인 흐름, 프레즌스, 그룹 관리 |
| **assistant-expert** | `assistant/`, `agent-protocol/`, `agent-ui/`, `docs/requirements/assistant.md` | 커맨드 처리, 감사 로그, 실행 계획 |
| **landing-expert** | `landing-content/`, `landing-ui/`, `docs/requirements/landing.md`, `docs/requirements/external-ai.md` | SEO 메타, 프리렌더, MCP 서버(후속) |

### 3.2 플랫폼 전문가 (2)

| 에이전트 | 스코프 | 작업 |
|---------|--------|------|
| **ui-platform-expert** | `shell-ui/`, `ui-components/`, `app/`, `agent-bridge/`, `dashboard-ui/`, `docs/design.md`, `docs/requirements/shell.md` | MD3 토큰, 동적 로딩, 차트 컴포넌트 |
| **events-expert** | `event/`, `event-broadcaster/`, `docs/requirements/events.md`, `docs/contracts/events.md` | Kafka 이벤트 타입, SSE, DLQ, 실시간 협업 |

### 3.3 운영 전문가 (1)

| 에이전트 | 스코프 | 도구 |
|---------|--------|------|
| **cluster-ops** | `charts/`, `helm-lib/`, `docs/system-overview.md` (배포), `.claude/skills/deployment.md`, `.claude/skills/kargo-strategy.md` | **`oc` 실행 가능** — 런타임 메시·Prometheus·ArgoCD 상태 진단 |

### 3.4 메타(구조) 전문가 (1)

| 에이전트 | 스코프 | 역할 |
|---------|--------|------|
| **docs-keeper** | `docs/**/*.md`, 모든 모듈 문서, `.claude/skills/doc-structure.md`, `.claude/agents/*.md`, `CLAUDE.md`, `memory/MEMORY.md` | 구조 무결성 감시. 크로스체크, 계약 인벤토리 동기화, 인덱스 유지 |

### 3.5 합치지 않은 이유

- **auth ↔ workspace**: 권한 모델 밀접하지만 경계 선언이 RBAC 규칙 표면화에 도움
- **schema ↔ document**: 상호 참조 많지만 각 UI/API 볼륨이 커서 합치면 컨텍스트 오버플로
- **dashboard → ui-platform 포함**: 차트는 중요하지만 단독 에이전트 만들 정도는 아님
- **mcp-server → landing 포함**: 외부 AI 테마가 랜딩 §3.22–§3.23 에서 이어짐

### 3.6 내장 에이전트와의 관계

- `Plan`, `Explore`, `general-purpose` 는 Claude Code 기본 — 설계/탐색 일반 작업용
- 프로젝트 에이전트는 **도메인 지식이 누적된 검증·조회** 전용
- `claude-code-guide`, `security-review` 등은 그대로 활용

---

## 4. 계약(Contract) 문서 설계

### 4.1 단일 출처 디렉토리: `docs/contracts/`

```
docs/contracts/
├── README.md              # 계약 매트릭스 (소유자·소비자·감시자)
├── menus.md               # Menu 도메인 + MenuSupplier 프로토콜
├── events.md              # Kafka 이벤트 카탈로그 (기존 kafka-events.md 이동)
├── agent-commands.md      # Agent Command 10종 상세 (기존 §3.17 발췌)
├── permissions.md         # RBAC Permission 매트릭스 (기존 §3.3 이동)
├── api.md                 # REST 엔드포인트 카탈로그 (기존 §4 이동)
├── audit.md               # AuditEntry / caller_type 규약
├── versioning.md          # @Version / rev 전파 규약
├── sse.md                 # SSE 스트림 시맨틱스
└── design-tokens.md       # MD3 토큰 (docs/design.md 축약 발췌)
```

### 4.2 계약 문서 표준 섹션

모든 계약 문서 상단에 공급자/소비자 인벤토리 의무화:

```markdown
# <계약명>

## 공급자 (Providers)
- 모듈 A — 공급 내용
- 모듈 B — 공급 내용

## 소비자 (Consumers)
- 모듈 C (구체 사용 지점)
- 모듈 D

## 변경 시 체크 대상
- 필드 추가 → 모든 소비자 기본값 동작 확인
- 스키마 변경 → 공급자·소비자 양방향 영향도
```

### 4.3 계약-에이전트 매트릭스 (`docs/contracts/README.md`)

```
           Menu  Event  Command  Permission  API  Audit  Version  SSE  Design
auth        .     .      .        OWNER       .    O       .       .     .
schema      .     O      .        O           O    .       O       .     .
document    .     O      .        O           O    .       O       .     .
workspace   .     O      .        O           O    .       O       .     .
assistant   .     O      OWNER    O           O    OWNER   .       .     .
landing     O     .      .        .           O    .       .       .     O
events      .     OWNER  .        .           .    .       .       OWNER .
ui-platform O     .      O        O           O    .       .       O     OWNER
cluster-ops .     .      .        .           .    .       .       .     .
docs-keeper W     W      W        W           W    W       W       W     W

OWNER = 계약 문서 업데이트의 1차 책임
O     = 공급자/소비자 (변경 영향 수신 필수)
W     = 감시자 (구조·일관성 검증)
```

---

## 5. 에이전트 시스템 프롬프트 공통 규칙

모든 도메인 에이전트의 `.claude/agents/*.md` 에 공통 삽입:

```
인터페이스 관련 질문을 받으면, 내 모듈 코드만 보고 판단하지 말고 반드시
docs/contracts/ 의 해당 계약 문서를 먼저 읽는다.

자신의 모듈이 영향을 주거나 받는 계약이면, 다른 소비자/공급자 목록을
계약 문서에서 확인하고 크로스 체크해야 할 대상을 명시해서 반환한다.

"내 모듈 관심사 아님" 으로 답하기 전에 계약 문서 확인 필수.

요구사항·유스케이스·모듈 문서를 수정할 때는, 자신의 도메인 파일만 편집하고,
글로벌 인덱스(CLAUDE.md, contracts/README.md)나 크로스 도메인 문서는
docs-keeper 에게 위임해야 할 항목으로 반환한다.
```

---

## 6. 메인 에이전트(Claude) 라우팅 규칙

CLAUDE.md 에 추가될 내용:

```
## 에이전트 라우팅 규칙

### 기본 원칙
- 도메인 깊이가 있는 요구사항/코드 조회 → 해당 도메인 에이전트
- 크로스 도메인 영향도 → 관련 전문가 복수 병렬 호출 후 합성
- 클러스터 상태/배포 문제 → cluster-ops
- 문서 구조 변경·크로스체크 → docs-keeper
- 코드 패턴 탐색 → Explore (내장)
- 설계 검토 → Plan (내장)

### 계약 변경 감지 시 강제 절차
인터페이스/계약이 걸린 작업(Menu/Event/Command/Permission/Audit/API/SSE 등)이면:
1. 계약 문서(docs/contracts/<X>.md) 먼저 확인 — 공급자/소비자 목록 파악
2. 해당 계약의 모든 당사자 에이전트를 병렬 호출하여 영향도 수집
3. 응답을 합성해서 사용자에게 전달

### docs-keeper 호출 시점
- 새 요구사항/유스케이스/모듈 추가 후 → 크로스체크
- 인터페이스 변경 후 → 계약 문서 동기화 확인
- 작업 착수 전 → "이 작업이 건드릴 문서 위치" 판정
- 중복 의심 시 → 동일 정보의 여러 문서 위치 탐지
```

---

## 7. docs-keeper 상세

### 7.1 역할 경계

- **구조 관리자** — 배치·관계·일관성
- **계약 감시자** — 계약 문서↔코드 현실 동기화
- **크로스체크 실행자** — `doc-structure.md` 매트릭스 자동 적용
- **인덱스 유지자** — CLAUDE.md, `docs/contracts/README.md`, `MEMORY.md`, 모듈 트레이서빌리티

### 7.2 금지 사항

- 도메인 의사결정 금지 — 불일치 발견 시 플래그만, 판단은 도메인 에이전트
- 새 요구사항 작성 금지 — 위치 안내만
- 코드 내부 로직 읽지 않음

### 7.3 상시 체크 항목

| 검사 | 설명 |
|------|------|
| 고아 UC | 매트릭스에서 참조되지 않는 UC |
| 깨진 링크 | 존재하지 않는 섹션·파일 참조 |
| 모듈 문서 누락 | 신규 모듈 4종 세트(README/DESIGN/USECASE/CLASS-DIAGRAM) |
| API 표 vs 엔드포인트 | @*Mapping 과 §4 표 동기화 |
| 계약 공급자/소비자 vs 실제 import | 인벤토리 정합성 |
| MEMORY.md vs 실제 파일 | 인덱스의 링크 실존 |
| CLAUDE.md 라우팅 vs 에이전트 정의 | 정의된 에이전트와 라우팅 일치 |
| 중복 내용 | 같은 정보가 두 문서에 있으면 참조 형태로 제안 |

### 7.4 자기 참조 규칙

- `.claude/agents/*.md` 변경 시 → CLAUDE.md 라우팅·계약 매트릭스 갱신 제안
- 새 계약 추가 시 → `docs/contracts/README.md` 매트릭스 자동 갱신 제안

---

## 8. 구현 순서

각 스텝마다 커밋한다.

1. **`docs/contracts/` 디렉토리 신설** + 흩어진 계약 내용 이동
2. **`docs/requirements/` 도메인별 분할** (auth, schema, document, workspace, assistant, landing, external-ai, shell, events, dashboard, mobile, operations 등)
3. **`docs/usecases/` 섹션별 분할**
4. **`docs/architecture.md` 슬림화** — 모듈 섹션은 모듈 README 로 통합, 가로 관심사만 유지
5. **CLAUDE.md 라우팅 규칙 + 도메인 인덱스 테이블 추가**
6. **`.claude/agents/*.md` 9개 정의** (도메인 6 + 플랫폼 2 + 운영 1)
7. **`.claude/agents/docs-keeper.md`** — 완성된 구조를 기준으로 검증 규칙 작성 (가장 마지막)
8. **docs-keeper 로 전체 크로스체크 1회 실행** → 남은 불일치 정리

---

## 9. 주요 설계 결정 요약

| 결정 | 이유 |
|------|------|
| 10개 에이전트 (6 도메인 + 2 플랫폼 + 1 운영 + 1 메타) | 도메인 보존하면서 과도한 분할 방지 |
| `docs/contracts/` 별도 승격 | 계약을 모듈에 묶지 않아 떠넘기기 차단 |
| docs-keeper 가 심판 포지션 | 플레이어와 심판 분리 — 도메인 결정은 도메인 에이전트가 |
| 계약 전담 에이전트 만들지 않음 | 떠넘길 명분을 원천 차단, 공동 소유 강화 |
| 서브에이전트는 one-shot 전용 | 긴 대화는 메인에서. 에이전트는 조회·검증·리서치 바운디드 작업만 |
| 사용자에겐 서브에이전트 투명 | 메인 Claude 가 라우팅 결정. 사용자는 결과만 경험 |
