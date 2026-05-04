# Handbook - 사용자 요구사항 정의서

## 1. 프로젝트 개요

Handbook은 **운영 중 스키마 변경과 이력 관리를 지원하면서 데이터 정합성을 유지**하는 문서 관리 시스템이다.

사용자는 워크스페이스 내에서 문서 타입(스키마)을 정의하고, 해당 타입에 따라 문서를 생성·관리할 수 있다.
서비스 중단 없이 스키마를 변경할 수 있으며, 변경 전후의 모든 상태를 이력으로 보존한다.
스키마 변경 시 기존 데이터와의 불일치는 검증 시스템이 감지하고, 사용자가 사후 보정하도록 유도한다.

### 핵심 목표

- **운영 중 변경**: 서비스를 중단하지 않고 타입(스키마)을 변경할 수 있다.
- **이력 관리**: 타입과 문서는 변경 시 기존 버전을 수정하지 않고 새 버전을 생성한다 (불변 이력). 모든 버전을 시간 기반(effectDateTime ~ expireDateTime)으로 보존하고, 특정 시점의 상태를 조회할 수 있다.
- **정합성 유지**: 스키마 변경 후 기존 데이터와의 불일치를 자동 감지하고, 사용자에게 경고하여 사후 보정을 유도한다 (강제 차단이 아닌 감지 + 사후 보정 방식).

### 아키텍처 원칙

- **CQRS**: 조회(Search)와 변경(Persist)을 분리한다.
- **이벤트 드리븐**: 도메인 변경 사항을 이벤트로 발행하고, 구독자가 비동기로 처리한다.
- **리액티브**: WebFlux 기반의 비동기·논블로킹 처리를 사용한다.
- **클린 아키텍처**: 각 모듈은 domain → usecase → interfaces 방향으로 의존한다. domain과 usecase는 프레임워크(Spring, Jackson 등)에 의존하지 않으며, 프레임워크 관련 설정은 interfaces 계층에 위치한다.
- **실시간 협업**: 같은 워크스페이스의 모든 참여자(사용자 + AI 에이전트)는 동일한 Kafka 이벤트 채널(`/workspaces/{id}/messages` SSE)을 공유한다. 한 사용자의 문서 저장, 다른 사용자의 타입 변경, 에이전트의 커맨드가 모두 같은 스트림으로 전달되어 전원이 실시간으로 변경사항을 관찰한다.
- **에이전트 = 협업자**: AI 에이전트는 별도의 시스템이 아니라 워크스페이스의 또 다른 참여자이다. 에이전트의 커맨드는 프론트엔드에서 시각적으로 실행되어, 마치 동료가 내 화면을 대신 조작해주는 것처럼 느껴진다. 모든 액션은 워크스페이스 이벤트로 기록되어 감사 추적이 가능하다.

### 시스템 구성

| 모듈 | 역할 |
|------|------|
| **workspace** | 워크스페이스·조직·권한 도메인 엔티티 |
| **schema** | 타입 시스템 도메인 엔티티 (Type, Attribute, Compliance 등) |
| **document** | 문서 생명주기 도메인 엔티티 |
| **event** | 도메인 이벤트 정의 (DocumentEvent, TypeEvent 등) |
| **search** | 검색 공유 라이브러리 (페이지네이션 + 필터 VO) |
| **authentication** | JWT 기반 인증·인가 라이브러리 (쿠키 → Spring Security) |
| **gateway** | API Gateway — 메뉴 집계, 서비스 라우팅 |
| **event-broadcaster** | Kafka 이벤트 수신 → SSE 실시간 브로드캐스트 |
| **login** | OAuth2 로그인 + JWT 토큰 발행 백엔드 |
| **login-ui** | 로그인/로그아웃 UI (GWT) |
| **workspace-command** | 워크스페이스 CUD 백엔드 |
| **type-command** | 타입 CRUD + 레이아웃 관리 백엔드 |
| **document-command** | 문서 CUD 백엔드 (저장/삭제 + Kafka 이벤트 발행) |
| **type-query** | 타입 읽기 전용 백엔드 (CQRS 읽기 측) |
| **document-query** | 문서 읽기 전용 백엔드 (검색/단건 조회 + 메뉴 제공) |
| **assistant** | 자연어 요청 해석 + 실행 계획 생성 + Kafka 이벤트 발행 (LLM 연동) |
| **shell-ui** | 웹 애플리케이션 프레임 (GWT) |
| **activity** | 동적 로딩 UI 활동 모듈 (GWT) |
| **agent-protocol** | 에이전트 커맨드 프로토콜 공유 라이브러리 (프론트/백) |
| **agent-ui** | 에이전트 커맨드 UI 렌더링 모듈 (GWT) |
| **ui-components** | 범용 UI 컴포넌트 라이브러리 — Toast, Overlay, Highlight, Scroll, ConfirmDialog, DiffPanel (GWT) |
| **agent-bridge** | GWT 모듈 간 에이전트 통신 브릿지 (CustomEvent + window 속성 기반) |
| **type-ui** | 캔버스 기반 타입 스키마 편집기 (GWT) |
| **workspace-ui** | 워크스페이스 생성/참여 UI (GWT) |
| **document-ui** | 스프레드시트 기반 문서 편집기 (GWT, Handsontable) |
| **dashboard-ui** | 워크스페이스 대시보드 UI (GWT) — 통계, 품질 현황, 에이전트 활동 |
| **landing-content** | SEO 랜딩 / 앱 내부 랜딩이 공유하는 기능 설명 카드 컬렉션 (GWT 순수 DOM 라이브러리) |
| **landing-ui** | SEO 랜딩 페이지 — 빌드 타임 프리렌더로 언어별 정적 HTML 생성, `/` 및 `/en/` 에 배포 (§3.22.2) |
| **e2e** | Playwright 기반 E2E 통합 테스트 |
| **app** | 정적 자산 호스트 — HTML, CSS, vendor JS, i18n 만 포함. GWT 컴파일 없음. shell-ui·agent-ui 는 각각 독립 GWT 모듈로 컴파일·배포(S3)되며, app.html 이 두 nocache.js 를 별도 `<script>` 로 로드 |

## 2. 도메인 모델

### 도메인 관계도

```mermaid
erDiagram
    Workspace ||--o{ Group : contains
    Workspace ||--o{ Type : contains
    Workspace ||--o{ Document : contains
    Workspace ||--o{ TypeLayout : contains
    TypeLayout }o--o{ Type : "positions (타입 배치)"

    Group ||--o{ Role : has
    Group }o--o{ User : belongs_to

    Type ||--o{ Attribute : has
    Type ||--o| Type : "parent (inheritance)"
    Type }o--o{ Compliance : validated_by

    Attribute ||--o| Validator : has

    Document }o--|| Type : "typed by"
    Document }o--o{ Compliance : validated_by

    Compliance }o--|| ValidationTask : "produced by"
```

### 버전 관리 모델

```mermaid
graph LR
    subgraph "Type: 'customer'"
        TV1["v1<br/>effect: 2024-01<br/>expire: 2024-06"]
        TV2["v2<br/>effect: 2024-06<br/>expire: ∞"]
    end
    TV1 -->|"새 버전 생성"| TV2

    subgraph "Document: serial='C-001'"
        DV1["ver1<br/>effect: 2024-03<br/>expire: 2024-09"]
        DV2["ver2<br/>effect: 2024-09<br/>expire: ∞"]
    end
    DV1 -->|"새 버전 생성"| DV2

    TV2 -.->|"재검증"| DV1
    TV2 -.->|"재검증"| DV2
```

### 정합성 검증 흐름

```mermaid
flowchart LR
    A["타입 변경<br/>(새 버전 생성)"] -->|이벤트 발행| B["ValidationTask<br/>생성 (NEW)"]
    B --> C["기존 문서를<br/>새 스키마로 검증"]
    C --> D{호환?}
    D -->|"모든 버전 만족"| E["DONE<br/>(정상)"]
    D -->|"불일치 발견"| F["FAILED<br/>Compliance 저장"]
    F --> G["사용자에게<br/>경고 노출"]
    G --> H["사용자가<br/>데이터 사후 보정"]
```

### 2.1 Workspace

- 독립된 테넌트 단위로 동작한다.
- 속성: id(UUID), name, description
- 워크스페이스 생성 시 Admin 그룹이 자동 생성되며, 생성자가 해당 그룹에 자동 배정된다.

### 2.2 User

- 속성: id(UUID), name
- 여러 워크스페이스에 소속될 수 있다.

### 2.3 Group

- 속성: id, workspace, name, description
- 워크스페이스 내에서 사용자를 조직화하고, 역할(Role)을 부여하는 단위이다.

### 2.4 Type (문서 타입/스키마)

- 속성: id, version, effectDateTime, expireDateTime, description, primitive, parent
- 버전 관리를 지원한다 (이전/다음 버전 링크, HATEOAS).
- 계층 구조를 지원한다 (parent를 통한 타입 상속).
- 유효 기간(effectDateTime ~ expireDateTime)으로 시간 기반 버전을 관리한다.
- 타입을 변경하면 기존 버전을 수정하지 않고 새 버전이 생성된다 (불변 이력).
- 새 버전 생성 시 해당 타입의 기존 문서에 대해 재검증이 트리거된다.

### 2.5 Attribute (타입 속성)

- 속성: name, order, description, type, nullable, inherited
- 지원하는 속성 타입:
  - **Text**: 단일 텍스트 값 (정규식 패턴 검증 지원)
  - **Bool**: 불리언 값
  - **Number**: 숫자 (범위 제한 지원)
  - **Date**: 날짜 (범위 제한 지원)
  - **Enum**: 허용 값 목록 중 선택
  - **Array**: 동일 타입의 다중 값
  - **Map**: 키-값 쌍 (키/값 각각 타입 지정)
  - **File**: 파일 참조 (확장자 검증)
  - **Document**: 다른 타입 문서에 대한 약한 참조 (serial 기반)

### 2.6 Validator (유효성 검증 규칙)

- **Regex**: 정규식 패턴 매칭
- **Bool**: 불리언 타입 검증
- **Number**: 숫자 범위 검증 (min/max)
- **Date**: 시간 범위 검증 (before/after)
- **Enum**: 허용 값 목록 검증

### 2.7 Document (문서)

- 속성: id, type, serial, data(Map<String, String?>), effectDateTime, expireDateTime
- 메타데이터: createDateTime, creator(UUID)
- 문서는 특정 타입 버전에 고정되지 않는다. 하나의 문서가 여러 버전의 스키마를 동시에 만족할 수 있다.
- 검증 시 현재 유효한 타입 버전 기준으로 호환 여부를 판별하며, 어떤 버전도 만족하지 못할 때만 경고한다.
- serial은 타입·워크스페이스 내에서 고유하다 (영숫자, 하이픈, 밑줄 허용).
- 문서를 변경하면 기존 버전을 수정하지 않고 새 버전이 생성된다 (불변 이력).
- 유효 기간(effectDateTime ~ expireDateTime)으로 버전의 시간 범위를 관리한다.

### 2.8 Type Layout (타입 시각화)

- 타입의 캔버스 시각화를 위한 별도 모델이다 (타입 도메인과 분리).
- 타입별 위치 속성: x, y, width, height
- Layout: workspace, id, effectDateTime, expireDateTime
- 워크스페이스별 타입 캔버스 배치 설정을 관리한다.

### 2.9 Event (도메인 이벤트)

- 속성: id(UUID), workspace, eventType, payload
- 도메인 변경 시 발행되어 후속 처리를 트리거한다.
- 이벤트 타입:
  - 타입 관련: TYPE_CREATED, TYPE_DELETED
  - 문서 관련: DOCUMENT_CREATED, DOCUMENT_DELETED
  - 검증 관련: VALIDATION_REQUESTED

### 2.10 Compliance (문서-스키마 호환성)

- 문서와 타입 버전 간의 호환성 검증 결과를 저장한다.
- 문서가 어떤 타입 버전들과 호환되는지, 어떤 버전과 불일치하는지를 추적한다.
- 불일치 시 구체적인 사유(어떤 속성이, 어떤 규칙에 위배되는지)를 포함한다.

### 2.11 Validation Task (검증 작업)

- 검증 실행의 워크플로우 상태를 추적한다.
- 상태: NEW → PROCESSING → DONE / FAILED
- 완료 시 Compliance 결과를 생성한다.

## 3. 기능 요구사항

### 3.1 워크스페이스 관리

- 워크스페이스를 생성할 수 있다.
- 생성 시 Admin 그룹이 자동 생성되고, 생성자가 자동 배정된다.
- 워크스페이스 삭제 시 관련 데이터가 종속 삭제(cascade)된다.
- 기존 워크스페이스에 조인할 수 있다.
- 로그인 후 참여 중인 워크스페이스가 있으면 마지막 액션을 취한 워크스페이스로 자동 진입한다.
- 참여 중인 워크스페이스가 없으면 워크스페이스 생성 또는 조인 화면으로 이동한다.
  - Shell 이 `WorkspaceList` 빈 방출을 감지하면 가상 onboarding 메뉴를 `MenuSelected` 에 1회 push 하여 `workspace-ui` 모듈(`workspace.nocache.js`)을 자동 주입한다 (MenuRail/MobileTabs 에는 비노출). 자세한 흐름은 UC-12 참조.
- 언제든 다른 워크스페이스로 전환할 수 있다.
- 같은 워크스페이스에 여러 사용자가 동시에 참여하여 실시간으로 협업할 수 있다.
  - 다른 사용자의 문서 저장, 타입 변경 등이 SSE 이벤트로 즉시 반영된다.
  - AI 에이전트의 액션도 동일한 이벤트 스트림으로 전달되어, 모든 참여자가 에이전트의 행동을 실시간으로 관찰할 수 있다.
  - **프레즌스**: 다른 사용자가 편집 중인 셀/타입 박스를 실시간으로 표시한다 (사용자별 고유 색상 보더 + 이름 라벨). 200ms 디바운스, 30초 타임아웃.

### 3.2 사용자 및 그룹 관리

- 사용자를 워크스페이스의 그룹에 배정할 수 있다.
- 그룹을 생성·삭제할 수 있다.
- 그룹에 역할(Role)을 부여하여 권한을 제어한다.

### 3.3 RBAC (역할 기반 접근 제어)

- 권한은 `리소스:동작` 형식의 Permission으로 관리한다.
- 와일드카드를 지원한다 (예: `type:*:view`).

#### Permission 목록

| Permission                                | 설명           |
|-------------------------------------------|----------------|
| `system:audit-logs`                       | 시스템 감사 로그 조회 |
| `{workspace}:role:assign`                 | 역할 부여       |
| `{workspace}:group:create/delete/view`    | 그룹 관리       |
| `{workspace}:user:assign/view`            | 사용자 배정/조회  |
| `{workspace}:type:create/delete`          | 타입 생성/삭제   |
| `{workspace}:type:{type}:view/edit`       | 특정 타입 조회/편집 |
| `{workspace}:type:{type}:document:view/edit` | 문서 조회/편집 |
| `{workspace}:type:{type}:attribute:{attr}:read/write` | 속성 필드 레벨 조회/편집 (3.20) |

#### Role 계층

- **System Role**: ADMIN (전체 시스템 접근)
- **Workspace Role**: ADMIN, GROUP_MANAGER, USER_MANAGER, TYPE_MANAGER, VIEWER, USER
- **Type Role**: MANAGER, VIEWER
- **Document Role**: MANAGER, VIEWER

### 3.4 타입(Type) 관리

- 워크스페이스 내에서 문서 타입(스키마)을 정의할 수 있다.
- 타입은 속성(Attribute)의 집합으로 구성된다.
- 타입 변경 시 기존 버전은 보존되고 새 버전이 생성된다 (불변 이력).
- 계층 구조(parent)를 통한 타입 상속을 지원한다.
- primitive 플래그로 기본 타입을 표시할 수 있다.
- 타입별로 접근 권한을 별도로 설정할 수 있다.
- 일괄(batch) 처리를 지원한다.
- **패치 기반 저장**: 변경된 속성만 서버에 전송하여 부분 업데이트한다. 두 사용자가 같은 타입의 서로 다른 속성을 동시에 수정해도 충돌 없이 병합된다.
  - 속성: 변경된 속성만 개별 upsert (전체 삭제-재삽입 대신)
  - `@Version`으로 타입 레벨 동시성 보장, 속성 레벨에서는 비충돌 병합
- **rev 필드 전파**: 타입의 `rev`(DB 버전) 값이 도메인 → API 응답 → 프론트엔드까지 전달되어, 클라이언트가 패치 기반 낙관적 잠금에 활용할 수 있다.

### 3.5 타입 시각화 (Type Layout)

- 캔버스 위에 타입을 배치하여 시각적으로 관계를 표현한다.
- 타입별 위치(x, y)와 크기(width, height)를 관리한다.
- 레이아웃을 저장·전환할 수 있다.
- 타입의 도메인 데이터와 시각화 데이터는 독립적으로 관리된다.

### 3.6 문서(Document) 관리

- 정의된 타입에 따라 문서를 생성·조회할 수 있다.
- 문서 변경 시 기존 버전은 보존되고 새 버전이 생성된다 (불변 이력).
- serial 기반으로 문서를 식별한다 (타입·워크스페이스 내 고유).
- 일괄(batch) 처리를 지원한다.
- 문서 저장 시 검증(validation)이 자동으로 트리거된다.
- 검색 시 페이지네이션, 정렬, 필터링을 지원한다.
- 다양한 날짜 포맷을 지원한다 (ISO-8601, yyyyMMdd, yyyy.MM.dd 등).
- **더티 트래킹**: 사용자 편집은 로컬 상태에만 반영되며, 서버에 즉시 저장되지 않는다.
- **원자적 저장**: Save 버튼을 누르면 생성/수정/삭제 변경점이 하나의 트랜잭션으로 일괄 저장된다.
- **시각적 상태 구분**: 생성(created), 수정(changed), 삭제 예정(deleted) 셀/행을 색상과 스타일로 구분하여 편집 중에 변경 내역을 즉시 확인할 수 있다.
- **충돌 감지**: 다른 사용자가 같은 문서를 동시에 수정한 경우 `@Version` 기반 낙관적 잠금으로 충돌을 감지하고, 409 Conflict 시 사용자에게 알린다.
- **패치 기반 저장**: 변경된 필드만 서버에 전송하여 부분 업데이트한다. 두 사용자가 같은 문서의 서로 다른 속성을 동시에 수정해도 충돌 없이 병합된다.
  - 문서: ChangeTracker가 추적한 변경 필드만 PATCH로 전송 → 백엔드에서 JSONB 머지 (`||` 연산자)
  - 타입: 변경된 속성만 개별 업데이트 (전체 삭제-재삽입 대신 upsert)
  - `@Version`으로 문서/타입 레벨 동시성 보장, 필드 레벨에서는 비충돌 병합
- **rev 필드 전파**: 문서의 `rev`(DB 버전) 값이 도메인 → API 응답 → 프론트엔드까지 전달되어, 클라이언트가 패치 기반 낙관적 잠금에 활용할 수 있다.

### 3.7 이력 조회

- 타입의 과거 버전을 조회할 수 있다 (특정 시점 기준 point-in-time query).
- 문서의 변경 이력을 시간 기반으로 추적할 수 있다.
- 이전 버전의 타입/문서는 삭제되지 않고 보존된다.
- **버전 간 Diff 비교**: 두 버전의 타입 또는 문서를 비교하여 변경된 속성/필드를 "before → after" 형식으로 시각적으로 표시한다.
  - 타입 diff: 속성 추가/삭제/변경, description 변경, 부모 타입 변경을 표시
  - 문서 diff: data 필드 값 변경을 표시
  - DiffPanel 컴포넌트(ui-components)를 사용하여 MD3 카드 형식으로 렌더링

### 3.8 인증

- JWT 토큰 기반 인증을 사용한다.
- 쿠키에서 JWT를 추출하여 인증 객체로 변환한다.
- RSA 공개키(PEM 형식)로 토큰 서명을 검증한다.
- 토큰 만료 시 쿠키를 자동 삭제한다.
- 토큰 갱신(refresh) 엔드포인트를 제공한다.

### 3.9 이벤트 처리

- 도메인 변경 시 이벤트를 발행한다.
- Kafka를 통한 이벤트 스트리밍을 지원한다.
- SSE(Server-Sent Events)를 통해 클라이언트에 실시간 알림을 전달한다.
- 워크스페이스 단위로 이벤트를 필터링한다.
- Keep-alive ping을 통해 HTTP/1.1 연결을 유지한다.
- 워크스페이스별 Sink를 lazy 생성하고, 모든 구독자가 해제되면 자동 정리한다.
- 구독자 등록/해제와 Sink 생성/제거는 원자적으로 처리하여 경합 조건을 방지한다.

### 3.10 검증(Validation) — 정합성 유지

- 문서 변경 시 이벤트 기반으로 비동기 검증을 수행한다.
- **타입 새 버전 생성 시 해당 타입의 기존 문서를 새 스키마 기준으로 재검증한다.**
- 속성별 Validator 규칙(Regex, Number, Date, Bool, Enum)을 적용한다.
- 타입 간 관계(parent, Document 참조)의 정합성도 검증 대상에 포함한다.
- 검증 결과를 Compliance로 저장하여, 문서가 호환되는 타입 버전을 추적한다.
- 검증 실패 시 사용자에게 경고를 노출하고, 기존 데이터의 사후 보정을 유도한다 (변경을 차단하지 않는다).
- Kafka 기반의 큐 처리를 지원한다.

### 3.11 웹 UI

#### Shell (애플리케이션 프레임)

- **Navigation Drawer**: Menu Rail과 Tool Rail을 포함하는 접이식 패널. Collapse/Expand/Hide 상태를 가진다.
- **Menu Rail**: 서비스에서 수집된 메뉴 목록을 정렬하여 표시. 메뉴 선택 시 해당 모듈 스크립트를 동적 로딩한다.
- **Tool Rail**: 선택된 메뉴의 도구(Tool) 목록 표시. 도구 선택 시 도구 함수를 실행한다.
- **Frame**: 메인 콘텐츠 영역. 모듈 전환 시 fade-in/out 애니메이션을 적용한다.
- **프로그레스 바**: API 호출 등 비동기 작업 진행 상태를 표시한다.
- **워크스페이스 선택기**: Drawer 내 셀렉트 박스로, 언제든 다른 워크스페이스로 전환할 수 있다.
- 클린 URL(Clean URL) 기반 라우팅으로 메뉴를 자동 선택한다 (정규식 매칭). 해시(#)를 사용하지 않는 HTML5 History API를 활용한다.
- 브라우저 뒤로가기/앞으로가기를 지원한다.
- 모듈 스크립트를 동적으로 주입하여 activity 모듈을 로딩한다.
- 사용자 정보를 주기적으로 갱신한다 (토큰 리프레시).

#### 로그인·홈

- 미인증 사용자가 접속하면 로그인 페이지(`/auth/login`)로 리다이렉트한다.
- 로그인/로그아웃 페이지를 제공한다.
- 자동 토큰 갱신을 지원한다.
- 세션 만료 시 로그인 페이지로 리다이렉트한다.
- 로그인 후 참여 중인 워크스페이스가 있으면 마지막 액션 워크스페이스로 자동 진입한다.
- 참여 중인 워크스페이스가 없으면 워크스페이스 생성/조인 화면을 표시한다.

#### 타입 관리 (Type Editor)

- 타입의 속성(Attribute)을 추가·삭제하고 Validator를 설정할 수 있다.
- Undo/Redo를 지원한다.

#### 타입 시각화 (Type Canvas)

- 캔버스 기반 시각적 타입 편집기를 제공한다.
- 타입을 캔버스에 추가·삭제·이동할 수 있다.
- 레이아웃을 저장·전환할 수 있다.
- 드래그 앤 드롭, 키보드 네비게이션, 컨텍스트 메뉴를 지원한다.

#### 문서 관리 (Document Editor)

- 탭 기반 인터페이스로 여러 타입의 문서를 동시에 편집할 수 있다.
- 스프레드시트 형태의 테이블 뷰를 제공한다 (Handsontable 6.2.4 MIT).
- 도구 모음: 저장, Undo/Redo, 추가, 삭제, 새로고침, 검증
- 검색 필터링을 지원한다.
- 일괄(batch) 작업을 지원한다.
- 검증 실패(Compliance 불일치) 문서에 대한 경고를 표시한다.
- **더티 트래킹**: 생성/수정/삭제 상태를 셀 단위로 시각적으로 구분한다.
  - 생성된 행: tertiary-container 배경, 좌측 3px tertiary 보더
  - 수정된 셀: tertiary 1px inset box-shadow
  - 삭제 예정 행: 취소선, 75% 투명화
  - 유효하지 않은 셀: error 텍스트, error 1px inset box-shadow (필수값 누락/형식 오류)
  - 충돌 문서: secondary-container 배경, secondary 2px 좌측 보더 (다른 사용자와 동시 수정)
- **Save 버튼**: 더티 없으면 비활성화, 변경 건수 뱃지 표시, 저장 중 스피너 표시
- **타입 전환 경고**: 미저장 변경이 있으면 확인 다이얼로그 표시
- 에이전트 편집과 사용자 편집은 동일한 Action/DirtyTracker 경로로 처리되며, Undo/Redo 가능

#### 사용자 관리

- 현재 사용자 정보를 표시한다.
- 소속 워크스페이스 목록을 표시한다.

## 4. API 엔드포인트

| Method | Path                                        | 설명                          |
|--------|---------------------------------------------|-------------------------------|
| GET    | `/auth/refresh`                             | JWT 토큰 갱신                  |
| GET    | `/oauth2/authorization/{provider}`          | OAuth2 로그인 시작 (리다이렉트)  |
| GET    | `/login/oauth2/code/{provider}`             | OAuth2 콜백 (인가 코드 수신)     |
| GET    | `/menus`                                    | 메뉴 목록 조회 (서비스 집계)     |
| GET    | `/user`                                     | 현재 사용자 정보 조회            |
| POST   | `/workspaces`                                | 워크스페이스 생성               |
| GET    | `/workspaces/{workspace}/types`              | 타입 목록 조회 (날짜 필터링)     |
| GET    | `/workspaces/{workspace}/types/{type}?version=` | 특정 타입 버전 조회           |
| PUT    | `/workspaces/{workspace}/types`              | 타입 일괄 저장 (새 버전 생성)    |
| PATCH  | `/workspaces/{workspace}/types`              | 타입 부분 업데이트 (변경 속성만) |
| DELETE | `/workspaces/{workspace}/types`              | 타입 일괄 삭제                  |
| GET    | `/workspaces/{workspace}/types/{type}/diff?v1=&v2=` | 타입 두 버전 간 diff       |
| GET    | `/workspaces/{workspace}/documents`          | 문서 검색 (페이지네이션)         |
| GET    | `/workspaces/{workspace}/{type}/{serial}`    | 특정 문서 조회                  |
| GET    | `/workspaces/{workspace}/{type}/{serial}?date=` | 특정 시점 문서 조회 (이력)    |
| PUT    | `/workspaces/{workspace}/documents`          | 문서 일괄 저장 (새 버전 생성)    |
| PATCH  | `/workspaces/{workspace}/documents`          | 문서 부분 업데이트 (변경 필드만) |
| DELETE | `/workspaces/{workspace}/documents`          | 문서 일괄 삭제                  |
| GET    | `/workspaces/{workspace}/{type}/{serial}/diff?date1=&date2=` | 문서 두 시점 간 diff |
| GET    | `/workspaces/{workspace}/compliance`         | 호환성 검증 결과 조회           |
| GET    | `/workspaces/{workspace}/layouts`            | 레이아웃 목록 조회              |
| GET    | `/workspaces/{workspace}/messages`           | SSE 실시간 이벤트 스트림        |
| POST   | `/workspaces/{workspace}/presence`           | 프레즌스 (편집 중 셀/타입 공유)  |
| POST   | `/workspaces/{workspace}/documents/import`   | 문서 일괄 임포트 (CSV/JSON)     |
| GET    | `/workspaces/{workspace}/documents/export`   | 문서 일괄 익스포트 (CSV/JSON)   |
| GET    | `/workspaces/{workspace}/dashboard`          | 워크스페이스 현황 대시보드       |
| GET    | `/workspaces/{workspace}/audit-logs`         | 감사 로그 조회                  |
| GET    | `/openapi.json`                             | OpenAPI 3.0 스펙               |
| POST   | `/assistant/request`                        | 자연어 요청 → 실행 계획 생성     |
| POST   | `/assistant/execute`                        | 실행 계획 확인 후 실행 (Kafka 발행) |
| POST   | `/assistant/abort`                          | 에이전트 작업 중단 (executionId 지정)  |
| POST   | `/assistant/respond`                        | 에이전트 확인 요청에 대한 사용자 응답    |
| GET    | `/assistant/executions`                     | 실행 상태/진행률 조회 (워크스페이스별)   |
| GET    | `/assistant/artifacts`                      | 실행 결과 아티팩트 조회                 |
| PATCH  | `/workspaces/{workspace}/documents/{id}/status` | 문서 상태 전이 (DRAFT/REVIEW/PUBLISHED) |
| POST   | `/workspaces/{workspace}/webhooks`           | 웹훅 등록                               |
| GET    | `/workspaces/{workspace}/webhooks`           | 웹훅 목록 조회                           |
| DELETE | `/workspaces/{workspace}/webhooks/{id}`      | 웹훅 삭제                               |
| GET    | `/workspaces/{workspace}/stats/timeline?from=&to=&interval=` | 시계열 통계 조회              |
| GET    | `/workspaces/{workspace}/stats/distribution` | 타입별 문서 분포 조회                    |
| GET    | `/`                                         | SEO 랜딩 (ko, §3.22.2)                   |
| GET    | `/en/`                                      | SEO 랜딩 (en, §3.22.2)                   |
| GET    | `/sitemap.xml`                              | 사이트맵 (§3.22.2)                       |
| GET    | `/robots.txt`                               | 크롤러 지시 (§3.22.2)                    |
| GET    | `/llms.txt`                                 | AI 에이전트 디스커버리 요약 (§3.23.1)     |
| GET    | `/llms-full.txt`                            | AI 에이전트 디스커버리 상세 (§3.23.1)     |
| GET    | `/app.html`                                 | 앱 셸 진입점 (`noindex, follow`)         |

### 3.12 API 접근성 (외부 시스템·AI 연동)

#### OpenAPI 스펙

- 모든 REST API에 대해 OpenAPI 3.0 스펙을 자동 생성하여 제공한다.
- 외부 시스템이나 AI 에이전트가 API를 자기 발견(self-discovery)할 수 있도록 한다.

#### 인증 방식 다양화

- 기존 쿠키 기반 JWT 인증 외에 **API Key** 또는 **Bearer Token** 방식의 인증을 추가로 지원한다.
- 프로그래밍 방식 접근(스크립트, AI 에이전트, CI/CD)에 적합한 인증 수단을 제공한다.

#### 벌크 데이터 처리

- CSV/JSON 파일을 통한 문서 일괄 임포트를 지원한다.
- 임포트 시 타입 스키마에 맞게 자동 매핑을 시도하고, 매핑 실패 항목은 사용자에게 확인을 요청한다.
- 문서를 CSV/JSON 형식으로 일괄 익스포트할 수 있다.

### 3.13 사용성 (비개발자 지원)

#### 가이드드 워크플로우

- 타입(스키마) 생성 시 자연어 기반 안내를 제공한다 (예: "어떤 정보를 관리하시겠습니까?" → 속성 자동 제안).
- 정합성 검증 실패 시 구체적인 보정 방법을 안내한다 (어떤 문서의 어떤 필드를 어떻게 수정해야 하는지).

#### 검색·필터링

- 문서 및 타입에 대한 전문 검색(full-text search)을 지원한다.
- 날짜 범위, 상태, 타입별 필터링을 조합하여 사용할 수 있다.

#### 알림·대시보드

- 정합성 검증 실패, 워크스페이스 초대 등 주요 이벤트에 대한 알림을 제공한다.
- 워크스페이스 현황 대시보드를 제공한다 (타입 수, 문서 수, 검증 상태 요약).

#### 감사 로그

- 누가, 언제, 어떤 리소스를 변경했는지 감사 로그를 기록하고 조회할 수 있다.
- 감사 로그는 불변으로 보존한다.

### 3.14 모바일 지원

- 반응형 레이아웃으로 모바일 및 태블릿 화면에서도 사용할 수 있다.
- Navigation Drawer 의 사이드바(워크스페이스 셀렉터 등 secondary UI)는 모바일에서 왼쪽 가장자리 스와이프로 여는 오버레이 모드로 동작한다.
- **하단 네비게이션 바는 단일 컨텍스트로 MenuRail ↔ ToolRail 을 드릴인 스왑한다**:
  - 초기에는 MenuRail 이 하단 네비게이션 바 자리를 차지한다 (메뉴 아이콘 최대 5개).
  - 도구가 2개 이상인 메뉴를 탭하면 같은 자리가 ToolRail 로 교체되며, 첫 아이템으로 ← (back) 버튼이 표시된다.
  - ← 버튼을 탭하면 메뉴 선택이 해제되고 MenuRail 이 복귀한다.
  - 도구가 1개뿐인 메뉴를 탭하면 드릴인 없이 바로 해당 도구로 이동하고 MenuRail 은 유지된다.
- 하단 바 전환 시에는 MD3 "emphasized decelerate" 커브(300ms)로 아래에서 위로 slide-up 된다.
- 스프레드시트(문서 편집기)는 모바일에서 카드 뷰로 전환하여 가독성을 확보한다.
- 타입 캔버스는 핀치 줌, 터치 드래그를 지원한다.
- 에이전트 입력창은 모바일 키보드와 호환되도록 하단 고정 배치한다.
- PWA(Progressive Web App)를 지원하여 홈 화면에 추가, 오프라인 캐싱을 제공한다.
- 최소 지원 뷰포트 너비는 360px이다.

### 3.15 대시보드

워크스페이스의 현황을 한눈에 파악할 수 있는 대시보드를 제공한다.

#### 워크스페이스 현황

- 타입 수, 문서 수, 사용자 수 등 기본 통계를 표시한다.
- 최근 변경 이력 (최근 N건의 생성/수정/삭제)을 타임라인으로 표시한다.
- 검증 상태 요약: 정합성 검증 성공/실패/미검증 비율을 표시한다.

#### 데이터 품질 현황

- 데이터 품질 감시 결과를 시각화한다 (결측치, 중복, 이상값 건수).
- 타입별 품질 점수를 차트로 표시한다.
- 품질 이슈 목록을 심각도별로 분류하여 표시하고, 클릭 시 해당 문서로 이동한다.

#### 에이전트 활동 로그

- AI 에이전트의 최근 활동 내역을 표시한다 (요청, 실행, 완료, 오류).
- 에이전트가 발행한 AGENT_COMMAND 이벤트를 시간순으로 표시한다.
- 에이전트 사용 빈도 및 처리 시간 통계를 제공한다.

#### 접근 방식

- 대시보드는 별도 GWT 모듈(`dashboard-ui`)로 구현되었으며, 메뉴에서 선택하여 접근한다.
- `DashboardApi`가 FetchApi를 통해 `dashboard/stats`, `dashboard/quality-issues`, `dashboard/agent-activity` 엔드포인트에서 데이터를 조회한다.
- `StatsProvider`, `QualityIssueList`, `AgentActivityList`가 BehaviorSubject로 상태를 관리하여 UI를 반응적으로 갱신한다.

#### 구현 현황

| 기능 | 상태 | 구현체 |
|------|------|--------|
| 통계 카드 (타입 수, 문서 수, 사용자 수) | 구현 완료 | `StatsCardElement` |
| 품질 이슈 목록 (심각도 배지) | 구현 완료 | `QualityPanelElement` |
| 에이전트 활동 타임라인 | 구현 완료 | `ActivityLogElement` |
| 품질 점수 차트 | 미구현 | - |
| 최근 변경 타임라인 | 미구현 | - |
| 실시간 SSE 카운터 갱신 | 미구현 | - |
| 문서 생성 추이 차트 | 미구현 | `TimelineChartElement` (3.21) |
| 검증 실패율 추이 차트 | 미구현 | `TimelineChartElement` (3.21) |
| 타입별 문서 분포 파이 차트 | 미구현 | `DistributionChartElement` (3.21) |
| 에이전트 사용량 추이 차트 | 미구현 | `TimelineChartElement` (3.21) |

### 3.16 데이터 품질 감시 (AI 에이전트)


사후 검증을 넘어 시스템 레벨에서 데이터의 품질을 선제적으로 감시한다.
AI 에이전트가 백그라운드에서 워크스페이스 내 데이터의 패턴을 분석하여 이상을 감지하고, 사용자에게 알린다.

#### 결측치 감지

- 필수 속성(nullable=false)이 비어 있거나 기본값으로만 채워진 문서를 탐지한다.
- 참조 타입 속성이 존재하지 않는 대상을 가리키는 경우를 탐지한다.
- 감지 결과를 AGENT_COMMAND 이벤트(notify)로 워크스페이스에 브로드캐스트한다.

#### 중복 데이터 감지

- 동일 타입 내에서 핵심 속성 조합이 유사하거나 동일한 문서를 탐지한다.
- 유사도 임계값은 워크스페이스 설정으로 조정할 수 있다.
- 중복 의심 문서를 highlight 커맨드로 시각적으로 표시한다.

#### 비정상 데이터 감지 (이상 탐지)

- 수치형 속성에서 통계적 이상값(outlier)을 탐지한다 (예: 평균 대비 3σ 이상 편차).
- 날짜형 속성에서 비논리적 값을 탐지한다 (예: 미래 날짜의 과거 이벤트, 시작일 > 종료일).
- 텍스트형 속성에서 패턴 불일치를 탐지한다 (예: 이메일 형식, 전화번호 형식).

#### 감시 실행 방식

- 에이전트는 주기적(스케줄) 또는 이벤트 트리거(DOCUMENT_CREATED 수신 시)로 감시를 실행한다.
- 감시 결과는 Kafka AGENT_COMMAND 이벤트로 발행되어 event-broadcaster를 통해 워크스페이스 멤버에게 실시간 알림된다.
- 심각도에 따라 notify(info/warning/error) 커맨드를 차등 적용한다.
- 사용자는 에이전트에게 "품질 검사 실행"을 자연어로 요청하여 즉시 감시를 수행할 수 있다.
- `POST /assistant/quality/scan?workspace={id}`로 REST API를 통해 즉시 스캔을 트리거할 수 있다.

#### 구현 현황

| 기능 | 상태 | 구현체 |
|------|------|--------|
| 필수 필드 결측치 감지 | 구현 완료 | `DefaultQualityMonitor.checkMissingRequiredFields()` |
| 동일 시리얼 중복 감지 | 구현 완료 | `DefaultQualityMonitor.checkDuplicateSerials()` |
| 수치형 이상값 감지 (3σ) | 구현 완료 | `DefaultQualityMonitor.checkNumericAnomalies()` |
| 감시 결과 Kafka 발행 | 구현 완료 | `QualityMonitorService` → `AgentCommandEventPublisher` |
| 즉시 스캔 REST API | 구현 완료 | `QualityController` (`POST /assistant/quality/scan`) |
| 감사 추적 저장/조회 | 구현 완료 | `AuditRepository` / `AuditController` (`GET /assistant/audit`) |
| 유사도 기반 중복 감지 | 미구현 | - |
| 날짜/텍스트 패턴 이상 감지 | 미구현 | - |
| 스케줄 기반 주기적 감시 | 미구현 | - |
| 이벤트 트리거 감시 | 미구현 | - |

### 3.17 자연어 기반 변경 요청 (AI 어시스턴트)

사용자가 자연어로 요청하면 시스템이 의도를 해석하여 해당 작업을 수행한다.
비개발자가 스키마 구조나 API를 몰라도 데이터를 관리할 수 있도록 한다.

#### 대화형 워크스페이스 설계 (초기 구축)

사용자가 로그인 후 워크스페이스를 처음 만들 때, 에이전트가 대화를 통해 전체 구조를 함께 설계한다.
사용자는 "어떤 시스템이 필요한지"만 설명하면, 에이전트가 타입·속성·관계를 구성하여 제안한다.

- 사용자가 자연어로 요구사항을 설명하면 에이전트가 워크스페이스 구조(타입, 속성, 관계)를 제안한다.
  - 예: "재고 관리 시스템이 필요해. 제품, 입출고, 창고를 관리하고 싶어"
  - → 에이전트가 3개 타입(제품, 입출고, 창고) + 속성 + 타입 간 관계를 설계하여 제안
- 제안은 **타입 캔버스 미리보기**로 시각화하여 보여준다 (타입 노드 + 관계선).
- 사용자가 수정을 요청하면 반복적으로 조정한다.
  - 예: "입출고에 승인자 필드도 필요해" → 속성 추가 반영
  - 예: "제품에 카테고리가 있으면 좋겠는데, 대분류/중분류/소분류로 나눠줘" → Enum 또는 계층 구조 제안
- 최종 확인 후 타입을 일괄 생성한다.

시나리오 예시:

```
사용자: "병원 진료 기록을 관리하고 싶어"

에이전트:
  attention(coachmark) → "진료 기록 관리를 위해 다음 구조를 제안합니다"
  preview(타입 캔버스) →
    ┌──────────┐     ┌──────────┐     ┌──────────┐
    │  환자     │────▶│  진료기록  │◀────│  의사     │
    │ 이름      │     │ 진료일    │     │ 이름      │
    │ 생년월일   │     │ 증상      │     │ 전문분야   │
    │ 연락처    │     │ 진단      │     │ 면허번호   │
    └──────────┘     │ 처방      │     └──────────┘
                     └──────────┘
  await_confirm → "이 구조로 시작할까요? 수정이 필요하면 말씀해주세요."

사용자: "처방은 별도 타입으로 분리하고, 약품 목록도 관리하고 싶어"

에이전트:
  preview(타입 캔버스 갱신) →
    환자 ──▶ 진료기록 ◀── 의사
                │
                ▼
              처방 ──▶ 약품
  await_confirm → "처방 타입과 약품 타입을 분리했습니다. 이대로 생성할까요?"

사용자: "좋아, 만들어줘"

에이전트:
  mutate → 5개 타입 일괄 생성
  navigate → 타입 캔버스로 이동
  attention(spotlight) → 생성된 타입 캔버스 표시
  complete → "5개 타입이 생성되었습니다. 이제 문서를 입력하거나 스키마를 수정할 수 있습니다."
```

#### 자연어 → 스키마 변경

- 자연어 요청으로 타입(스키마)을 생성·수정할 수 있다.
  - 예: "고객 타입에 전화번호 필드 추가해줘" → 해당 타입의 새 버전에 Text(전화번호) 속성 추가
  - 예: "주문 타입 만들어줘. 주문번호, 고객, 금액, 날짜가 필요해" → 속성 4개를 가진 타입 생성
- 요청을 해석한 결과를 **미리보기**로 보여주고, 사용자가 확인한 후에 실행한다.
- 모호한 요청은 추가 질문으로 명확화한다 (예: "금액의 범위 제한이 필요한가요?").

#### 자연어 → 문서 변경

- 자연어 요청으로 문서를 생성·수정·검색할 수 있다.
  - 예: "지난달 생성된 주문 문서 보여줘" → 날짜 필터링 + 타입 필터링 검색
  - 예: "고객 C-001의 이메일을 xxx@example.com으로 변경해줘" → 해당 문서의 새 버전 생성
  - 예: "만료된 문서 중 검증 실패한 것만 보여줘" → Compliance 상태 필터링
- 변경 작업은 기존 불변 이력 모델을 따른다 (새 버전 생성).

#### 자연어 → 정합성 보정

- 정합성 검증 실패 시 자연어로 보정 방법을 안내하고, 사용자가 승인하면 자동 보정한다.
  - 예: "고객 타입에 필수 필드 '이메일'이 추가되었는데 기존 문서 50건에 값이 없습니다. 기본값으로 채울까요?"
- 일괄 보정 시 영향 범위(몇 건의 문서가 변경되는지)를 미리 보여준다.
- 보정 내역은 감사 로그에 기록한다.

#### 실행 모델

- 사용자의 자연어 요청은 **의도 해석 → 실행 계획 생성 → 미리보기 → 사용자 확인 → 실행** 단계를 거친다.
- 기술적으로 에이전트는 Kafka 이벤트(`AGENT_COMMAND`)를 발행하고, event-broadcaster를 통해 프론트엔드에 커맨드가 전달된다.
- **UX 원칙: 에이전트가 내 화면을 대신 조작해주는 느낌.** 프론트엔드는 수신한 커맨드를 즉시 시각적으로 실행하여, 사용자 눈앞에서 화면이 전환되고, 셀이 편집되고, 타입이 추가되는 과정을 보여준다.
  - `navigate` → 화면이 자연스럽게 전환되며 사용자가 이동 과정을 관찰
  - `highlight` → 대상 요소가 시각적으로 강조되며 사용자의 시선을 유도
  - `mutate` → 스프레드시트 셀이 하나씩 채워지거나 타입 노드가 캔버스에 등장하는 애니메이션
  - `preview` → 변경 전후 diff가 인라인으로 표시되어 사용자가 검토
  - `await_confirm` → 에이전트가 잠시 멈추고 사용자에게 선택지를 제시
- 사용자가 에이전트의 모든 행동을 실시간으로 관찰하고, 언제든 Undo/Redo로 되돌리거나 중단(abort)할 수 있다.
- Assistant는 DB에 직접 접근하지 않으며, 독립 서비스로 수평 확장이 가능하다.
- 워크스페이스별 권한을 그대로 적용한다 (자연어 요청이라도 권한 밖의 작업은 거부).

#### 병렬 단계 실행 (Parallel Step Execution)

- `ExecutionStep`에 `group: Int` 필드를 추가한다. 같은 group 값을 가진 단계들은 동시(병렬)에 실행되고, 서로 다른 group 간에는 순차적으로 실행된다.
- `SequentialPlanExecutor`를 `GroupedPlanExecutor`로 교체한다. 같은 그룹의 단계는 `Flux.merge`로 병렬 실행한다.
- `PROGRESS` 커맨드 페이로드에 그룹 정보를 포함한다: `{ currentGroup, totalGroups, parallel, stepCount }`.
- 병렬 실행 중 한 단계가 실패하면 같은 그룹의 나머지 단계도 취소한다.

#### 다중 실행 및 실행 컨텍스트 (Multi-Execution)

- `AssistantService`는 `ConcurrentHashMap<UUID, ExecutionContext>`로 복수의 동시 실행을 관리한다.
- 각 실행은 고유한 `executionId`, 감사 추적(`AuditEntry`), 응답 sink(`Sinks.One`)를 가진다.
- `request()` 메서드는 `ExecutionRequest(executionId, plan)`을 반환하여 클라이언트가 실행을 식별할 수 있게 한다.
- `execute()`, `respond()`, `abort()` 메서드는 `executionId`를 매개변수로 받아 특정 실행을 대상으로 동작한다.
- `GET /assistant/executions?workspace={id}`로 워크스페이스 내 모든 실행의 상태(실행 계획, 현재 그룹, 진행률, 상태)를 조회할 수 있다.

#### 아티팩트 (Artifacts)

- 실행이 완료되면 결과를 `Artifact`로 수집한다.
- `Artifact`에는 `executionId`, `summary`(실행 결과 요약), `changes`(변경 목록: `{ type, target, description }`), `timestamp`가 포함된다.
- `COMPLETE` 커맨드 페이로드에 artifact summary를 포함하여 클라이언트에 전달한다.
- `AuditEntry`에 `artifact` 필드를 확장하여 아티팩트를 감사 추적과 함께 보존한다.
- `GET /assistant/artifacts?workspace={id}`로 워크스페이스의 실행 결과 아티팩트를 조회할 수 있다.

#### 에이전트 감사 추적 (Audit Trail)

에이전트의 모든 동작은 근거가 있어야 하며, 사후 감사가 가능해야 한다.

- **의도 근거 기록**: 에이전트가 실행 계획을 생성할 때, 원본 사용자 메시지와 LLM의 해석 결과(intent, confidence)를 함께 저장한다.
- **커맨드별 사유**: 각 AGENT_COMMAND 이벤트에 `description` 필드로 해당 동작의 이유를 기록한다 (예: "사용자가 '이메일 필드 추가' 요청 → 고객 타입에 Text(email) 속성 추가").
- **이벤트 불변 로그**: 모든 AGENT_COMMAND 이벤트는 Kafka에 발행되어 이벤트 로그에 영구 보존된다. 삭제·수정이 불가능한 불변 이력이다.
- **감사 조회**: 워크스페이스 관리자는 에이전트의 과거 행동을 시간순으로 조회할 수 있다 — 누가, 언제, 어떤 요청으로, 어떤 커맨드가 실행되었는지 추적 가능.
- **실행 계획 보존**: 사용자가 확인(confirm)한 실행 계획 전체를 저장하여, "왜 이 변경이 발생했는가"를 사후에 확인할 수 있다.

> **협업 모드 vs 감시 모드**
>
> - **협업 모드** (UC-A1~A4): 사용자 요청 기반. 프론트엔드에서 커맨드를 시각적으로 실행하여 "동료가 화면을 조작해주는 느낌"을 제공한다. 사용자가 실시간 관찰·개입·Undo 가능.
> - **감시 모드** (UC-A5, 3.16 데이터 품질 감시): 백그라운드 서버에서 자동 실행. 결과만 `notify` 커맨드로 알리며, 사용자 화면을 직접 조작하지 않는다.

#### 에이전트 → UI 제어 프로토콜

백엔드 에이전트가 프론트엔드 UI를 단계별로 조작할 수 있는 스트리밍 프로토콜을 정의한다.
에이전트가 작업을 수행하면서 변경사항을 실시간으로 UI에 반영하여, 사용자가 진행 과정을 시각적으로 확인할 수 있다.

##### 전송 방식

- 에이전트는 UI 커맨드를 **Kafka 이벤트** (`AGENT_COMMAND`)로 발행한다.
- `event-broadcaster`가 Kafka 이벤트를 수신하여 워크스페이스별 SSE 스트림(`/workspaces/{id}/messages`)으로 브로드캐스트한다.
- 같은 워크스페이스의 **모든 멤버**가 에이전트 액션을 실시간으로 관찰할 수 있다.
- 에이전트는 제3의 협업자로서 다른 사용자의 변경 이벤트(DOCUMENT_CREATED, TYPE_CREATED)와 동일한 채널로 행동한다.
- 에이전트가 사용자 확인을 기다리는 구간에서는 `await_confirm` 커맨드로 스트림을 일시 정지한다.
- 사용자 응답은 `POST /assistant/respond`로 전달하며, 에이전트가 다음 커맨드를 발행하여 재개한다.

##### 커맨드 타입

```
navigate      — 특정 화면으로 이동 (메뉴/도구 선택, URL 변경)
highlight     — 특정 요소를 강조 (필드, 행, 타입 노드)
scroll        — 특정 위치로 스크롤/포커스 이동
preview       — 변경 전후 diff를 인라인으로 표시
mutate        — 실제 값 변경 (필드 입력, 행 추가/삭제)
notify        — 토스트/배너 메시지 표시 (정보, 경고, 에러)
progress      — 진행률 표시 (일괄 작업 시)
attention     — 사용자에게 특정 영역을 안내 (코치마크, 툴팁, 오버레이)
await_confirm — 사용자 확인 대기 (계속/취소/수정 선택지 제공)
complete      — 작업 완료 (요약 표시)
```

##### 커맨드 메시지 구조

```json
{
  "seq": 1,
  "type": "navigate",
  "target": { "menu": "type-editor", "tool": "customer" },
  "description": "고객 타입 편집기로 이동합니다"
}
```
```json
{
  "seq": 2,
  "type": "highlight",
  "target": { "selector": "[data-attribute='email']" },
  "style": "pulse",
  "description": "이메일 필드를 찾았습니다"
}
```
```json
{
  "seq": 3,
  "type": "preview",
  "changes": [
    { "path": "attributes[4]", "op": "add", "value": { "name": "phone", "type": "Text" } }
  ],
  "description": "전화번호 필드를 추가합니다"
}
```
```json
{
  "seq": 4,
  "type": "await_confirm",
  "options": ["confirm", "cancel", "edit"],
  "description": "이대로 적용할까요?"
}
```
```json
{
  "seq": 5,
  "type": "mutate",
  "changes": [
    { "path": "attributes[4]", "op": "add", "value": { "name": "phone", "type": "Text" } }
  ],
  "description": "전화번호 필드를 추가했습니다"
}
```
```json
{
  "seq": 6,
  "type": "complete",
  "summary": "고객 타입 v3이 생성되었습니다. 전화번호(Text) 필드가 추가되었습니다.",
  "affectedResources": [{ "type": "Type", "id": "customer", "version": "v3" }]
}
```

##### attention 커맨드 (UI 안내)

`attention`은 사용자에게 "화면의 이 부분을 봐라"고 안내하는 커맨드다. `highlight`가 단순 강조(깜빡임)라면, `attention`은 **설명을 동반한 안내**이다.

```json
{
  "seq": 3,
  "type": "attention",
  "target": { "selector": "#compliance-panel .warning-badge" },
  "style": "coachmark",
  "message": "검증 실패한 문서가 3건 있습니다. 여기서 상세 내역을 확인할 수 있습니다.",
  "position": "bottom",
  "dismissable": true
}
```

지원하는 스타일:

| 스타일 | 설명 | 용도 |
|--------|------|------|
| `coachmark` | 대상 요소 주변에 말풍선 + 반투명 오버레이 | 기능 안내, 온보딩 |
| `spotlight` | 대상만 밝게, 나머지 어둡게 (스포트라이트) | 주의 집중 |
| `pulse` | 대상 요소 테두리 반복 강조 | 변경 사항 알림 |
| `arrow` | 대상을 가리키는 화살표 + 메시지 | 위치 안내 |
| `badge` | 대상 옆에 숫자/아이콘 뱃지 | 알림 카운트 |

##### 프론트엔드 처리

- Shell UI에 `AgentCommandHandler`를 두어 커맨드를 수신하고 해당 UI 컴포넌트에 위임한다.
- `navigate` → UrlBasedMenuResolver / HistoryManager 호출
- `highlight` → 대상 DOM 요소에 애니메이션 클래스 적용
- `attention` → 대상 요소에 코치마크/스포트라이트 오버레이 렌더링
- `preview` → 변경 대상 옆에 diff 오버레이 표시
- `mutate` → 실제 데이터 변경 API 호출 후 UI 갱신
- `await_confirm` → 확인 다이얼로그 표시, 사용자 응답을 에이전트에 반환
- 커맨드 실행 중 사용자가 언제든 **중단(abort)**할 수 있다.

##### 에이전트 외 활용

UI 어텐션 시스템은 에이전트 전용이 아니라 범용 메커니즘이다. 다음 상황에서도 동일한 커맨드를 사용한다.

- **온보딩 가이드**: 신규 사용자 첫 접속 시 주요 UI 요소를 순차적으로 안내
  ```
  attention(coachmark, "#workspace-select", "여기서 워크스페이스를 선택합니다")
  → attention(coachmark, ".rail .item:first", "메뉴를 선택하여 시작하세요")
  ```
- **정합성 경고**: 검증 실패 이벤트 수신 시 해당 문서/필드를 자동으로 안내
  ```
  attention(badge, "#menu-documents", "3")
  → navigate(documents, customer)
  → attention(spotlight, "tr[data-serial='C-042'] td.email", "이 필드가 새 스키마와 호환되지 않습니다")
  ```
- **협업 공유**: 사용자가 특정 위치를 다른 사용자에게 공유 ("이 부분 확인해주세요")
  ```
  attention(arrow, "[data-attribute='amount']", "김팀장: 이 금액 확인 부탁드립니다")
  ```

##### 시나리오 예시

사용자: "고객 타입에 전화번호 필드 추가해줘"

```
 1. navigate      → 타입 편집기 > 고객 타입으로 이동
 2. attention     → (spotlight) 현재 속성 목록을 안내: "현재 5개 속성이 정의되어 있습니다"
 3. preview       → 속성 목록 끝에 '전화번호(Text)' diff 표시
 4. attention     → (coachmark) diff 영역 안내: "여기에 새 필드가 추가됩니다"
 5. await_confirm → "전화번호(Text, nullable) 필드를 추가합니다. 적용할까요?"
 6. mutate        → 타입 새 버전 생성 API 호출
 7. attention     → (pulse) 추가된 필드 강조: "전화번호 필드가 추가되었습니다"
 8. notify        → "검증 작업이 시작되었습니다 (기존 문서 127건 대상)"
 9. complete      → "고객 타입 v3 생성 완료"
```

### 부록: 에이전트 기반 데이터베이스 설계 E2E 시나리오

> 이 시나리오는 **개발 경험이 없는 제조업 관리자**가 AI 에이전트의 도움만으로 생산 관리 시스템의 데이터베이스를 설계·생성·운영하는 전 과정을 보여준다. 10개 커맨드 타입이 실제 흐름에서 어떻게 사용되는지 확인할 수 있다.

---

#### Phase 1: 로그인 ~ 워크스페이스 생성

사용자가 시스템에 처음 로그인한다. 워크스페이스가 없으므로 빈 대시보드가 표시된다.

사용자: "뭘 해야 하는지 모르겠어."

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
 1   attention      (coachmark) 화면 중앙 "워크스페이스 만들기" 버튼을 가리키며:
                    "여기를 눌러 새 워크스페이스를 만들 수 있습니다.
                     또는 저에게 어떤 시스템이 필요한지 말씀해 주세요."
```

사용자: "워크스페이스가 뭔지도 잘 모르겠는데, 그냥 도와줘."

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
 2   await_confirm  "워크스페이스는 데이터를 저장하는 공간입니다.
                     어떤 업무를 관리하고 싶으신지 알려주시면
                     제가 알맞은 구조를 만들어 드리겠습니다.
                     계속할까요?"
                    [계속] [직접 만들기]
```

사용자: [계속] 선택

---

#### Phase 2: 요구사항 대화

에이전트가 사용자의 업무 요구사항을 자연어로 수집한다.

사용자: "우리 회사는 제조업이야. 생산 관리 시스템이 필요해. 원자재 입고, 생산 계획, 생산 실적, 출하까지 관리하고 싶어."

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
 3   await_confirm  "제조 생산 관리 시스템이군요. 몇 가지 확인하겠습니다.
                     원자재를 납품하는 공급업체도 함께 관리할까요?"
                    [예, 공급업체도 관리] [아니오, 원자재만]
```

사용자: [예, 공급업체도 관리] 선택

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
 4   await_confirm  "생산한 제품의 종류(완제품 목록)도 별도로 관리할까요?
                     아니면 출하 시에만 제품명을 적으면 될까요?"
                    [별도로 제품 목록 관리] [출하 시 직접 입력]
```

사용자: [별도로 제품 목록 관리] 선택

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
 5   notify         (info) "파악된 요구사항: 공급업체, 원자재, 생산계획,
                    작업지시, 생산실적, 제품, 출하 — 총 7개 영역"
```

---

#### Phase 3: 타입 구조 설계 제안

에이전트가 대화 내용을 바탕으로 타입 구조를 설계하고, 타입 캔버스에 미리보기를 표시한다.

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
 6   navigate       워크스페이스 생성 → 타입 캔버스 화면으로 이동
 7   attention      (coachmark) 타입 캔버스 영역:
                    "여기에 데이터 구조를 시각적으로 설계합니다.
                     지금부터 제안드리는 구조를 미리보기로 보여드릴게요."
 8   preview        타입 캔버스에 7개 타입 미리보기 표시:
                    ┌─────────────┐     ┌──────────────┐
                    │ 공급업체     │────▶│ 원자재        │
                    │ ─────────── │     │ ──────────── │
                    │ 업체명  Text │     │ 품명    Text  │
                    │ 연락처  Text │     │ 규격    Text  │
                    │ 주소    Text │     │ 단위    Text  │
                    └─────────────┘     │ 단가  Number  │
                                        │ 공급업체  Ref  │
                                        └──────┬───────┘
                                               │
                    ┌──────────────┐    ┌───────▼──────┐
                    │ 생산계획      │    │ 작업지시      │
                    │ ──────────── │    │ ──────────── │
                    │ 계획일  Date  │    │ 지시일  Date  │
                    │ 제품    Ref   │    │ 생산계획 Ref  │
                    │ 목표수량 Num  │    │ 라인    Text  │
                    │ 상태   Text   │    │ 목표수량 Num  │
                    └──────────────┘    └───────┬──────┘
                                                │
                    ┌──────────────┐    ┌────────▼─────┐
                    │ 제품          │    │ 생산실적      │
                    │ ──────────── │    │ ──────────── │
                    │ 제품명  Text  │    │ 작업지시 Ref  │
                    │ 규격    Text  │    │ 생산일  Date  │
                    │ 단위    Text  │    │ 생산수량 Num  │
                    └──────┬───────┘    └──────────────┘
                           │
                    ┌──────▼───────┐
                    │ 출하          │
                    │ ──────────── │
                    │ 출하일  Date  │
                    │ 제품    Ref   │
                    │ 수량   Number │
                    │ 거래처  Text  │
                    └──────────────┘
 9   scroll         캔버스를 전체 보기(fit-to-view)로 조정
10   highlight      7개 타입 노드를 순서대로 깜빡여 전체 구조 강조
11   attention      (coachmark) 공급업체 → 원자재 연결선:
                    "공급업체와 원자재가 참조(Ref)로 연결됩니다.
                     하나의 공급업체가 여러 원자재를 납품할 수 있어요."
12   attention      (coachmark) 생산계획 → 작업지시 → 생산실적 흐름:
                    "생산계획을 세우면 작업지시가 나가고,
                     현장에서 생산실적을 기록하는 흐름입니다."
13   await_confirm  "이 구조로 시작할까요? 수정이 필요하면 말씀해 주세요."
                    [이대로 생성] [수정 필요]
```

---

#### Phase 4: 사용자 피드백 반영

사용자가 추가 요구사항을 전달하고, 에이전트가 구조를 수정한다.

사용자: "품질검사도 필요해. 그리고 원자재에 LOT번호 관리가 되어야 해."

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
14   preview        타입 캔버스 갱신:
                    - [추가] 품질검사 타입 (검사일 Date, 작업지시 Ref,
                      검사항목 Text, 판정 Text, 비고 Text)
                    - [변경] 원자재 타입에 'LOT번호 Text' 필드 추가
                    (diff: 추가된 타입은 녹색, 변경된 필드는 파란색)
15   scroll         새로 추가된 '품질검사' 타입 노드로 포커스 이동
16   highlight      품질검사 타입 노드 및 원자재의 LOT번호 필드 강조
17   attention      (coachmark) 품질검사 타입:
                    "작업지시별로 품질검사를 기록할 수 있도록 추가했습니다.
                     판정 필드에서 합격/불합격을 관리합니다."
18   await_confirm  "변경 사항을 확인해주세요:
                     1) 품질검사 타입 신규 추가
                     2) 원자재에 LOT번호 필드 추가
                     적용할까요?"
                    [적용] [추가 수정]
```

사용자: "작업지시에 담당자 배정 기능도 넣어줘."

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
19   preview        타입 캔버스 갱신:
                    - [변경] 작업지시 타입에 '담당자 Text' 필드 추가
                    (diff: 추가된 필드 파란색 표시)
20   highlight      작업지시 타입의 '담당자' 필드 강조
21   await_confirm  "작업지시 타입에 '담당자' 필드를 추가했습니다.
                     이제 최종 구조를 확정할까요?"
                    [최종 확정] [추가 수정]
```

사용자: [최종 확정] 선택

---

#### Phase 5: 최종 확인 및 생성

에이전트가 최종 구조를 요약하고, 사용자 확인 후 실제 타입을 생성한다.

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
22   preview        최종 타입 구조 전체 요약 미리보기:
                    ┌───────────────────────────────────────────┐
                    │ 최종 구조 (8개 타입)                       │
                    ├───────────────────────────────────────────┤
                    │ 1. 공급업체  — 3개 필드                    │
                    │ 2. 원자재    — 6개 필드 (LOT번호 포함)     │
                    │ 3. 제품      — 3개 필드                    │
                    │ 4. 생산계획  — 4개 필드                    │
                    │ 5. 작업지시  — 5개 필드 (담당자 포함)      │
                    │ 6. 생산실적  — 3개 필드                    │
                    │ 7. 품질검사  — 5개 필드                    │
                    │ 8. 출하      — 4개 필드                    │
                    └───────────────────────────────────────────┘
23   await_confirm  "위 8개 타입을 생성합니다. 진행할까요?"
                    [생성] [취소]
```

사용자: [생성] 선택

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
24   mutate         워크스페이스 생성 API 호출 (이름: "생산관리시스템")
25   progress       "[1/8] 공급업체 타입 생성 중..."
26   mutate         공급업체 타입 생성 API 호출
27   progress       "[2/8] 원자재 타입 생성 중..."
28   mutate         원자재 타입 생성 API 호출
29   progress       "[3/8] 제품 타입 생성 중..."
30   mutate         제품 타입 생성 API 호출
31   progress       "[4/8] 생산계획 타입 생성 중..."
32   mutate         생산계획 타입 생성 API 호출
33   progress       "[5/8] 작업지시 타입 생성 중..."
34   mutate         작업지시 타입 생성 API 호출
35   progress       "[6/8] 생산실적 타입 생성 중..."
36   mutate         생산실적 타입 생성 API 호출
37   progress       "[7/8] 품질검사 타입 생성 중..."
38   mutate         품질검사 타입 생성 API 호출
39   progress       "[8/8] 출하 타입 생성 중..."
40   mutate         출하 타입 생성 API 호출
41   navigate       타입 캔버스 화면으로 이동 (생성된 결과 반영)
42   attention      (spotlight) 생성된 8개 타입을 캔버스에서 하이라이트:
                    "8개 타입이 모두 생성되었습니다."
43   complete       "생산관리시스템 워크스페이스와 8개 타입 생성이
                     완료되었습니다. 이제 데이터를 입력하거나
                     구조를 수정할 수 있습니다."
```

---

#### Phase 6: 초기 데이터 입력 안내

사용자가 첫 데이터를 입력할 수 있도록 에이전트가 안내한다.

사용자: "이제 원자재를 등록하려면 어떻게 해?"

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
44   navigate       문서 편집기 > 원자재 타입 선택
45   attention      (coachmark) 문서 목록 상단의 "새 문서" 버튼:
                    "여기서 원자재를 등록할 수 있습니다.
                     버튼을 눌러 새 원자재 정보를 입력해 보세요."
46   highlight      "새 문서" 버튼 강조
```

사용자: "엑셀에 있는 원자재 목록을 한번에 올릴 수 있어?"

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
47   navigate       데이터 가져오기(Import) 화면으로 이동
48   attention      (coachmark) 파일 업로드 영역:
                    "CSV 또는 Excel 파일을 여기에 끌어다 놓으세요.
                     컬럼명이 필드명과 일치하면 자동으로 매핑됩니다."
49   scroll         매핑 설정 영역으로 스크롤 이동
50   attention      (coachmark) 컬럼 매핑 테이블:
                    "파일의 컬럼과 원자재 필드를 연결합니다.
                     이름이 비슷하면 자동으로 매핑되고,
                     수동으로 변경할 수도 있습니다."
```

사용자가 파일을 업로드한다. 150건의 원자재 데이터가 포함된 CSV 파일.

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
51   preview        매핑 결과 미리보기:
                    품명 → 품명 ✓ | 규격 → 규격 ✓ | 단위 → 단위 ✓
                    단가 → 단가 ✓ | LOT번호 → LOT번호 ✓
                    업체명 → 공급업체(Ref) ⚠ 참조 매핑 필요
52   attention      (coachmark) '업체명' 매핑 경고:
                    "'업체명' 컬럼은 공급업체 타입의 참조 필드입니다.
                     공급업체 데이터를 먼저 등록하거나,
                     업체명으로 자동 생성할 수 있습니다."
53   await_confirm  "업체명에 해당하는 공급업체가 없으면 자동 생성할까요?"
                    [자동 생성] [건너뛰기] [취소]
```

사용자: [자동 생성] 선택

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
54   mutate         CSV 데이터 기반 공급업체 자동 생성 (12개 업체)
55   progress       "[1/2] 공급업체 12건 생성 완료"
56   mutate         원자재 150건 일괄 생성
57   progress       "[2/2] 원자재 150건 가져오기 완료"
58   navigate       문서 편집기 > 원자재 목록으로 이동
59   highlight      새로 생성된 원자재 목록 강조
60   notify         (info) "공급업체 12건, 원자재 150건이
                    성공적으로 등록되었습니다."
61   complete       "데이터 가져오기가 완료되었습니다."
```

---

#### Phase 7: 스키마 변경 (운영 중)

수 주 후, 사용자가 운영 중인 시스템의 스키마를 변경한다.

사용자: "생산실적에 불량수량 필드를 추가하고 싶어."

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
62   navigate       타입 편집기 > 생산실적 타입으로 이동
63   scroll         속성 목록 하단으로 스크롤
64   attention      (spotlight) 현재 속성 목록 안내:
                    "현재 생산실적 타입에는 3개 필드가 있습니다:
                     작업지시(Ref), 생산일(Date), 생산수량(Number)"
65   preview        속성 목록 diff 표시:
                    작업지시  Ref     (기존)
                    생산일    Date    (기존)
                    생산수량  Number  (기존)
                  + 불량수량  Number  (추가, nullable, 기본값: null)
66   attention      (coachmark) diff 영역:
                    "불량수량 필드가 Number 타입으로 추가됩니다.
                     기존 데이터에는 null이 들어갑니다."
67   await_confirm  "생산실적 타입에 '불량수량(Number, nullable)'
                     필드를 추가합니다. 적용할까요?"
                    [적용] [수정] [취소]
```

사용자: [적용] 선택

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
68   mutate         생산실적 타입 새 버전 생성 API 호출
                    (v1 → v2, 불량수량 필드 추가)
69   notify         (info) "기존 생산실적 320건에 대해
                    검증이 시작됩니다."
70   navigate       타입 캔버스로 이동 (갱신된 구조 반영)
71   highlight      생산실적 타입 노드의 '불량수량' 필드 강조
72   attention      (badge) 정합성 검증(Compliance) 메뉴에 뱃지 표시:
                    "320건의 기존 문서에 대한 검증이 진행 중입니다.
                     정합성 메뉴에서 결과를 확인할 수 있습니다."
73   complete       "생산실적 타입 v2 생성 완료.
                     320건 검증이 백그라운드로 진행됩니다."
```

---

#### Phase 8: 정합성 보정

검증 결과를 확인한 사용자가 에이전트에게 일괄 보정을 요청한다.

사용자: "불량수량이 빈 거 0으로 채워줘."

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
74   navigate       정합성 검증 결과 화면으로 이동
75   scroll         '불량수량 = null' 검증 항목으로 스크롤
76   preview        일괄 변경 미리보기:
                    ┌────────────────────────────────────────────┐
                    │ 대상: 생산실적 320건                        │
                    │ 변경: 불량수량 null → 0                     │
                    ├────────────────────────────────────────────┤
                    │ #001  2026-03-15  A라인  불량수량: null → 0 │
                    │ #002  2026-03-15  B라인  불량수량: null → 0 │
                    │ #003  2026-03-16  A라인  불량수량: null → 0 │
                    │ ...                                        │
                    │ #320  2026-04-04  C라인  불량수량: null → 0 │
                    └────────────────────────────────────────────┘
77   await_confirm  "생산실적 320건의 불량수량을 0으로 변경합니다.
                     진행할까요?"
                    [진행] [취소]
```

사용자: [진행] 선택

```
seq  type           payload
───  ─────────────  ───────────────────────────────────────────────────
78   mutate         생산실적 일괄 업데이트 API 호출
                    (불량수량: null → 0, 대상 320건)
79   progress       "일괄 변경 진행 중... [32/320] 10%"
80   progress       "일괄 변경 진행 중... [160/320] 50%"
81   progress       "일괄 변경 진행 중... [320/320] 100%"
82   notify         (info) "320건 변경 완료. 정합성 위반 항목이
                    0건으로 감소했습니다."
83   highlight      정합성 대시보드의 '위반 0건' 표시 강조
84   complete       "생산실적 320건의 불량수량 보정이 완료되었습니다.
                     모든 문서가 현재 스키마와 일치합니다."
```

---

> **사용된 커맨드 요약**
>
> | 커맨드 | 사용 횟수 | 주요 사용 맥락 |
> |---|---|---|
> | `navigate` | 8회 | 화면 전환 (캔버스, 편집기, 임포트, 정합성) |
> | `highlight` | 6회 | 생성된 요소, 변경된 필드 시각적 강조 |
> | `scroll` | 5회 | 포커스 이동, fit-to-view, 특정 항목 탐색 |
> | `preview` | 7회 | 타입 구조 미리보기, diff 표시, 일괄 변경 미리보기 |
> | `mutate` | 12회 | 타입 생성, 데이터 가져오기, 일괄 업데이트 |
> | `notify` | 4회 | 요구사항 요약, 검증 시작, 변경 완료 알림 |
> | `progress` | 7회 | 타입 일괄 생성, 데이터 가져오기, 일괄 보정 진행률 |
> | `attention` | 11회 | coachmark 안내, spotlight 강조, badge 알림 |
> | `await_confirm` | 7회 | 구조 확인, 생성 승인, 일괄 작업 승인 |
> | `complete` | 5회 | 각 Phase 종료 시 작업 완료 요약 |

### 3.18 워크플로우/상태 머신 (문서 생명주기)

문서는 DRAFT → REVIEW → PUBLISHED 생명주기를 따르며, 상태에 따라 편집 가능 여부가 결정된다.

- 문서는 DRAFT, REVIEW, PUBLISHED 세 가지 상태를 가진다.
- **DRAFT**: 편집 가능. 작성자가 자유롭게 수정할 수 있다.
- **REVIEW**: 읽기 전용. 승인자만 상태 전이(PUBLISHED 또는 DRAFT 반려)를 수행할 수 있다.
- **PUBLISHED**: 읽기 전용. 재편집이 필요하면 DRAFT로 되돌린 후 수정한다.
- 상태 전이 규칙:
  - DRAFT → REVIEW: 작성자가 검토 요청
  - REVIEW → PUBLISHED: 승인자가 승인
  - REVIEW → DRAFT: 승인자가 반려
  - PUBLISHED → DRAFT: 재편집 요청 (권한 필요)
- API: `PATCH /workspaces/{workspace}/documents/{id}/status`
  - 요청 바디: `{ "status": "REVIEW" }`
  - 권한 검증 후 상태 전이 수행. 유효하지 않은 전이 시 400 응답.

### 3.19 웹훅 알림 (외부 시스템 연동)

문서·타입 변경 시 외부 시스템에 HTTP 콜백으로 알림을 전송한다.

- 워크스페이스별로 웹훅 URL을 등록할 수 있다.
  - `POST /workspaces/{workspace}/webhooks` — 웹훅 등록
  - `GET /workspaces/{workspace}/webhooks` — 웹훅 목록 조회
  - `DELETE /workspaces/{workspace}/webhooks/{id}` — 웹훅 삭제
- 이벤트 발생 시 등록된 URL로 HTTP POST 콜백을 전송한다.
  - 페이로드: 이벤트 타입, 워크스페이스, 페이로드, 타임스탬프
- 이벤트 필터링을 지원한다 (DOCUMENT_CREATED, DOCUMENT_DELETED, TYPE_CREATED, TYPE_DELETED 등 선택 가능).
- 재시도 정책: 최대 3회, 지수 백오프 (1초, 2초, 4초).
- 연속 실패 시 웹훅을 비활성화(active=false)하고 워크스페이스 관리자에게 알린다.

### 3.20 필드 레벨 권한

타입의 각 속성(attribute)에 역할 기반 편집/조회 권한을 설정하여, 문서 편집 시 사용자 역할에 따라 특정 필드의 접근을 제한한다.

#### 속성별 권한 설정

- 타입 정의 시 각 속성에 `read_roles`(조회 허용 역할)와 `write_roles`(편집 허용 역할)를 설정할 수 있다.
- `read_roles`/`write_roles`는 JSONB 배열로 `type_attributes` 테이블에 저장된다.
- 역할 값은 기존 RBAC Role 계층(3.3)의 역할명을 사용한다.
- 빈 배열(`[]`)은 "제한 없음"을 의미한다 (기본값).

#### 문서 편집 시 적용

- 문서 편집기(스프레드시트)가 로드될 때, 사용자의 역할과 각 속성의 권한을 비교한다.
- `write_roles`에 사용자 역할이 포함되지 않은 속성의 셀은 읽기 전용으로 표시한다.
- `read_roles`에 사용자 역할이 포함되지 않은 속성의 셀은 마스킹 처리한다.

#### API

- 기존 타입 저장(`PUT /workspaces/{workspace}/types`) 및 패치(`PATCH /workspaces/{workspace}/types`) 시 속성의 `read_roles`/`write_roles`를 포함하여 전송한다.
- 타입 조회 시 각 속성의 `read_roles`/`write_roles`가 응답에 포함된다.

### 3.21 대시보드 차트

워크스페이스 통계를 시계열 차트로 시각화하여 추이를 파악할 수 있도록 한다.

#### 문서 생성 추이

- 일별/주별 문서 생성 건수를 라인 차트로 표시한다.
- 기간(from/to) 및 집계 간격(day/week)을 선택할 수 있다.

#### 검증 실패율 추이

- 기간별 검증 성공/실패 비율을 라인 차트로 표시한다.

#### 타입별 문서 분포

- 타입별 문서 수를 파이 차트로 표시한다.

#### 에이전트 사용량 추이

- 기간별 에이전트 요청 건수를 라인 차트로 표시한다.

#### API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspaces/{workspace}/stats/timeline?from=&to=&interval=` | 시계열 통계 (문서 생성, 검증 실패율, 에이전트 사용량) |
| GET | `/workspaces/{workspace}/stats/distribution` | 타입별 문서 분포 |

### 3.22 랜딩 페이지

제품을 설명하는 공개 콘텐츠를 두 가지 표면에 노출한다. 두 표면은 **동일한 "기능 설명" 원소스**(`landing-content`)를 공유하며, 외피(히어로·CTA·SEO 메타)만 각각 다르다.

| 표면 | URL | 산출 방식 | 목적 |
|------|-----|-----------|------|
| **SEO 랜딩** | `/` (ko), `/en/` | 빌드 타임 프리렌더된 정적 HTML | 검색엔진 인덱싱, 비로그인 방문자 첫 노출, 앱 진입 유도 |
| **앱 내부 랜딩** | 앱 내 메뉴(이름 미정) | 런타임 GWT activity 모듈 | 앱 사용자에게 기능 요약 제공 (로그인·비로그인 모두 접근 가능) |

#### 3.22.1 공통 원소스 (`landing-content`)

- 순수 GWT 라이브러리 모듈로, **Handbook 의 기능을 설명하는 카드 컬렉션**만 포함한다 (운영 중 스키마 변경, 이력 관리, AI 에이전트, 실시간 협업 등).
- 히어로, CTA, SEO 메타, 외부 앵커는 포함하지 않는다 — 이들은 각 표면(SEO 랜딩 / 앱 내부)에서 추가한다.
- 공개 팩토리 메서드로 `HTMLElement` 를 반환하여, SEO 랜딩(`landing-ui`)과 앱 내부 activity 양쪽에서 동일 DOM 을 생성한다.
- i18n 은 기존 `src/main/i18n/language.{locale}.json` 패턴으로 제공한다.

#### 3.22.2 SEO 랜딩

검색엔진 크롤러와 비로그인 첫 방문자가 보는 공개 페이지. 앱(`/app.html`) 과 완전히 분리된 정적 산출물이며, 런타임 서버 렌더링은 하지 않는다.

##### 목적

- **공개 진입점 제공**: 로그인 없이 접근 가능한 제품 소개 페이지.
- **검색엔진 최적화(SEO)**: 크롤러가 사이트 구조와 핵심 콘텐츠를 인덱싱할 수 있도록 의미 있는 정적 HTML 을 제공한다. 현재 앱은 GWT SPA 로 `<body>` 가 비어 있어 크롤러가 인덱싱할 내용이 없다.
- **앱 진입 유도**: 방문자가 CTA 클릭으로 앱으로 이동하거나, 이미 로그인한 경우 자동으로 앱으로 넘어갈 수 있게 한다.

##### 콘텐츠 범위

- **히어로** (SEO 랜딩 전용) — 제품 제목/부제 + CTA 앵커 (`<a href="/app.html">`, `<a href="/auth/login">`)
- **기능 설명 카드** — `landing-content` 로부터 공통 주입
- **푸터** — 회사/라이선스/언어 전환

상세 카피와 시각 자산은 후속 반복에서 정의한다. 초기 릴리스는 최소 골격(히어로 + 공통 기능 설명 + 푸터)으로 출발한다.

##### 메뉴/링크

- 공개 엔트리의 **정적 링크** 집합으로 구성한다.
- 초기 집합: `/auth/login` (로그인), `/app.html` (앱 진입).
- `/menus` API 는 호출하지 않는다 — 프리렌더 시점에 인증 상태가 없고, 워크스페이스별 메뉴를 랜딩에 노출하지 않는다.
- 추후 공개 섹션이 추가되면 같은 메뉴에 정적 링크로 합류한다.

##### 로그인 상태 자동 리다이렉트

- 랜딩 HTML 에 포함된 얇은 인라인 스크립트가 JWT 쿠키 존재 여부를 검사한다.
- 쿠키가 있으면 `location.replace('/app.html')` 로 즉시 앱으로 이동한다.
- 쿠키가 없으면(크롤러 및 미로그인 방문자) 랜딩을 그대로 노출한다.
- **크롤러가 쿠키 없이 JS 를 실행해도 리다이렉트가 일어나지 않아야 한다.** Googlebot 은 JS 를 실행하지만 쿠키는 없으므로 동일한 판정을 거친다.
- 스크립트는 `<head>` 최상단이 아닌 `defer` 스크립트로 배치한다 (Googlebot 의 "지연된 서버 리다이렉트" 오해 방지).

##### 다국어 SEO

- **URL 전략**: 서브디렉토리. `/` (ko 기본, `x-default`), `/en/` (영어). 서브도메인·쿼리 파라미터·쿠키 기반 언어 분기는 사용하지 않는다.
- **hreflang**: 각 로케일 HTML 의 `<head>` 에 양방향 전체 목록을 선언한다. 자기 자신 포함.

  ```html
  <link rel="alternate" hreflang="ko" href="https://handbook.sayaya.cloud/">
  <link rel="alternate" hreflang="en" href="https://handbook.sayaya.cloud/en/">
  <link rel="alternate" hreflang="x-default" href="https://handbook.sayaya.cloud/">
  ```
- **`<html lang>`**: 각 HTML 에 정확한 로케일 코드 (`lang="ko"`, `lang="en"`).
- **canonical**: 자기 자신으로. ko 페이지가 `/en/` 을 canonical 로 지정하지 않는다.
- **Accept-Language 자동 리다이렉트 금지**: 브라우저 언어를 보고 302 응답을 주지 않는다 (Google 가이드라인 위반).
- **쿠키/세션 기반 언어 분기 금지**: 같은 URL 이 요청자마다 다른 HTML 을 반환하면 크롤러가 혼동한다.

##### SEO 필수 요소

각 로케일 HTML 의 `<head>` 에 다음을 포함한다.

| 요소 | 설명 |
|------|------|
| `<title>` | 로케일별 페이지 타이틀 |
| `<meta name="description">` | 150자 이내 페이지 요약 |
| `<meta property="og:*">` | Open Graph (title, description, image, url, type) |
| `<meta name="twitter:*">` | Twitter Card (summary_large_image) |
| `<link rel="canonical">` | 자기 URL |
| `<link rel="alternate" hreflang>` | 전체 로케일 링크 |
| `<html lang>` | 로케일 코드 |

추가로 `/sitemap.xml`, `/robots.txt`, `/llms.txt`, `/llms-full.txt` 를 같은 S3 버킷에 배포한다.

- **`/sitemap.xml`**: 각 로케일 URL 을 엔트리로 포함하고, 각 엔트리에 `xhtml:link rel="alternate"` 로 다른 로케일을 명시한다.
- **`/robots.txt`**: 크롤링 허용 + `Sitemap:` 지시어로 사이트맵 위치 제공. `/app.html` 은 **Disallow 하지 않는다** — 크롤링을 막으면 `noindex` 메타를 읽지 못해 의도치 않은 스니펫이 노출될 수 있다. 색인 차단은 `/app.html` 측의 `<meta name="robots">` 로 수행한다.
- **`/llms.txt`**: AI 에이전트·LLM 디스커버리용 사이트 요약. 마크다운 포맷으로 "이 사이트는 무엇이며 어떤 주요 리소스가 있는지" 를 자연어로 기술. 랜딩 빌드 파이프라인이 i18n 입력에서 동일한 원천으로 생성해 SEO 랜딩 콘텐츠와 일치시킨다.

  ```
  # Handbook

  > 운영 중 스키마 변경과 이력 관리를 지원하는 문서 관리 시스템

  ## Key Resources
  - [Landing (ko)](https://handbook.sayaya.cloud/)
  - [Landing (en)](https://handbook.sayaya.cloud/en/)
  - [OpenAPI spec](https://handbook.sayaya.cloud/openapi.json)

  ## Capabilities
  - 운영 중 스키마 변경 및 이력 관리
  - AI 에이전트 기반 자연어 데이터 조작
  - 실시간 협업 (SSE)
  ```
- **`/llms-full.txt`**: 랜딩 콘텐츠 전체를 하나의 마크다운 파일로 덤프. 오프라인 인덱싱 / 긴 컨텍스트 모델에서 참조.

##### SEO 힌트 및 앱 진입 유도

검색 결과(SERP)에서 랜딩이 노출되고, 사용자가 클릭한 뒤 랜딩 → 앱(`/app.html`)으로 자연스럽게 진입하도록 유도한다. 크롤러에게는 "이 사이트는 웹 앱이며, 앱 진입점은 `/app.html` 이다" 를 구조화 데이터로 명시한다.

###### 1) 구조화 데이터 (JSON-LD)

각 로케일 랜딩 HTML 의 `<head>` 에 Schema.org `WebApplication` 과 `WebSite` 를 JSON-LD 로 포함한다.

```html
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "WebApplication",
  "name": "Handbook",
  "url": "https://handbook.sayaya.cloud/",
  "applicationCategory": "BusinessApplication",
  "operatingSystem": "Web",
  "browserRequirements": "Requires JavaScript",
  "offers": { "@type": "Offer", "price": "0" }
}
</script>
```

필요 시 `WebSite` + `potentialAction` (SearchAction), `SiteNavigationElement` (메뉴 구조), `HowTo` (주요 사용 흐름 요약) 를 함께 추가하여 사이트 네비게이션/검색박스 리치 결과 후보로 편입하고, AI 에이전트에게도 구조적 의미를 전달한다.

###### 2) PWA 매니페스트 연결

랜딩 HTML 의 `<head>` 에 `<link rel="manifest" href="/manifest.json">` 을 선언하여 설치 가능한 웹 앱임을 표시한다. 모바일 SERP 에서 "앱 열기" 라벨/설치 프롬프트 후보가 된다.

###### 3) 시맨틱 CTA 링크

히어로·메뉴의 앱 진입 버튼은 반드시 **실제 `<a href="/app.html">` 앵커 태그**로 작성한다. JS 전용 핸들러(`<button onclick="...">` 또는 `window.location` 만 쓰는 링크)는 크롤러가 추적하지 못한다. 앵커 텍스트에는 "앱 시작하기"·"Handbook 열기" 등 의도가 드러나는 문자열을 사용한다.

###### 4) 앱 셸 색인 차단

`/app.html` 은 빈 DOM 을 반환하므로 크롤러가 직접 색인하면 thin content 로 판정될 수 있다. `app.html` 의 `<head>` 에 다음을 추가한다.

```html
<meta name="robots" content="noindex, follow">
```

- `noindex`: `/app.html` 페이지 자체는 색인하지 않는다. 결과적으로 SERP 에는 랜딩만 노출된다.
- `follow`: 페이지 내부 링크(있다면)는 따라가도 된다.
- **`robots.txt` 에서 `/app.html` 을 Disallow 하지 않는다** — 크롤링을 막으면 메타 태그를 읽지 못한다.

###### 5) 내부 링크와 앵커 텍스트

랜딩 내 주요 섹션(기능 소개, 시작하기, 문의 등)은 각각 고유한 URL fragment(`/#features`, `/#pricing`) 또는 내부 페이지로 분리하고, 각 링크의 앵커 텍스트를 명확하게 구성한다. 이는 Google 의 sitelinks 생성에 영향을 준다.

##### 빌드 및 배포 (빌드 타임 프리렌더)

- 개발은 기존 GWT 스택으로 한다: 새 모듈 `landing-ui` 가 `landing-content` 의 공통 DOM 을 감싸고 히어로·CTA·SEO 메타 마커를 추가한다.
- 배포 산출물은 **정적 HTML** 이며 런타임 JS 프레임워크를 포함하지 않는다. 즉 빌드 시 GWT 모듈을 실행하여 DOM 을 캡처하고, 그 결과를 최종 HTML 로 덤프한다.
- 프리렌더 파이프라인:

  ```
  ./gradlew :landing-ui:prerender
    → (로케일별 반복)
        1. landing-ui GWT 컴파일 (해당 로케일의 language.{locale}.json 머지)
        2. 경량 로컬 서버(Jetty)로 컴파일 산출물 서빙
        3. Playwright 헤드리스로 접속 → body.rendered 마커 대기
        4. page.content() 로 HTML 덤프
        5. 후처리: <script src="...landing.nocache.js"> 제거,
                 <html lang>, <link rel="canonical">, hreflang 주입,
                 OG/Twitter/title/description 주입
        6. build/landing/{locale}/index.html 로 저장
    → sitemap.xml 및 robots.txt 생성
  ```
- **결정성(determinism)**: 같은 입력(소스 + i18n + 자산)은 같은 HTML 을 생성해야 한다. 타임스탬프, 난수, 외부 API 호출을 프리렌더 중에 포함하지 않는다.
- **렌더 완료 시그널**: `landing-ui` 는 초기화 마지막에 `document.body.classList.add('rendered')` 를 호출한다. Playwright 는 이 클래스를 기다린 후에만 덤프한다.
- **아이콘 치환 대기**: FontAwesome JS 가 `<i>` 를 `<svg>` 로 치환하므로, 치환 완료 이후에 덤프해야 한다 (마커에 치환 완료 신호 포함).
- 산출물은 S3 (`ceph-rgw`) 의 `handbook-<stage>/static/landing/{locale}/index.html` 로 업로드한다. `sitemap.xml` 과 `robots.txt` 는 `handbook-<stage>/static/` 루트에 업로드한다.
- 프론트엔드 다른 GWT 모듈(shell-ui, login-ui, workspace-ui) 과 동일한 Kargo Release Train 패턴을 따르되, sync-job 이 업로드하는 오브젝트 키와 후처리 단계가 다르다 (`handbook-lib` 에 랜딩 전용 템플릿 추가).

##### Ingress 경로

`app` 차트의 HTTPRoute 를 다음과 같이 구성한다 (셸/앱의 기존 규칙은 유지).

| 경로 | 대상 오브젝트 |
|------|---------------|
| `/` (GET) | `static/landing/ko/index.html` |
| `/en/` (GET) | `static/landing/en/index.html` |
| `/sitemap.xml` (GET) | `static/sitemap.xml` |
| `/robots.txt` (GET) | `static/robots.txt` |
| `/llms.txt` (GET) | `static/llms.txt` |
| `/llms-full.txt` (GET) | `static/llms-full.txt` |
| `/app.html`, `/js/**`, `/css/**`, `/manifest.json`, `/service-worker.js` | 기존대로 (앱) |

##### 제약 및 금지 사항

- **Cloaking 금지**: 크롤러와 사람에게 동일한 URL 에 동일한 HTML 을 제공한다. User-Agent 로 응답을 분기하지 않는다.
- **프리렌더 중 백엔드 호출 금지**: 결정적 빌드를 위해 SEO 랜딩은 `/menus`·`/auth/*` 등 API 를 호출하지 않는다. 필요한 데이터는 빌드 타임에 코드/i18n 으로 확정한다.
- **인증 종속 콘텐츠 금지**: 워크스페이스·사용자·동적 데이터를 SEO 랜딩에 노출하지 않는다.
- **외부 도메인 자산 최소화**: 폰트·아이콘 등 외부 리소스는 기존 `app` 모듈과 동일 출처를 재사용하거나 랜딩 번들에 포함한다.

#### 3.22.3 앱 내부 랜딩

앱(`/app.html`) 의 메뉴 중 하나로 제공되는 "소개" 성격의 activity. 로그인·비로그인 사용자 모두 접근할 수 있으며, 같은 `landing-content` 를 재사용해 기능 설명을 보여준다.

##### 목적

- 이미 앱 진입 경로에 도달한 방문자에게 제품의 핵심 기능을 간결하게 요약한다.
- 로그인 전후로 메뉴 포지션을 유지해 "이 제품이 뭘 할 수 있는지" 를 언제든 다시 확인할 수 있게 한다.

##### 콘텐츠 범위

- **기능 설명 카드** — `landing-content` 로부터 공통 주입 (SEO 랜딩과 동일한 블록).
- **상태별 CTA** (아래 "로그인/비로그인 분기" 참조).
- **히어로·SEO 메타는 포함하지 않는다** — 이미 앱에 진입한 사용자에게는 제품 식별이 불필요하고, 메뉴·프레임이 앱 셸 chrome 으로 이미 제공된다.

##### 메뉴 공급

- 새 백엔드 모듈 또는 gateway 내부에서 `MenuSupplier` 를 구현하여 `/menus` 집계에 참여한다 (기존 `login` 모듈의 Sign In/Out 메뉴 공급 패턴을 그대로 따른다).
- 메뉴 이름·URL 은 추후 결정한다 (초기 구현 시 플레이스홀더 사용).
- 인증 여부와 무관하게 메뉴에 노출된다 — 비로그인 사용자도 `/menus` 응답에서 이 엔트리를 받는다.
- `urlRegex()` 를 설정하여 `UrlBasedMenuResolver` 가 딥링크 진입 시 이 메뉴를 자동 선택하게 한다.

##### 로그인/비로그인 분기 (CTA 만)

- 본문(기능 설명 카드)은 로그인 여부와 무관하게 동일하다.
- 하단 CTA 한 줄만 상태에 따라 달라진다.

  | 상태 | CTA |
  |------|-----|
  | 비로그인 | "시작하기" → `/auth/login` |
  | 로그인 | "새 워크스페이스" → 워크스페이스 생성 다이얼로그 (UC-10) 트리거 |

- 상태는 런타임에 FetchApi 로 `/user` 를 조회해 판별한다. 인증 오류(401) 는 비로그인으로 간주.
- 상태 판별 실패 시 비로그인 variant 를 기본으로 노출한다 (안전한 기본값).

##### 제약 및 금지 사항

- **SEO 랜딩과 콘텐츠 분기점을 동기화한다** — 공통 카드 구성이 변경되면 양쪽 표면에 즉시 반영되어야 한다 (`landing-content` 가 단일 출처).
- **앱 내부 랜딩에는 SEO 메타·JSON-LD·hreflang 등을 넣지 않는다** — 앱 셸 내부 DOM 이라 검색엔진 색인과 무관하다.
- **로그인/비로그인 상태별 콘텐츠 분기는 CTA 영역으로 한정한다** — 본문을 분기하면 공통 원소스 원칙이 훼손된다.

### 3.23 외부 AI 에이전트 통합

검색엔진과 별개로, Gemini·ChatGPT 등 외부 AI 에이전트가 Handbook 을 **발견(discovery)** 하고 **조작(tool use)** 할 수 있도록 지원한다. 내부 `assistant` 모듈(§3.17) 과는 구분되는 통합 경로이며, 감사 로그에서 외부 에이전트 호출을 별도 트레이스로 구분한다.

#### 3.23.1 디스커버리 (AI 가 사이트를 읽을 수 있게)

AI 에이전트가 크롤링/검색 과정에서 Handbook 의 존재와 기능을 이해할 수 있도록 표준 리소스를 제공한다. SEO (§3.22.2) 와 표면을 공유하지만 대상 수요자가 다르다.

| 리소스 | 경로 | 용도 |
|--------|------|------|
| `llms.txt` | `/llms.txt` | LLM 디스커버리용 사이트 요약 (마크다운). 핵심 리소스 링크 제공 |
| `llms-full.txt` | `/llms-full.txt` | 랜딩 콘텐츠 전체 덤프 (긴 컨텍스트 모델용) |
| OpenAPI 스펙 | `/openapi.json` | REST API 전체 스펙 (§3.12) |
| JSON-LD | SEO 랜딩 `<head>` | `WebApplication`, `potentialAction`, `HowTo` (§3.22.2) |

랜딩 빌드 파이프라인이 `/llms.txt` 와 `/llms-full.txt` 를 동시에 생성한다 (§3.22.2 빌드 및 배포 참조). 콘텐츠 분기를 방지하기 위해 랜딩 카드 i18n 과 동일한 원천을 사용한다.

#### 3.23.2 Tool Use (AI 가 Handbook 을 조작)

AI 에이전트가 function calling / tool use 로 Handbook REST API 를 호출해 실제 작업(문서 생성·타입 변경·검색 등)을 수행할 수 있도록 한다.

##### 인증

- §3.12 의 **API Key / Bearer Token** 인증을 재사용한다.
- 사용자별로 Personal Access Token (PAT) 을 발급하고, PAT 에 워크스페이스·역할 제한을 바인딩한다.
- 기존 RBAC (§3.3) 이 그대로 적용된다 — 외부 에이전트가 권한 밖의 작업을 요청하면 거부.
- PAT 발급·회수 UI 는 shell-ui 의 사용자 설정(§6.8) 영역에 추가한다.

##### OpenAPI 공개

- `/openapi.json` 은 인증 없이 읽을 수 있도록 공개한다 (스펙 자체는 민감 정보 아님).
- 각 엔드포인트에 **AI 친화적 설명** (`summary`, `description`, `examples`) 을 충분히 기입하여 function calling 품질을 높인다.
- `operationId` 는 스네이크 케이스 동사_명사 형태 (예: `create_workspace`, `search_documents`) 로 통일.

##### `/.well-known/` 디스커버리

- `/.well-known/openapi.yaml` → `/openapi.json` 으로 리다이렉트 또는 미러.
- `/.well-known/ai-plugin.json` — 레거시 ChatGPT plugin 포맷이 여전히 일부 도구에서 사용됨. 초기엔 생략 가능.

##### MCP 서버 (후속 반복)

> **미구현 / 후속 반복.** 초기 릴리스에는 포함하지 않는다. 아래는 예정된 설계.

- 별도 모듈 **`mcp-server`** 를 신설하여 Anthropic Model Context Protocol (MCP) 규격에 맞는 서버를 제공한다.
- Handbook REST API 를 MCP `tools` 로 래핑하여 Gemini Desktop, VS Code MCP 클라이언트 등에서 자연어로 Handbook 을 조작할 수 있도록 한다.
- 노출 대상 tool (예시):
  - `list_workspace_menu()` → `GET /menus` (workspace-query — 워크스페이스 기능 디스커버리)
  - `create_workspace(name, description)` → `POST /workspaces`
  - `list_types(workspace)` → `GET /workspaces/{workspace}/types`
  - `create_type(workspace, name, attributes[])` → `PUT /workspaces/{workspace}/types`
  - `search_documents(workspace, type, query, limit, page)` → `GET /workspaces/{workspace}/documents`
  - `patch_document(workspace, type, serial, changes)` → `PATCH /workspaces/{workspace}/documents`
- MCP `resources` 로 워크스페이스의 타입 스키마·문서 샘플을 조회할 수 있게 한다.
- MCP `prompts` 로 "새 워크스페이스 설계" 같은 재사용 가능한 프롬프트 템플릿을 제공.
- 인증은 PAT 를 MCP 세션 초기화 시 전달하는 방식으로 간소화.
- 배포: Spring Boot WebFlux 서비스로 구성하고 기존 JVM 백엔드 Release Train 에 합류.

#### 3.23.3 감사 추적

- 외부 AI 에이전트 호출은 Gateway 에서 **API Key 기반 인증 경로** 를 거치므로, 기존 감사 로그(§3.17 에이전트 감사 추적, §6.9) 에 호출자 구분 필드를 추가한다.
- 필드: `caller_type` (user / internal_agent / external_agent / mcp_client), `caller_id` (사용자 UUID + 토큰 ID).
- 감사 로그 조회 UI 에서 호출자 타입별 필터를 제공한다.

#### 3.23.4 제약 및 금지 사항

- **PAT 는 워크스페이스 범위 제한이 필수** — 전 사이트 접근 토큰은 발급하지 않는다.
- **외부 AI 호출도 기존 Rate Limiting (§7.1) 을 적용** — 토큰 단위로 분당 한도 제한.
- **MCP 서버는 DB 직접 접근 금지** — 내부 `assistant` 와 동일하게 Gateway API 를 경유한다 (권한 검증 일관성).
- **`/openapi.json` 에 내부 전용 엔드포인트 노출 금지** — `/actuator/**` 등은 별도 스펙 또는 필터링.

### 3.24 사용자 상태 모델 및 메뉴 가시성

메뉴/기능의 가시·활성 여부를 **하드코딩된 분기(`if principal == null`, `if workspace != null`)** 로 결정하지 않고, 공급자(`MenuSupplier`)가 자기 기능이 **노출될 세션 상태 집합** 을 선언하면, Shell 이 **현재 사용자 상태(SessionState)** 를 평가해 자동으로 가시/활성/Call-To-Action 을 결정한다. 의도: 온보딩(UC-12) · 로그인 유도 · 권한 기반 비활성화 로직이 메뉴마다 흩어지는 문제를 단일 상태 머신으로 수렴.

#### 3.24.1 사용자 상태 축 (전체 레퍼런스)

사용자의 전역 상태는 다음 6개 축의 **직교 조합** 으로 표현될 수 있다. 본 릴리스(Phase 1)에서는 A·C 만 구현하며 나머지는 후속 Phase 로 이월한다.

| 축 | 값 | Phase | 비고 |
|----|----|-------|------|
| A. 인증 수명 | Anonymous / Authenticated | **Phase 1** | 쿠키·JWT 유효성 |
| B. 계정 상태 | ACTIVE / INACTIVATED / SUSPENDED | Phase 2 | `user.state` 필드. 전역 차단 오버레이 (`AccountStatusGuard`) 필요 |
| C. 워크스페이스 멤버십 | NoWorkspace / InWorkspace / InWorkspaceAsAdmin | **Phase 1** (Admin 제외) | `workspaces` 리스트 + 선택 컨텍스트 |
| D. 연결 상태 | Online / Offline / Reconnecting | Phase 2 | service-worker 기반 오프라인 감지 · AppBar 배너 + 쓰기 액션 disabled |
| E. 법적 상태 | TermsAccepted / TermsPending / EmailUnverified | Phase 2 | 전역 모달 게이트 (약관·이메일 확인) |
| F. 과금/구독 | Tier(Free/Team/Enterprise…) + FeatureFlags + CreditBalance | Phase 2 | 플랜별 접근권 + 크레딧 잔액 (가변 비용 action 차감) |

**Phase 2 이후 — requirements-only 기록 (코드 변경 없음):**
- B. `AccountStatusGuard` 도입 시 `INACTIVATED` 상태는 모든 메뉴를 disabled 로 강제.
- D. 연결 상태는 `SessionState` 의 외부에 **직교 축** 으로 병렬 유지 (인증·멤버십과 독립).
- E. 법적 상태는 `Authenticated` 도달 직후·모든 메뉴 클릭 전 평가되는 gate.
- **`IN_WORKSPACE_AS_ADMIN` 등 새 `SessionStateKind` 값** 은 Role 세분화(§3.3) 가 `workspace:admin` 권한을 제공하기 시작하면 enum 에 추가.
- **B/D/E 축 확장** 은 `Menu` 에 새 필드(`allowedAccountStates`, `allowedConnections`, `requiredConsents`) 를 **각 축 독립 선언** 으로 추가하고 평가 함수에 **AND 절을 한 줄씩 늘리는** 방식으로 대응. 기존 `allowedSessionStates` 의 의미는 변하지 않는다.

**F. 과금/구독 축 (미구현, 복합 구조 확정):**
- **설계 방향**: **티어(주) + 크레딧(보조)** 복합 구조. 월 구독 플랜(Free/Team/Enterprise 등) 이 기능 접근권·워크스페이스 상한·rate-limit·CS 등급을 결정하고, 플랜당 월 크레딧이 포함되며 초과분은 pay-as-you-go 로 추가 구매. 근거: Anthropic/OpenAI 모델 벤치마킹.
- **메뉴 가시성 측면**: `Menu` 에 `allowedSubscriptionTiers: Set<Tier>?` (플랜별 접근권) · `requiredFeatureFlags: Set<FeatureFlag>?` (베타/프리미엄 기능) 두 필드 추가. B/D/E 와 동일하게 **각 축 독립 선언** · AND 절 확장.
- **액션 과금 측면**: 메뉴 가시성과 **별개 레이어**. 컨트롤러 진입 시점 crediting decorator (예: `@RequiresCredits(amount)`) 가 잔액 확인 + post-action 차감. AI 생성 토큰·임포트 용량 등 가변 비용 action 전용. 메뉴 필드로 섞지 않음. 과금 요구사항 본편은 별도 Phase RFC 에서 구체화.

#### 3.24.2 Phase 1 상태 모델 (sealed class + kind enum)

Phase 1 은 A + C 를 **sealed class `SessionState`** 로 모델링하고, 메뉴 가시성 판정은 여기서 파생되는 **kind enum (`SessionStateKind`)** 집합에 대한 멤버십으로 수행한다. 계층 추론은 하지 않는다.

```kotlin
sealed class SessionState {
    object Anonymous : SessionState()                           // 인증 없음 (쿠키 무효/부재)
    data class Authenticated(
        val user: User,
        val workspaces: List<WorkspaceSummary>,
    ) : SessionState()                                          // 인증 성공, 활성 워크스페이스 미선택
    data class InWorkspace(
        val user: User,
        val workspaces: List<WorkspaceSummary>,
        val workspace: Workspace,
        val memberships: List<Membership>,
    ) : SessionState()                                          // 인증 + 활성 워크스페이스 선택
}

enum class SessionStateKind {
    ANONYMOUS,          // SessionState.Anonymous
    AUTHENTICATED,      // SessionState.Authenticated (워크스페이스 선택 전)
    IN_WORKSPACE,       // SessionState.InWorkspace
    // IN_WORKSPACE_AS_ADMIN — Phase 2 (role 세분화 후 추가)
}

val SessionState.kind: SessionStateKind get() = when (this) {
    is SessionState.Anonymous     -> SessionStateKind.ANONYMOUS
    is SessionState.Authenticated -> SessionStateKind.AUTHENTICATED
    is SessionState.InWorkspace   -> SessionStateKind.IN_WORKSPACE
}
```

- 상태 전이: `Anonymous → Authenticated` (로그인 성공), `Authenticated ↔ InWorkspace` (워크스페이스 선택/해제), `* → Anonymous` (로그아웃·세션 만료).
- `SessionStateKind` 값은 **집합 멤버십 비교의 원자 단위** 이다. 상위·하위 관계는 정의되지 않으며, 평가 함수는 이를 암묵 추론하지 않는다.

#### 3.24.3 `Menu.allowedSessionStates` (Phase 1)

각 메뉴는 자신이 **어떤 세션 상태에서 보여야 하는지를 집합으로 열거** 한다. 계약 상세는 `docs/contracts/menus.md` 의 "allowedSessionStates" 섹션.

```kotlin
data class Menu(
    // ... title, order, icon, script, bottom, appBarSlot, url, urlRegex, ...
    val allowedSessionStates: Set<SessionStateKind>? = null,
    // ...
)
```

- **`url`**: 메뉴 선택 시 주소창에 반영할 대표 경로. 명시되지 않으면 주소창을 변경하지 않는다 (Pure SPA Navigation).
- **`urlRegex`**: 브라우저 URL 기반 자동 메뉴 선택을 위한 패턴 목록.
- **null (기본값)** ⇒ **모든 상태에서 상시 보임 (무제약)**. 공급자가 필드를 누락하면 이 의미가 된다.
- 값은 `SessionStateKind` 값의 **집합 (Set)**. 비어 있지 않은 집합을 넣는다.
- **계층 추론 없음**: `{AUTHENTICATED}` 만 선언하면 `IN_WORKSPACE` 사용자는 **제외** 된다. "로그인 이후 모두에게 보이는" 메뉴는 `{AUTHENTICATED, IN_WORKSPACE}` 처럼 **명시 열거** 해야 한다.

> **공급자 주의 박스 — 보안적 영향**
> null default 는 "모두에게 보임" 이다. 인증 필요 메뉴가 공급자 쪽에서 필드를 빼먹으면 **익명 사용자에게도 노출** 된다. 서버 측 권한 체크가 최종 방어선이지만, UI 노출만으로도 정보 유출 소지가 있으므로 공급자 **필드 기입을 체크리스트 항목으로** 강제한다 (`docs/contracts/menus.md` 참조).

#### 3.24.4 평가 알고리즘

**Phase 1 (단일 축):**
```
visible(menu, state) =
    menu.allowedSessionStates == null
    OR state.kind ∈ menu.allowedSessionStates
```

**Phase 2 확장 시 (AND 절 확장):**
```
visible(menu, state) =
      (menu.allowedSessionStates == null OR state.kind           ∈ menu.allowedSessionStates)
  AND (menu.allowedAccountStates == null OR state.accountStatus  ∈ menu.allowedAccountStates)
  AND (menu.allowedConnections   == null OR state.connection     ∈ menu.allowedConnections)
  AND (menu.requiredConsents     == null OR menu.requiredConsents ⊆ state.acceptedConsents)
```

각 축은 **독립 선언** 이며, 미선언(null) ⇒ 해당 축 무제약, AND 결합. Phase 2 신규 필드가 추가되어도 **Phase 1 공급자의 기존 선언 의미는 불변** (기존 축만 평가).

> **주의 — 메뉴 가시성 ≠ 액션 실행 권한**
> 가시성은 축별 `Set<T>?` 필드로 AND 결합해 "보일지" 만 결정한다. 액션 실행 비용(크레딧 차감, rate-limit 등) 은 **별도 decorator/aspect 레이어** 에서 메뉴와 독립적으로 관리. 예: 플랜상 접근권은 있지만 크레딧이 부족한 상태 → 메뉴는 보이되 실행 시점에 decorator 가 거절. 이 분리를 지키지 않으면 가시성 필드가 과금 로직을 흡수해 `Menu` 가 비대해진다.

**CTA 규칙:**
- `visible` 판정이 `false` 여도 **메뉴는 hide 하지 않고 disabled 로 렌더** 한다 (불투명도 + 자물쇠 힌트). 상태 집합에서 "현재 상태 → 허용 상태" 로 넘어가는 가장 짧은 경로를 CTA 로 제시:
  - 현재 `ANONYMOUS` + 허용 집합이 `AUTHENTICATED`/`IN_WORKSPACE` 포함 → **Sign In** CTA.
  - 현재 `AUTHENTICATED` + 허용 집합이 `IN_WORKSPACE` 만 포함 → **Create/Join Workspace** CTA.
- **예외**: `appBarSlot = "trailing"` 인 세션 액션(Sign In/Sign Out) 은 공급자 내부에서 인증 상태별로 **하나만 emit** 한다. 이 분기는 `allowedSessionStates` 로도 표현 가능하지만(각각 `{ANONYMOUS}`, `{AUTHENTICATED, IN_WORKSPACE}`) 현재 login 공급자 동작 호환을 위해 내부 분기를 유지한다.

#### 3.24.5 공급자 선언 예시

| 메뉴 | `allowedSessionStates` | 효과 |
|------|-----------------------|------|
| 랜딩/소개 | `null` | 모든 상태에서 상시 보임 |
| Sign In | `{ANONYMOUS}` | 로그인 안 한 사람만 |
| Sign Out | `{AUTHENTICATED, IN_WORKSPACE}` | 로그인한 사람만 (두 상태 **명시 열거**) |
| 워크스페이스 생성/참여 | `{AUTHENTICATED, IN_WORKSPACE}` | 로그인 사용자 — 워크스페이스 보유자도 추가 생성 가능 |
| 타입/문서/워크스페이스 관리 | `{IN_WORKSPACE}` | 워크스페이스 소속자만 |

#### 3.24.6 `WorkspaceOnboardingBootstrapper` 와의 관계

기존 shell-ui `WorkspaceOnboardingBootstrapper` 는 `WorkspaceList` 가 empty 일 때 workspace-ui 스크립트를 client-side synthetic 메뉴로 주입해 "빈 워크스페이스 자동 온보딩" (UC-12) 을 수행한다. Phase 1 `allowedSessionStates` 도입 이후:

- `workspace-query` 의 워크스페이스 관리 메뉴는 `allowedSessionStates = {AUTHENTICATED, IN_WORKSPACE}` 로 공급.
- 워크스페이스 생성/참여 엔트리를 **별도 공급자** 가 `{AUTHENTICATED, IN_WORKSPACE}` 로 공급하면, `AUTHENTICATED` 상태 사용자에게도 enabled 로 노출 → 클릭 시 Create/Join 화면으로 자연 라우팅.
- **onboarding bootstrapper 유지 (2026-04-23)** — `allowedSessionStates` 기반의 명시적 CTA (클릭 유도) 가 가능하더라도, 워크스페이스가 없는 신규 사용자를 위한 자동 진입 로직은 UX 마찰을 줄이기 위해 유지한다.

#### 3.24.7 구현 책임

- **Shell (shell-ui)**: `SessionState` observable 공급(인증·워크스페이스 컨텍스트 관찰) · `allowedSessionStates` 기반 가시/활성 결정 · CTA 라우팅.
- **각 공급자 (MenuSupplier)**: 자기 메뉴의 `allowedSessionStates` 명시. 누락 시 default `null` (= 무제약 노출) 이므로 **인증 필요 메뉴는 반드시 명시**.
- **계약 문서**: `docs/contracts/menus.md` — `allowedSessionStates` 필드 스펙, 하위 호환(additive, `application/vnd.sayaya.handbook.v1+json` 유지).

#### 3.24.8 대응 UC

- **UC-73 (예약)** — 메뉴 가시성 평가 및 상태 기반 CTA. 본문은 후속 작업에서 `ui-platform-expert` / `auth-expert` / `workspace-expert` 가 작성.
- 연관: UC-04 (홈 화면 진입), UC-12 (빈 워크스페이스 자동 온보딩), UC-70 (메뉴 선택), UC-72 (URL 라우팅).

## 5. 비기능 요구사항

### 5.1 기술 스택

- **백엔드**: Kotlin 2.3.0, Spring Boot 4.0.1, Spring WebFlux (리액티브)
- **프론트엔드**: Java (GWT 2.13.0 → JavaScript 컴파일), Material Design 3
- **데이터베이스**: PostgreSQL (R2DBC, 리액티브)
- **메시징**: Kafka (Spring Cloud Stream)
- **검색**: Elasticsearch 9.3.3 (전문 검색 및 복합 필터링)
- **인프라**: Kubernetes, Helm Charts, Jib 컨테이너화
- **서비스 디스커버리**: Zookeeper

### 5.2 데이터 무결성

- 트랜잭션 경계를 통한 원자적 일괄 처리를 보장한다.
- 워크스페이스 삭제 시 종속 삭제(cascade)를 수행한다.
- 복합 인덱스를 통한 쿼리 성능을 최적화한다.
- 테이블 파티셔닝(생성 날짜 기준)을 적용한다.

### 5.3 회복성

- 외부 서비스 호출 시 3회 재시도(1초 간격, 지수 백오프)를 적용한다.
- 외부 서비스 실패 시에도 graceful degradation을 보장한다.

### 5.4 모니터링

- Spring Boot Actuator를 통한 상태 확인을 제공한다.
- Prometheus 메트릭을 수집한다.

### 5.5 품질

- 모든 모듈의 테스트 커버리지 최소 80% 유지 (Kover).
- Kotest + MockK 기반 테스트 프레임워크 사용.

### 5.6 배포

- Kubernetes 환경에 배포한다.
- Helm Chart를 통해 인프라를 관리한다.
- 프론트엔드 GWT 모듈(shell-ui, agent-ui)은 각각 독립적으로 S3에 배포하며, 모듈별 전용 CI 워크플로와 Kargo warehouse/stage를 갖는다. app 모듈(HTML/CSS/i18n)은 별도 CI 워크플로로 배포한다.

## 6. 추가 구현 요구사항

### 6.1 워크스페이스 참여 (JOIN)
- 사용자는 워크스페이스 ID를 입력하여 기존 워크스페이스에 참여할 수 있어야 한다.
- POST /workspaces/{id}/join 엔드포인트 필요.
- workspace-ui SubmitButton에서 JOIN 모드 처리 구현.

### 6.2 대시보드 API 통합
- 대시보드 프론트엔드 API URL을 워크스페이스 기반으로 수정.
- 품질 이슈 조회 엔드포인트 구현 (GET /workspaces/{id}/quality-issues).
- 에이전트 활동 이력 조회 엔드포인트 구현 (GET /workspaces/{id}/agent-activity).

### 6.3 에러 핸들링 개선
- 프론트엔드 API 호출 실패 시 사용자에게 토스트 알림 표시 (사일런트 실패 금지).
- save/delete/patch 실패 시 에러 메시지 표시 + 충돌 해결 UI.
- SSE 연결 끊김 시 자동 재연결 + 알림.

### 6.4 페이지네이션 경계 처리
- 마지막 페이지에서 Next 버튼 비활성화.
- 총 페이지 수 또는 hasMore 플래그를 API 응답에 포함.
- 결과 없음 상태 UI.

### 6.5 입력 검증 강화
- 워크스페이스 이름: 영숫자+한글+공백+하이픈+언더스코어, 최대 255자.
- 프론트엔드 클라이언트 사이드 검증 + 백엔드 서버 사이드 검증 이중화.

### 6.6 접근성 (Accessibility)
- 모든 다이얼로그에 role="dialog" 또는 role="alertdialog" 속성.
- 폼 요소에 aria-label 또는 label 연결.
- 키보드 네비게이션 지원 (Tab, Enter, Escape).

### 6.7 파일 업로드
- File 속성 타입에 대한 multipart/form-data 업로드 엔드포인트 구현.
- 저장소 백엔드 (S3 또는 로컬 파일시스템) 연동.
- 파일 확장자 검증은 기존 AttributeType.File의 extensions 규칙을 따른다.

### 6.8 사용자 설정
- 언어 선택 (ko/en) 퍼시스턴스 (localStorage 또는 서버 사이드).
- 다크/라이트 테마 전환 UI 및 퍼시스턴스.
- 설정 패널은 shell-ui Drawer 또는 별도 모달로 제공.

### 6.9 감사 로그 UI
- dashboard-ui 또는 별도 화면에서 감사 이력 조회 (누가, 언제, 무엇을 변경했는지).
- 에이전트 활동 이력과 사용자 변경 이력을 통합 타임라인으로 표시.
- 필터: 기간, 사용자, 이벤트 타입.

### 6.10 벌크 작업
- 문서 다중 선택 → 일괄 삭제, 일괄 상태 변경.
- 타입 다중 선택 → 일괄 삭제.
- 선택 UI: 체크박스 또는 Shift+클릭 범위 선택.

### 6.11 세션 관리
- 토큰 만료 전 자동 갱신 (refresh token 사용).
- 세션 만료 시 로그인 페이지로 리다이렉트 + 알림.
- 비활성 타임아웃 경고 (만료 5분 전 알림).

### 6.12 타입 버전 히스토리 UI
- 타입의 전체 버전 목록을 시각적으로 브라우징.
- 두 버전 간 diff 비교 (기존 diff API 활용).
- 타임라인 또는 리스트 뷰.

## 7. 품질 향상 요구사항

### 7.1 보안 강화
- **CORS 설정**: Gateway에 허용 도메인/메서드/헤더 명시. 프로덕션에서 와일드카드 금지.
- **CSP 헤더**: Content-Security-Policy 헤더 추가. 인라인 스크립트/스타일 제한.
- **인증 Rate Limiting**: OAuth2/JWT 엔드포인트에 요청 속도 제한 (예: 10회/분). 429 Too Many Requests 반환.
- **파일 업로드 크기 제한**: multipart max-size 설정 (예: 50MB). 초과 시 413 반환.
- **검색 쿼리 제한**: 전문 검색 쿼리 최대 1000자. Rate limiting 적용.

### 7.2 성능 최적화
- **DB 인덱스**: documents 테이블 (workspace, type, serial), (workspace, effect_date_time, expire_date_time) 복합 인덱스.
- **Elasticsearch 9.3.3 전문 검색**: 대량의 문서 검색 및 복합 필터링 성능을 위해 Elasticsearch 9.3.3을 활용한다. PostgreSQL은 원천 데이터(Source of Truth)를 보관하고, 검색 쿼리는 ES 인덱스를 통해 처리한다.
- **검색 지연 시간**: 전문 검색 및 복합 필터링 시 평균 응답 시간 200ms 이하를 유지한다.
- **데이터 동기화**: PostgreSQL 변경 사항은 Kafka 이벤트를 통해 비동기로 Elasticsearch에 반영되며, 동기화 지연 시간(Lag)은 평균 1초 이내로 관리한다.
- **Export 스트리밍**: 대량 내보내기 시 chunked transfer encoding 사용. 메모리 일괄 적재 금지.
- **WebClient/R2DBC 타임아웃**: 연결 타임아웃 5초, 요청 타임아웃 30초, 풀 유휴 타임아웃 설정.
- **R2DBC 커넥션 풀**: 최대 커넥션 수, 유효성 검사 쿼리 설정.

### 7.3 회복성 강화
- **SSE 재연결**: 클라이언트 측 exponential backoff 재연결 (1초→2초→4초→최대 30초).
- **Kafka DLQ**: 실패한 이벤트를 dead-letter-topic에 저장. 재처리 가능.
- **Webhook 실패 저장**: 실패한 웹훅 호출을 DB에 기록. 재시도 큐 구현.
- **서비스 graceful degradation**: 선택적 서비스(assistant, event-broadcaster) 장애 시 핵심 기능 유지.

### 7.4 관측성 (Observability)
- **요청 추적 ID**: Gateway에서 X-Correlation-Id 생성. 전 서비스/Kafka 헤더 전파. MDC 로깅.
- **Prometheus 메트릭**: /actuator/prometheus 노출. 지연 시간, 에러율, 큐 깊이, 커넥션 풀 모니터링.
- **구조화 로깅**: JSON 로그 포맷. 요청 ID, 사용자 ID, 워크스페이스 ID 포함.

### 7.5 UX 개선
- **빈 상태 UI**: "결과 없음", "로딩 중", "에러 발생" 상태를 시각적으로 구분.
- **삭제 확인**: 파괴적 작업(삭제, 벌크 삭제) 전 ConfirmDialog 필수.
- **성공 피드백**: 저장/삭제/생성 완료 시 SUCCESS 토스트 표시.
- **Soft Delete**: 즉시 삭제 대신 30일 보존 후 하드 삭제. 복구 가능.

### 7.6 코드 품질
- **AssistantService 분리**: 255줄 → SubAgentOrchestrator, ExecutionLifecycleManager, AuditingService 3클래스.
- **테스트 커버리지 80% (미구현)**: 전 모듈 Kover 최소 커버리지 충족. 에러 경로/타임아웃 테스트 보강.
- **누락 Javadoc 보완 (미구현)**: 헬퍼/유틸리티 클래스 문서화.
문서화.
�.
