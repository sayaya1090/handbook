# 문서 구조

## 프로젝트 레벨 (docs/)
| 파일 | 역할 |
|------|------|
| requirements.md | 기능/비기능 요구사항 |
| architecture.md | 시스템 아키텍처, 모듈 구조 |
| design.md | UI/UX 디자인 시스템 (MD3 토큰) |
| usecases.md | 글로벌 유스케이스 (UC-01~UC-93) |
| design-patterns.md | 공통 설계 패턴 (Action, 더티 트래킹, 프레즌스) |
| error-handling.md | 오류 처리 전략 |
| kafka-events.md | 이벤트 카탈로그 (토픽, 발행/구독, SSE 흐름) |
| database-schema.md | DB 스키마 (ER 다이어그램, 테이블 상세, 설계 결정) |
| development.md | 빌드/테스트 가이드 |

## 모듈 레벨 (각 모듈/)
| 파일 | 역할 | 필수 섹션 |
|------|------|----------|
| README.md | 모듈 요약 (목적, 컴포넌트, API, 실행) | **에이전트 연동** (내부 assistant · 외부 AI Tool Use · MCP) |
| DESIGN.md | 모듈 전용 설계 — 설계 결정이 복잡한 모듈에만 작성 | — |
| USECASE.md | 모듈 유스케이스 + 시퀀스 다이어그램 | 트레이서빌리티 매트릭스, **에이전트 연동 시나리오** |
| CLASS-DIAGRAM.md | 클래스 구조 (mermaid) | — |

## 크로스체크 매트릭스
| 변경 유형 | 체크 대상 |
|-----------|-----------|
| 클래스/패키지 경로 변경 | docs/architecture.md, 모듈/CLASS-DIAGRAM.md, 모듈/README.md |
| 요구사항 추가/변경 | docs/requirements.md, docs/usecases.md, 모듈/USECASE.md, 모듈/DESIGN.md |
| 디자인 토큰/시각 상태 변경 | docs/design.md, docs/design-patterns.md, 모듈/DESIGN.md |
| API 엔드포인트 변경 | docs/requirements.md (4. API 엔드포인트), 모듈/README.md, **모듈/README.md#에이전트-연동**, docs/contracts/api.md |
| 유스케이스 추가 | docs/usecases.md (글로벌 UC), 모듈/USECASE.md (트레이서빌리티 매트릭스) |
| 신규 모듈 추가 | 위 4종 + `docs/contracts/README.md` 매트릭스 등록 + `.gemini/agents/<expert>.md` 스코프 확장 + **에이전트 연동 섹션** 필수 |

---

## 에이전트 연동 섹션 (표준 템플릿)

모든 모듈 `README.md` 와 `USECASE.md` 에 다음 섹션이 있어야 한다.
체크리스트는 GEMINI.md "에이전트 연동 체크리스트" 와 일치한다.

### README.md#에이전트 연동 (필수)

```markdown
## 에이전트 연동

### 내부 assistant
- 호출 경로: <직접 REST | AGENT_COMMAND 대상>
- 시나리오: <자연어 요청 예시 → 이 모듈의 역할>

### 외부 AI (Tool Use)
- 노출 엔드포인트: <GET /... | 없음>
- OpenAPI `summary` / `description` 기입 위치: <Controller 메서드 경로>
- 감사 경로: `caller_type=EXTERNAL_AGENT` → `AuditEntry`

### (후속) MCP
- 관련 Tool 매니페스트: <이름 | 미정>

### Agent Command 타겟
- navigate: <menu, tool 키워드>
- highlight/mutate selector 패턴: <CSS 선택자 예>
```

### USECASE.md#에이전트 연동 시나리오 (선택 — 에이전트가 주요 사용자면 필수)

시퀀스 다이어그램으로 "assistant / 외부 AI → 이 모듈" 흐름 1개 이상 작성.
트레이서빌리티 매트릭스에 UC 번호 추가 (`docs/usecases.md` §3.17 / §3.23 와 연결).

### 면제 조건

- 완전 내부 전용 모듈 (예: `event/`, `search/` 공용 라이브러리) 은 섹션 자체를 생략 가능.
  단 README 최상단에 `**에이전트 연동: 없음 (내부 전용).**` 한 줄 명시.
