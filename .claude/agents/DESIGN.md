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
| CLAUDE.md 라우팅 vs 에이전트 정의 | 정의된 에이전트와 라우팅 일치 |
| 중복 내용 | 같은 정보가 두 문서에 있으면 참조 형태로 제안 |
| **UC → 테스트 매핑** | 각 UC 에 대응하는 테스트 존재 여부 (§7.5) |
| **요구사항 → UC 체인** | 각 §3.X 가 최소 1개의 UC 로 연결되는지 |
| **트레이서빌리티 매트릭스 "미구현" 진위** | 매트릭스가 "미구현" 표시인데 실제로 테스트 존재/부재 여부 |

### 7.4 자기 참조 규칙

- `.claude/agents/*.md` 변경 시 → CLAUDE.md 라우팅·계약 매트릭스 갱신 제안
- 새 계약 추가 시 → `docs/contracts/README.md` 매트릭스 자동 갱신 제안

### 7.5 테스트 커버리지 검증

CLAUDE.md 규칙: "모든 유스케이스에는 시퀀스 다이어그램과 대응 테스트가 있어야 한다.
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
| 에이전트 업무 고도화 지원 (§10) | 정의 고정이 아니라 경험 누적 → 갈수록 빠르고 정확 |

---

## 10. 에이전트 업무 고도화 (Progressive Specialization)

각 에이전트가 시간이 지남에 따라 자신의 도메인에서 더 날카로워지도록 **학습 메모리** 를 갖춘다.
정의 고정이 아니라 운영 중 누적 → 경험 기반 전문화.

### 10.1 구조 — 정의와 노트의 엄격한 분리

```
.claude/agents/
├── DESIGN.md                    # 이 문서 (전체 설계 원칙)
├── <agent-name>.md              # 에이전트 정의 — 부여된 원칙. 에이전트 자신은 절대 수정 금지
└── <agent-name>.notes.md        # 에이전트의 업무 노트 — 에이전트가 스스로 갱신 가능
```

예: `schema-expert.md` (읽기만) + `schema-expert.notes.md` (읽기+쓰기)

**두 파일의 권한 차이**:

| 파일 | 에이전트 권한 | 메인 Claude(나) 권한 | 비고 |
|------|-------------|-------------------|------|
| `<agent>.md` | **읽기만** | 읽기+쓰기 | 시스템 프롬프트, 스코프, 도구 목록, 핵심 규칙. 에이전트 존재 이유 |
| `<agent>.notes.md` | **읽기+쓰기** | 읽기+쓰기 (주기적 감사) | 경험 누적, 패턴, 반복 함정. 에이전트가 자유롭게 편집 |

에이전트 정의 파일의 시스템 프롬프트 마지막에 강제 조항:

```
절대 `.claude/agents/<your-name>.md` 를 수정하지 말 것.
네가 쓸 수 있는 파일은 `.claude/agents/<your-name>.notes.md` 뿐이다.
정의 변경이 필요하다고 판단되면 notes.md 에 "원칙 갱신 제안" 으로 기록하고,
메인 Claude 가 주기 감사에서 처리한다.
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

#### 10.3.1 에이전트의 자가 갱신 (승인 게이트 없음)

에이전트는 작업 중·후에 자신의 `notes.md` 를 **자유롭게 직접 편집** 한다.
메인 Claude 의 개별 승인 필요 없음.

편집 트리거 (에이전트의 시스템 프롬프트에 명시):
- 예상치 못한 함정에 부딪혀 해결한 경우 → "반복 함정" 에 기록
- 3번 이상 같은 질문 유형 받은 경우 → "탐색 패턴" 에 단축 경로 기록
- 사용자 피드백으로 틀렸음을 확인한 경우 → "과거 실수" 에 기록
- 정의 파일에 넣었으면 좋겠다 싶은 패턴 발견 → "원칙 갱신 제안" 섹션에 기록

에이전트는 작업 응답에 **한 줄 메타 정보** 를 포함:

```
=== 답변 ===
[요청받은 내용]

=== 노트 갱신 ===
notes.md 에 2개 항목 추가/갱신: "반복 함정: R2DBC JSONB", "탐색 패턴: 타입 버전 쿼리"
```

메인 Claude(나) 는 이를 로그로 받되, 개별 항목을 검토하지 않음.

#### 10.3.2 메인 Claude(나) 의 주기 감사

작업 도중 개입하지 않고, 별도의 감사 시점에 일괄 검토:

- **주기**: 2주 ~ 1개월 (또는 사용자 요청 시)
- **방법**: docs-keeper 에게 위임 → 각 notes.md 를 분석하고 다음을 분류
  1. **정의로 승격할 원칙** — 반복 확인된 작업 원칙. `<agent>.md` 의 시스템 프롬프트로 흡수
  2. **정식 문서로 승격할 도메인 사실** — 요구사항·계약 문서로 이동
  3. **교체·삭제할 노이즈** — 코드 변경으로 유효하지 않아진 것, 중복
  4. **유지** — 작업 패턴으로 그대로 둘 것
- **승격 시 변경**: 메인 Claude 가 `<agent>.md` 의 시스템 프롬프트를 편집. 승격된 항목은 notes 에서 제거 (단일 출처 유지)

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
| 공용 패턴 | 여러 에이전트 notes 에 반복 출현 → CLAUDE.md 또는 `.claude/skills/` 로 |
| 계약 정보 | 여러 모듈 간 약속 → `docs/contracts/` |

#### 10.3.4 docs-keeper 의 관리 역할

docs-keeper 는 감사 실행자로서:

- **중복 감지** — 여러 에이전트 notes 에 같은 내용 있으면 승격 제안
- **스테일 감지** — notes 의 코드 참조(클래스명·파일경로) 가 현재 유효한지 grep
- **비대화 경고** — 개별 notes 가 500 라인 초과 시 정제·카테고리 분리 제안
- **승격 후보 리포트** — §10.3.3 기준 매칭 항목을 메인 Claude 에게 반환
- **승격 실행 자체는 메인 Claude 가 수행** — docs-keeper 는 후보만 제시

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
   → docs-keeper 가 감지 → `docs/design-patterns.md` 나 `.claude/skills/gwt-stack.md` 로 승격
```

### 10.5 메인 에이전트(나) 의 행동 규칙

CLAUDE.md 에 추가:

```
### 에이전트 노트 관리
- 에이전트 호출 응답의 "=== 노트 갱신 ===" 라인은 로그로만 받는다. 개별 개입 없음.
- 작업 흐름 중에는 notes.md 를 건드리지 않는다 (에이전트 자율 영역).
- 주기 감사(2주~1개월) 또는 사용자 요청 시 docs-keeper 에게 전수 리뷰 의뢰.
- docs-keeper 가 반환하는 "승격 후보" 리스트를 검토하고 직접 승격 실행:
  - 작업 원칙 → 해당 에이전트 정의(`<agent>.md`) 시스템 프롬프트 편집
  - 도메인 사실 → 요구사항/계약 문서로 이동
  - 공용 패턴 → CLAUDE.md 또는 `.claude/skills/`
- 승격 후 원본 항목은 notes 에서 제거 (단일 출처 유지).
```

### 10.6 노트 포맷 표준

```markdown
# <agent-name> Operational Notes

에이전트 자신이 갱신하는 업무 노트. 작업 패턴·반복 함정·내부 체크리스트 기록.
이 파일은 에이전트가 직접 편집한다. 정의 파일(`<agent-name>.md`) 은 건드리지 않는다.

도메인 사실은 여기가 아니라 정식 문서(요구사항/계약) 로.

---

## 탐색 패턴

- **타입 버전 쿼리**: `TypeVersionService.findActive(typeId, date)` 사용.
  단순 `typeRepository.findById` 는 이전 버전 포함되어 동작 어긋남.
  (2026-04 검증, 3회 반복)

## 반복 함정

- **R2DBC JSONB**: `io.r2dbc.postgresql.codec.Json` 타입 사용. `String` 은 "bad SQL grammar".
  (CLAUDE.md 에 이미 있음 — 참조만)

## 내부 체크리스트

- [ ] 타입 변경 영향도 문의 시 → document-expert 호출 (재검증 트리거 영향)
- [ ] 속성 타입 추가 시 → schema.md 의 지원 타입 목록 + ValidatorEditorFactory 양쪽 업데이트 필요

## 과거 실수

- 2026-02: TypeLayoutService 를 TypeService 와 혼동해 잘못된 답변. 도메인 분리 이유 =
  캔버스 시각화와 타입 도메인의 의존 단절.

## 원칙 갱신 제안 (메인 Claude 감사 대상)

- "타입 조회 응답에는 rev 필수 포함" — 이걸 정의 파일의 기본 원칙으로 추가하면 좋겠음.
  (메인 Claude 가 주기 감사에서 판정)

---

마지막 감사: YYYY-MM-DD (메인 Claude)
```

### 10.7 기대 효과

- **첫 달**: 에이전트가 자주 틀리거나 느린 질문 유형을 식별
- **3개월차**: 반복 함정 대부분 notes 에 축적 → 같은 실수 안 반복
- **6개월차**: 메인 에이전트(나)의 라우팅이 정확해짐 — 어떤 질문을 어느 에이전트에 보내면 가장 빠른지 학습
- **1년차**: 각 에이전트가 자기 도메인에서 문서보다 빠른 실전 지식 축적 — "이 상황엔 여기부터 봐야 함" 패턴 확립

### 10.8 주의

- **노트가 도메인 문서를 대체하면 안 됨** — 사용자·다른 개발자가 읽어야 하는 것은 정식 문서로
- **notes 는 Claude 내부 운영 지식** — CLAUDE.md 메모리와 유사한 위치
- 장기적으로 정식 문서와의 동기화는 docs-keeper 책임
- notes 파일은 Git 에 커밋 (재현 가능성, 팀원간 공유)
