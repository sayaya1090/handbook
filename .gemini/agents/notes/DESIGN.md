# Handbook 에이전트 설계

이 문서는 Handbook 프로젝트에서 Gemini 가 사용할 서브에이전트 세트의 설계 배경과 구성을 기록한다.
개별 에이전트 정의는 `.gemini/agents/<name>.md` 에 있다.

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
- 사용자는 서브에이전트 존재를 인식할 필요 없음 — Gemini 내부 최적화

### 1.3 사용자 vs Gemini 관점

| 관점 | 내용 |
|------|------|
| 사용자 | 메인 대화만 경험. 속도·정확도 개선, 컨텍스트 초과로 인한 저하 감소 |
| Gemini | 메인 컨텍스트 가볍게 유지, 큰 문서 읽기 대행, 도메인별 누적 지식 활용 |

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
| **schema-expert** | `schema/`, `type-ui/`, `type-command/`, `type-query/`, `docs/requirements/schema.md` | 속성 타입 추가 절차, 타입 버전 생성 흐름 |
| **document-expert** | `document/`, `document-ui/`, `document-command/`, `document-query/`, `docs/requirements/document.md` | 더티 트래킹 규칙, JSONB 머지, import/export |
| **workspace-expert** | `workspace/`, `workspace-ui/`, `workspace-command/`, `docs/requirements/workspace.md` | 조인 흐름, 프레즌스, 그룹 관리 |
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
| **cluster-ops** | `charts/`, `helm-lib/`, `docs/system-overview.md` (배포), `.gemini/skills/deployment.md`, `.gemini/skills/kargo-strategy.md` | **`oc` 실행 가능** — 런타임 메시·Prometheus·ArgoCD 상태 진단 |

### 3.4 메타(구조) 전문가 (1)

| 에이전트 | 스코프 | 역할 |
|---------|--------|------|
| **docs-keeper** | `docs/**/*.md`, 모든 모듈 문서, `.gemini/skills/doc-structure.md`, `.gemini/agents/*.md`, `GEMINI.md`, `memory/MEMORY.md` | 구조 무결성 감시. 크로스체크, 계약 인벤토리 동기화, 인덱스 유지 |

### 3.5 합치지 않은 이유

- **auth ↔ workspace**: 권한 모델 밀접하지만 경계 선언이 RBAC 규칙 표면화에 도움
- **schema ↔ document**: 상호 참조 많지만 각 UI/API 볼륨이 커서 합치면 컨텍스트 오버플로
- **dashboard → ui-platform 포함**: 차트는 중요하지만 단독 에이전트 만들 정도는 아님
- **mcp-server → landing 포함**: 외부 AI 테마가 랜딩 §3.22–§3.23 에서 이어짐

### 3.6 내장 에이전트와의 관계

- `Plan`, `Explore`, `general-purpose` 는 Gemini Code 기본 — 설계/탐색 일반 작업용
- 프로젝트 에이전트는 **도메인 지식이 누적된 검증·조회** 전용
- `gemini-code-guide`, `security-review` 등은 그대로 활용

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

모든 도메인 에이전트의 `.gemini/agents/*.md` 에 공통 삽입:

```
인터페이스 관련 질문을 받으면, 내 모듈 코드만 보고 판단하지 말고 반드시
docs/contracts/ 의 해당 계약 문서를 먼저 읽는다.

자신의 모듈이 영향을 주거나 받는 계약이면, 다른 소비자/공급자 목록을
계약 문서에서 확인하고 크로스 체크해야 할 대상을 명시해서 반환한다.

"내 모듈 관심사 아님" 으로 답하기 전에 계약 문서 확인 필수.

요구사항·유스케이스·모듈 문서를 수정할 때는, 자신의 도메인 파일만 편집하고,
글로벌 인덱스(GEMINI.md, contracts/README.md)나 크로스 도메인 문서는
docs-keeper 에게 위임해야 할 항목으로 반환한다.
```

---

## 6. 메인 에이전트(Gemini) 라우팅 규칙

GEMINI.md 에 추가될 내용:

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
- **인덱스 유지자** — GEMINI.md, `docs/contracts/README.md`, `MEMORY.md`, 모듈 트레이서빌리티
- **테스트 커버리지 감시자** — UC ↔ 테스트 매핑, 요구사항 → UC → 테스트 체인 검증 (§7.5)

### 7.2 금지 사항

- 도메인 의사결정 금지 — 불일치 발견 시 플래그만, 판단은 도메인 에이전트
- 새 요구사항 작성 금지 — 위치 안내만
- 코드 내부 로직 읽지 않음 — 단, 테스트 파일의 **존재와 타이틀** 은 확인 대상 (상세 로직 이해는 도메인 에이전트)
- 새 테스트 작성 금지 — 누락 플래그만, 작성은 도메인 에이전트가 담당

### 7.3 상시 체크 항목

| 검사 | 설명 |
|------|------|
| 고아 UC | 매트릭스에서 참조되지 않는 UC |
| 깨진 링크 | 존재하지 않는 섹션·파일 참조 |
| 모듈 문서 누락 | 신규 모듈 4종 세트(README/DESIGN/USECASE/CLASS-DIAGRAM) |
| API 표 vs 엔드포인트 | @*Mapping 과 §4 표 동기화 |
| 계약 공급자/소비자 vs 실제 import | 인벤토리 정합성 |
| MEMORY.md vs 실제 파일 | 인덱스의 링크 실존 |
| GEMINI.md 라우팅 vs 에이전트 정의 | 정의된 에이전트와 라우팅 일치 |
| 중복 내용 | 같은 정보가 두 문서에 있으면 참조 형태로 제안 |
| **UC → 테스트 매핑** | 각 UC 에 대응하는 테스트 존재 여부 (§7.5) |
| **요구사항 → UC 체인** | 각 §3.X 가 최소 1개의 UC 로 연결되는지 |
| **트레이서빌리티 매트릭스 "미구현" 진위** | 매트릭스가 "미구현" 표시인데 실제로 테스트 존재/부재 여부 |

### 7.4 자기 참조 규칙

- `.gemini/agents/*.md` 변경 시 → GEMINI.md 라우팅·계약 매트릭스 갱신 제안
- 새 계약 추가 시 → `docs/contracts/README.md` 매트릭스 자동 갱신 제안

### 7.5 테스트 커버리지 검증

GEMINI.md 규칙: "모든 유스케이스에는 시퀀스 다이어그램과 대응 테스트가 있어야 한다.
테스트가 없는 UC 는 매트릭스에 '미구현'으로 표시."

docs-keeper 가 이 규칙을 기계적으로 적용한다.

#### 7.5.1 UC → 테스트 매핑 탐지 방법

다음을 순차 적용 (어느 하나라도 매칭되면 "테스트 존재" 판정):

1. **명시적 UC 참조**: 테스트 파일에 `UC-XX` 리터럴이 포함된 경우
   - Kotest `describe`/`given`/`when`/`then` 블록 텍스트
   - JUnit `@DisplayName` 애노테이션
   - Playwright 테스트 타이틀
   - 주석 (`// UC-07: SEO 랜딩 방문`)
2. **모듈 USECASE.md 트레이서빌리티 매트릭스**: "구현체" 열에 테스트 클래스명 명시된 경우
3. **명명 규칙 매칭**: UC 제목의 핵심 동작이 테스트 이름에 포함 (약한 매칭 — 경고로만 표시)

3번만 매칭되는 경우는 "추정됨 (명시적 참조 권장)" 으로 리포트.

#### 7.5.2 보고 포맷

docs-keeper 는 아래 표 형태로 결과를 반환:

```
| UC | 제목 | 매트릭스 표시 | 실제 테스트 | 상태 |
|----|------|--------------|-------------|------|
| UC-07 | SEO 랜딩 방문 | 미구현 | 없음 | OK (매트릭스 일치) |
| UC-08 | 로그인 자동 리다이렉트 | 구현 | 없음 | 🚨 매트릭스 거짓 양성 |
| UC-09 | 앱 내부 랜딩 방문 | 미구현 | LandingActivityTest#방문_시_FeatureGrid_표시 | 🚨 매트릭스 거짓 음성 — 업데이트 필요 |
| UC-30 | 타입 정의 | 구현 | PersistTypeIntegrationTest#타입_일괄_저장 | OK |
```

#### 7.5.3 체크 범위

- 백엔드: `*/src/test/**/*.kt` (Kotest BehaviorSpec), `*/src/test/**/*Test.java`
- 프론트엔드: `*-ui/src/test/**/*.java` (GWT + Playwright)
- E2E: `e2e/src/test/**/*` (Playwright + HttpClient)
- 누락을 발견해도 테스트 자체는 작성하지 않음 — 도메인 에이전트에게 위임

#### 7.5.4 보고 의무

매트릭스 불일치를 발견하면:
1. **거짓 양성** (매트릭스: 구현 / 실제: 없음) → 즉시 플래그, 매트릭스 수정 제안 또는 테스트 작성 요청
2. **거짓 음성** (매트릭스: 미구현 / 실제: 존재) → 매트릭스 업데이트 제안
3. **미연결** (UC 는 있는데 매트릭스에서 추적 안 됨) → 매트릭스에 행 추가 제안

#### 7.5.5 활용 시나리오

```
나: [새 UC 추가 후]
Agent(docs-keeper, "
  UC-07, UC-08, UC-09, UC-85 가 방금 추가되었음.
  각각의 대응 테스트 존재 여부 확인 + 해당 모듈 USECASE.md 트레이서빌리티
  매트릭스에 올바르게 반영되어 있는지 검증.
")
```

```
나: [PR 리뷰 전]
Agent(docs-keeper, "
  전체 UC 목록(docs/usecases/) 과 테스트 커버리지 전수 감사. 불일치만 보고.
")
```

---

## 8. 구현 순서

각 스텝마다 커밋한다.

1. **`docs/contracts/` 디렉토리 신설** + 흩어진 계약 내용 이동
2. **`docs/requirements/` 도메인별 분할** (auth, schema, document, workspace, assistant, landing, external-ai, shell, events, dashboard, mobile, operations 등)
3. **`docs/usecases/` 섹션별 분할**
4. **`docs/architecture.md` 슬림화** — 모듈 섹션은 모듈 README 로 통합, 가로 관심사만 유지
5. **GEMINI.md 라우팅 규칙 + 도메인 인덱스 테이블 추가**
6. **`.gemini/agents/*.md` 9개 정의** (도메인 6 + 플랫폼 2 + 운영 1)
7. **`.gemini/agents/docs-keeper.md`** — 완성된 구조를 기준으로 검증 규칙 작성 (가장 마지막)
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
| 사용자에겐 서브에이전트 투명 | 메인 Gemini 가 라우팅 결정. 사용자는 결과만 경험 |
| 에이전트 업무 고도화 지원 (§10) | 정의 고정이 아니라 경험 누적 → 갈수록 빠르고 정확 |

---

## 10. 에이전트 업무 고도화 (Progressive Specialization)

각 에이전트가 시간이 지남에 따라 자신의 도메인에서 더 날카로워지도록 **학습 메모리** 를 갖춘다.
정의 고정이 아니라 운영 중 누적 → 경험 기반 전문화.

### 10.1 구조 — 정의와 노트의 엄격한 분리

```
.gemini/agents/
├── assistant-expert.md           # 에이전트 정의 — 부여된 원칙. 에이전트 자신은 절대 수정 금지
└── notes/
    ├── DESIGN.md                 # 이 문서 (전체 설계 원칙)
    └── assistant-expert.notes.md # 에이전트의 업무 노트 — 에이전트가 스스로 갱신 가능
```

예: `schema-expert.md` (읽기만) + `.gemini/agents/notes/schema-expert.notes.md` (읽기+쓰기)

**두 파일의 권한 차이**:

| 파일 | 에이전트 권한 | 메인 Gemini(나) 권한 | 비고 |
|------|-------------|-------------------|------|
| `<agent>.md` | **읽기만** | 읽기+쓰기 | 시스템 프롬프트, 스코프, 도구 목록, 핵심 규칙. 에이전트 존재 이유 |
| `.gemini/agents/notes/<agent>.notes.md` | **읽기+쓰기** | 읽기+쓰기 (주기적 감사) | 경험 누적, 패턴, 반복 함정. 에이전트가 자유롭게 편집 |

에이전트 정의 파일의 시스템 프롬프트 마지막에 강제 조항:

```
절대 `.gemini/agents/<your-name>.md` 를 수정하지 말 것.
네가 쓸 수 있는 파일은 `.gemini/agents/notes/<your-name>.notes.md` 뿐이다.
정의 변경이 필요하다고 판단되면 notes.md 에 "원칙 갱신 제안" 으로 기록하고,
메인 Gemini 가 주기 감사에서 처리한다.
```

### 10.2 노트 파일에 들어가는 것

**도메인 사실이 아니라 "일하는 방법" 중심.** 프로젝트 문서에 써야 할 것과 구분한다.

| 유형 | 예시 (schema-expert) |
|------|---------------------|
| **효과적 탐색 패턴** | "타입 버전 관련 질문은 `TypeVersionService` + `type_versions` 테이블 스키마 먼저 확인" |
| **반복 함정** | "R2DBC JSONB 는 `io.r2dbc.postgresql.codec.Json` — String 이면 bad SQL grammar. 3번 반복됨" |
| **내부 체크리스트** | "타입 변경 영향도 평가 시 반드시 document-expert 에 재검증 영향 질의" |
| **과거 실수의 반성** | "2026-02 TypeLayoutService 를 TypeService 로 혼동 → 도메인 분리 이유 명시 중요" |
| **해당 도메인 특유의 단축 질의** | "`@Version` 이 누락된 엔티티 찾기 → grep 'data class.*Entity' | xargs grep -L '@Version'`" |
| **문서에 없는 관용** | "FieldUpdate DSL 은 Spring Data R2DBC 내부 API. 사용 시 R2dbc* 패키지에 묶어둘 것" |

### 10.3 업데이트 흐름

#### 10.3.1 에이전트의 자가 갱신 (매 요청 필수)

에이전트는 **모든 호출에서** 자신의 `notes.md` 를 Edit 툴로 갱신한다. 메인 Gemini 의 개별 승인 없음.

**매 호출 3단계** (정의 파일 "## 노트 갱신 (필수 — 매 요청)" 섹션에 동일 내용 복제):

1. **요청 로그 한 줄 추가 (예외 없음)** — `## 요청 로그` 최상단에 `- YYYY-MM-DD: <요청 ≤20자> → <결론 ≤40자>`. "특별할 것 없음" 도 기록. 이는 "노트 쓸 게 없다" 는 판단으로 섹션이 비어버리는 회귀를 방지한다.
2. **Crystallized 섹션 보강 (해당 시)** — 반복 함정 해결/3회째 질의 패턴/사용자 피드백으로 틀림/정의 승격 후보 4종 트리거에 해당하면 해당 섹션에 직접 추가.
3. **로그 압축 (30항목 초과 시)** — 가장 오래된 10개를 Crystallized 로 승격 / 1회성이면 삭제 / 패턴 징후는 `## 아카이브 요약` 에 한 줄 집약. 압축 후 `## 요청 로그` 30줄 이내. 컨텍스트 압축과 동일 모델 — 에이전트 자신이 수행.

에이전트는 응답에 **갱신 요약 한 줄** 포함 (빈 섹션 금지):

```
=== 답변 ===
[요청받은 내용]

=== 노트 갱신 ===
요청 로그 1줄 추가 + 반복 함정 "R2DBC JSONB" 보강 + 로그 30건 이내 유지
```

메인 Gemini(나) 는 이를 로그로 받되, 개별 항목을 검토하지 않음.

#### 10.3.2 메인 Gemini(나) 의 주기 감사

작업 도중 개입하지 않고, 별도의 감사 시점에 일괄 검토:

- **주기**: 주 1회 (또는 사용자 요청 시) — 매 호출 로그 누적으로 감사 재료가 풍부해진 만큼 사이클 단축
- **방법**: docs-keeper 에게 위임 → 각 notes.md 를 분석하고 다음을 분류
  1. **정의로 승격할 원칙** — 반복 확인된 작업 원칙. `<agent>.md` 의 시스템 프롬프트로 흡수
  2. **정식 문서로 승격할 도메인 사실** — 요구사항·계약 문서로 이동
  3. **교체·삭제할 노이즈** — 코드 변경으로 유효하지 않아진 것, 중복
  4. **유지** — 작업 패턴으로 그대로 둘 것
- **승격 시 변경**: 메인 Gemini 가 `<agent>.md` 의 시스템 프롬프트를 편집. 승격된 항목은 notes 에서 제거 (단일 출처 유지)

이 감사는 **에이전트 호출 흐름 밖의 별도 작업** — 사용자에게 "노트 감사 실행할까요?" 라고 물어서 수행.

#### 10.3.3 승격 판정 기준

| notes → 정의 승격 (`<agent>.md` 프롬프트) | 조건 |
|----|----|
| 작업 원칙 | 3회 이상 동일 패턴 적용, 코드 변경에 무관하게 유효 |
| 금지 사항 | "이건 하면 안 된다" 가 명시적 실수로 2회 이상 발생 |
| 체크리스트 | 특정 상황에 반드시 거쳐야 할 단계가 확립 |

| notes → 정식 문서 승격 (요구사항/계약) | 조건 |
|----|----|
| 도메인 사실 | 사용자·다른 개발자가 알아야 하는 내용 |
| 공용 패턴 | 여러 에이전트 notes 에 반복 출현 → GEMINI.md 또는 `.gemini/skills/` 로 |
| 계약 정보 | 여러 모듈 간 약속 → `docs/contracts/` |

#### 10.3.4 docs-keeper 의 관리 역할

docs-keeper 는 감사 실행자로서:

- **중복 감지** — 여러 에이전트 notes 에 같은 내용 있으면 승격 제안
- **스테일 감지** — notes 의 코드 참조(클래스명·파일경로) 가 현재 유효한지 grep
- **비대화 경고** — 개별 notes 가 500 라인 초과 시 정제·카테고리 분리 제안
- **승격 후보 리포트** — §10.3.3 기준 매칭 항목을 메인 Gemini 에게 반환
- **승격 실행 자체는 메인 Gemini 가 수행** — docs-keeper 는 후보만 제시

### 10.4 에이전트 간 학습 전파

경계를 넘는 패턴은 에이전트 간 공유 필요:

```
(A) auth-expert 가 학습: "PAT 토큰은 32바이트 base64, prefix 'hbt_'"
   → 이건 도메인 사실이므로 docs/requirements/auth.md 에 기록
   → notes 대신 정식 문서

(B) schema-expert 가 학습: "타입 조회 시 effectDateTime 범위 필수"
   → 해당 에이전트의 notes 에만 유지 (작업 패턴)

(C) 여러 도메인 에이전트가 동일 패턴 학습:
   "GWT 모듈에서 Dagger 컴포넌트 주입 누락 시 MissingBinding 에러"
   → docs-keeper 가 감지 → `docs/design-patterns.md` 나 `.gemini/skills/gwt-stack.md` 로 승격
```

### 10.5 메인 에이전트(나) 의 행동 규칙

GEMINI.md 에 추가:

```
### 에이전트 노트 관리
- 에이전트 호출 응답의 "=== 노트 갱신 ===" 라인은 로그로만 받는다. 개별 개입 없음.
- 작업 흐름 중에는 notes.md 를 건드리지 않는다 (에이전트 자율 영역).
- 주기 감사(주 1회) 또는 사용자 요청 시 docs-keeper 에게 전수 리뷰 의뢰.
- docs-keeper 가 반환하는 "승격 후보" 리스트를 검토하고 직접 승격 실행:
  - 작업 원칙 → 해당 에이전트 정의(`<agent>.md`) 시스템 프롬프트 편집
  - 도메인 사실 → 요구사항/계약 문서로 이동
  - 공용 패턴 → GEMINI.md 또는 `.gemini/skills/`
- 승격 후 원본 항목은 notes 에서 제거 (단일 출처 유지).
```

### 10.6 노트 포맷 표준

```markdown
# <agent-name> Operational Notes

에이전트 자신이 갱신하는 업무 노트. 작업 패턴·반복 함정·내부 체크리스트 기록.
이 파일은 에이전트가 직접 편집한다. 정의 파일(`<agent-name>.md`) 은 건드리지 않는다.

매 호출마다 `## 요청 로그` 최상단에 한 줄 추가. 30개 초과 시 압축 (정의 파일 "## 노트 갱신" 참조).
도메인 사실은 여기가 아니라 정식 문서(요구사항/계약) 로.

---

## 요청 로그

- 2026-04-18: 타입 필드 권한 문의 → 기존 read_roles/write_roles JSONB 패턴 안내
- 2026-04-17: 속성 타입 추가 절차 → 지원 타입 목록 + ValidatorEditorFactory 체크
- ...

## 탐색 패턴

- **타입 버전 쿼리**: `TypeVersionService.findActive(typeId, date)` 사용.
  단순 `typeRepository.findById` 는 이전 버전 포함되어 동작 어긋남.
  (2026-04 검증, 3회 반복)

## 반복 함정

- **R2DBC JSONB**: `io.r2dbc.postgresql.codec.Json` 타입 사용. `String` 은 "bad SQL grammar".
  (GEMINI.md 에 이미 있음 — 참조만)

## 내부 체크리스트

- [ ] 타입 변경 영향도 문의 시 → document-expert 호출 (재검증 트리거 영향)
- [ ] 속성 타입 추가 시 → schema.md 의 지원 타입 목록 + ValidatorEditorFactory 양쪽 업데이트 필요

## 과거 실수

- 2026-02: TypeLayoutService 를 TypeService 와 혼동해 잘못된 답변. 도메인 분리 이유 =
  캔버스 시각화와 타입 도메인의 의존 단절.

## 원칙 갱신 제안 (메인 Gemini 감사 대상)

- "타입 조회 응답에는 rev 필수 포함" — 이걸 정의 파일의 기본 원칙으로 추가하면 좋겠음.
  (메인 Gemini 가 주기 감사에서 판정)

## 아카이브 요약

- 2026-03: 타입 버전 검증 관련 질의 5건 — 지금은 `## 탐색 패턴` 단축 경로로 흡수됨

---

마지막 감사: YYYY-MM-DD (메인 Gemini)
```

### 10.7 기대 효과

- **첫 달**: 에이전트가 자주 틀리거나 느린 질문 유형을 식별
- **3개월차**: 반복 함정 대부분 notes 에 축적 → 같은 실수 안 반복
- **6개월차**: 메인 에이전트(나)의 라우팅이 정확해짐 — 어떤 질문을 어느 에이전트에 보내면 가장 빠른지 학습
- **1년차**: 각 에이전트가 자기 도메인에서 문서보다 빠른 실전 지식 축적 — "이 상황엔 여기부터 봐야 함" 패턴 확립

### 10.8 주의

- **노트가 도메인 문서를 대체하면 안 됨** — 사용자·다른 개발자가 읽어야 하는 것은 정식 문서로
- **notes 는 Gemini 내부 운영 지식** — GEMINI.md 메모리와 유사한 위치
- 장기적으로 정식 문서와의 동기화는 docs-keeper 책임
- notes 파일은 Git 에 커밋 (재현 가능성, 팀원간 공유)

---

## 11. 에이전트 간 통신 (브로커 모델)

### 11.1 기본 원칙 — 직통 통신 금지

**에이전트끼리 서로 호출하거나 대화하지 않는다.** 항상 메인 Gemini 가 **브로커(중계자)** 로
개입한다. 근거:

- §2.1 에서 밝힌 "떠넘기기 차단" 효과 유지 — A 가 "이건 B 영역" 로 단정 후 B 에게 직행하면
  합의·책임 주체가 모호해짐
- §9 의 "서브에이전트는 one-shot 전용" 원칙 유지 — 직통 허용 시 A→B→A 재귀로 컨텍스트 폭발
- 사용자 투명성 유지 — 모든 왕복은 메인 대화에 기록됨

### 11.2 `followup` 필드 — 구조화된 중계 힌트

각 도메인 에이전트는 응답에 `=== followup ===` 섹션을 통해 **"다음으로 누구에게 무엇을
물어야 하는지"** 를 **구조화된 YAML** 로 반환한다. 메인 Gemini 는 이를 파싱하여
자동 후속 호출 여부를 판단한다.

#### 11.2.1 표준 포맷

```yaml
=== followup ===
- agent: events-expert
  priority: required
  reason: events.md 의 WORKSPACE_* 섹션이 현재 공백
  question: |
    WORKSPACE_CREATED/DELETED 를 handbook-events 로 통합할지,
    별도 workspace-events 유지할지 결정 필요
- agent: docs-keeper
  priority: optional
  reason: contracts/README.md 매트릭스 행 갱신
  question: workspace 행 Menu=O 반영 가능한지
```

**필드 규칙**:

| 필드 | 값 | 설명 |
|------|----|------|
| `agent` | 등록된 에이전트 이름 | GEMINI.md 도메인 매핑 표에 존재해야 |
| `priority` | `required` \| `optional` | required 는 메인 Gemini 자동 후속 호출 대상. optional 은 사용자 판단 |
| `reason` | 한 줄 사유 | 왜 이 에이전트가 필요한지 |
| `question` | 실제 물어볼 질문 | 맥락·시간범위·반환 포맷 포함 (§9 맥락 전달 규칙 준수) |

#### 11.2.2 메인 Gemini 동작

메인 Gemini 는 에이전트 응답의 `followup` 블록을 읽고:

1. `required` 항목은 **해당 세션 내 자동 후속 호출** — 사용자 재승인 불필요
2. `optional` 항목은 응답 합성에 "권장 추가 조사" 로 노출, 사용자 판단에 위임
3. 순환 호출 방지 — 같은 에이전트가 같은 세션에서 2회 이상 followup 대상으로 지정되면
   스킵하고 사용자에게 보고
4. followup 깊이 최대 2단계 — A 의 followup 으로 호출된 B 의 followup 을 다시 호출하진 않음.
   필요 시 메인이 명시적으로 판단해 호출

#### 11.2.3 기존 `=== 크로스 도메인 영향 ===` 과의 관계

기존 "크로스 도메인 영향" 섹션은 **자유 서술** 로 "이 변경이 어느 도메인에 파급될지"
정보 전달. followup 은 그중 **즉시 후속 호출이 필요한 항목을 구조화** 한 것.

- 크로스 도메인 영향: 파급 범위 기록 (docs-keeper 감사에도 활용)
- followup: 자동 중계 대상 (메인이 파싱)

둘 다 유지하되 역할 분리.

### 11.3 notes.md 기반 비동기 공유 (강화)

에이전트 A 는 다른 에이전트의 `<other>.notes.md` 를 **읽을 수는 있으나 쓸 수는 없다**.
이 규칙은 그대로 유지하여 느슨한 결합을 보존한다.

- 실시간 협업 아님 — 에이전트 입장에서 타 notes 는 "과거의 다른 에이전트 경험담"
- docs-keeper 주기 감사 때 notes 간 중복·승격 대상 식별
- 직통 통신의 대체재가 아님 — 급한 협업은 followup 으로, 누적 경험은 notes 로

---

## 12. Triage 에이전트 (메타 에이전트)

### 12.1 배경

증상 기반 버그 리포트("X 가 안 뜬다", "Y 가 느리다") 를 받았을 때, 메인 Gemini 가
§8 규칙대로 공급자·소비자 양쪽을 병렬 호출하지만 **어느 도메인 몇 개를 어떤 순서로**
호출할지 판단하는 비용이 있다. 이번 2026-04-17 세션에서 workspace-expert +
ui-platform-expert + auth-expert 를 병렬 호출했는데 답변 일부가 겹치고 "이건 내 영역
아님" 응답도 섞여 효율 저하.

### 12.2 역할

**증상·요구사항을 입력받아, 호출해야 할 도메인 에이전트 목록과 각자의 질문 템플릿을
생성** 한다. **자기 자신은 도메인 답을 하지 않는다** — 오직 분류·라우팅 계획 생성.

### 12.3 스코프

- `GEMINI.md` 도메인 매핑 표 + `docs/contracts/README.md` 매트릭스 읽기
- 기존 에이전트 정의 파일(`.gemini/agents/*.md`) 스코프 필드 참조
- `docs/requirements/README.md`, `docs/usecases/README.md` 인덱스 (도메인 판별 보조)

### 12.4 입력·출력

**입력**: 사용자 원문 요청 + 메인 Gemini 의 맥락 요약

**출력** (구조화 YAML):

```yaml
=== triage plan ===
parallel_batch_1:
  - agent: auth-expert
    question: "... 300단어 이내"
  - agent: workspace-expert
    question: "..."
sequential_batch_2:
  - agent: cluster-ops
    question: "batch_1 결과를 받아 dev 배포 검증"
    depends_on: [auth-expert, workspace-expert]
skip:
  - agent: ui-platform-expert
    reason: 증상이 백엔드 한정으로 좁혀짐 (JWT 서명 실패)
```

### 12.5 사용 조건

메인 Gemini 는 다음 경우에 triage 를 먼저 호출한다:

1. **증상 기반 버그** (§8) 의 경우 + 관련 도메인이 3개 이상 후보일 때
2. **사용자 요청이 다중 도메인에 걸칠 때** — 예: "워크스페이스 기능 전면 점검"
3. **계약 변경 시** — OWNER/소비자 매트릭스 + 실제 의존성 중 어느 에이전트를 불러야
   할지 명확히 모를 때

**스킵 조건**: 단일 도메인이 명백한 경우 (예: "타입 버전 쿼리 패턴" → schema-expert 즉시).
triage 자체도 Agent 호출 비용이 있으므로 남발 금지.

### 12.6 제약

- **도메인 답변 금지** — 라우팅 계획만 생성
- **followup 사용 금지** — triage 자체가 followup 을 생성하면 무한 재귀
- **plan 실행 금지** — 계획만 반환, 호출은 메인 Gemini 가 수행
- **결정론적 출력** — 동일 입력에 동일 plan 반환 (매트릭스·스코프 기반)

### 12.7 기대 효과

- 병렬 호출 중복 제거 (이번 세션의 workspace-expert + ui-platform-expert + auth-expert
  일부 중복 답변 방지)
- 호출 순서 최적화 — 선행 결과가 후속 질문을 좁히는 경우 자동 직렬화
- 사용자에게도 "어떤 조사 계획으로 들어가는지" 투명 공개 가능
