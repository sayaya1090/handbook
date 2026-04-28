# REST API 엔드포인트 계약

Gateway 를 통해 노출되는 공개 REST 엔드포인트 카탈로그.

## 공급자 (Providers)

- **login** — `/auth/*`, `/oauth2/*`, `/login/oauth2/*`, `/user`
- **gateway** — `/menus` (집계)
- **type-command** / **type-query** — `/workspace/{ws}/types/**`
- **document-command** / **document-query** — `/workspace/{ws}/documents/**`, `/workspace/{ws}/{type}/**`
- **workspace-command** — `/workspace` (POST), `/workspace/{ws}/*/presence`
- **event-broadcaster** — `/workspace/{ws}/messages` (SSE)
- **assistant** — `/assistant/**`
- **(후속) mcp-server** — `/mcp/**` (MCP 프로토콜)

## 소비자 (Consumers)

- **shell-ui / 각 UI activity** — FetchApi 를 통한 REST 호출
- **외부 AI 에이전트** — `/openapi.json` 참조 후 function calling
- **외부 시스템** — 웹훅 수신 + 직접 API 호출

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 엔드포인트 추가 | OpenAPI 자동 생성 확인 + 게이트웨이 라우트 등록 + `docs/contracts/api.md` 표 업데이트 |
| 경로/메서드 변경 | 모든 프론트엔드 FetchApi 클래스 + 외부 공개면 의미체계 버저닝 |
| 응답 스키마 변경 | 모든 소비자 역직렬화 + `application/vnd.sayaya.handbook.v*+json` 버저닝 |
| 인증 요구 변경 | gateway Security 설정 + 감사 로그 `caller_type` |

---

## 인증 / 사용자

| Method | Path | 설명 |
|--------|------|------|
| GET | `/auth/refresh` | JWT 토큰 갱신 |
| GET | `/oauth2/authorization/{provider}` | OAuth2 로그인 시작 |
| GET | `/login/oauth2/code/{provider}` | OAuth2 콜백 |
| GET | `/user` | 현재 사용자 정보 |
| GET | `/menus` | 메뉴 목록 (집계) |

## 워크스페이스

| Method | Path | 설명 |
|--------|------|------|
| POST | `/workspace` | 워크스페이스 생성 |
| POST | `/workspace/{id}/join` | 워크스페이스 조인 |
| POST | `/workspace/{ws}/groups` | 그룹 생성 |
| GET | `/workspace/{ws}/groups` | 그룹 목록 조회 |
| DELETE | `/workspace/{ws}/groups/{gid}` | 그룹 삭제 |
| POST | `/workspace/{ws}/groups/{gid}/members/{uid}` | 그룹 멤버 추가 |
| DELETE | `/workspace/{ws}/groups/{gid}/members/{uid}` | 그룹 멤버 삭제 |
| GET | `/workspace/{ws}/groups/{gid}/roles` | 그룹의 역할 조회 |
| POST | `/workspace/{ws}/groups/{gid}/roles` | 그룹에 역할 부여 |
| DELETE | `/workspace/{ws}/groups/{gid}/roles/{role}` | 그룹의 역할 제거 |

## 타입

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspace/{ws}/types` | 타입 목록 (날짜 필터링) |
| GET | `/workspace/{ws}/types/{type}?version=` | 특정 타입 버전 |
| PUT | `/workspace/{ws}/types` | 타입 일괄 저장 (새 버전) |
| PATCH | `/workspace/{ws}/types` | 타입 부분 업데이트 |
| DELETE | `/workspace/{ws}/types` | 타입 일괄 삭제 |
| GET | `/workspace/{ws}/types/{type}/diff?v1=&v2=` | 버전 간 diff |
| GET | `/workspace/{ws}/layouts` | 레이아웃 목록 |

## 문서

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspace/{ws}/documents` | 문서 검색 (페이지네이션) |
| GET | `/workspace/{ws}/{type}/{serial}` | 특정 문서 조회 |
| GET | `/workspace/{ws}/{type}/{serial}?date=` | 특정 시점 문서 |
| PUT | `/workspace/{ws}/documents` | 일괄 저장 (새 버전) |
| PATCH | `/workspace/{ws}/documents` | 부분 업데이트 |
| DELETE | `/workspace/{ws}/documents` | 일괄 삭제 |
| GET | `/workspace/{ws}/{type}/{serial}/diff?date1=&date2=` | 시점 간 diff |
| PATCH | `/workspace/{ws}/documents/{id}/status` | 상태 전이 (DRAFT/REVIEW/PUBLISHED) |
| POST | `/workspace/{ws}/documents/import` | 일괄 임포트 (CSV/JSON) |
| GET | `/workspace/{ws}/documents/export` | 일괄 익스포트 |

## 실시간 / 정합성

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspace/{ws}/messages` | SSE 이벤트 스트림 ([sse.md](sse.md)) |
| POST | `/workspace/{ws}/presence` | 프레즌스 (편집 위치 공유) |
| GET | `/workspace/{ws}/compliance` | 호환성 검증 결과 |

## AI 어시스턴트 (내부)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/assistant/request` | 자연어 요청 → 실행 계획 생성 |
| POST | `/assistant/execute` | 실행 계획 확정 실행 |
| POST | `/assistant/abort` | 실행 중단 (`executionId`) |
| POST | `/assistant/respond` | 사용자 확인 응답 |
| GET | `/assistant/executions` | 실행 상태/진행률 |
| GET | `/assistant/artifacts` | 결과 아티팩트 |
| POST | `/assistant/quality/scan?workspace={id}` | 즉시 품질 스캔 |
| GET | `/assistant/audit` | 감사 추적 조회 ([audit.md](audit.md)) |

## 대시보드

| Method | Path | 설명 |
|--------|------|------|
| GET | `/workspace/{ws}/dashboard` | 워크스페이스 현황 |
| GET | `/workspace/{ws}/stats/timeline?from=&to=&interval=` | 시계열 통계 |
| GET | `/workspace/{ws}/stats/distribution` | 타입별 문서 분포 |
| GET | `/workspace/{ws}/quality-issues` | 품질 이슈 목록 |
| GET | `/workspace/{ws}/agent-activity` | 에이전트 활동 |
| GET | `/workspace/{ws}/audit-logs` | 감사 로그 |

## 웹훅

| Method | Path | 설명 |
|--------|------|------|
| POST | `/workspace/{ws}/webhooks` | 웹훅 등록 |
| GET | `/workspace/{ws}/webhooks` | 목록 |
| DELETE | `/workspace/{ws}/webhooks/{id}` | 삭제 |

## 디스커버리 (외부 AI / SEO)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/openapi.json` | OpenAPI 3.0 스펙 (공개, 인증 불필요) |
| GET | `/` | SEO 랜딩 ko ([landing](../requirements/landing.md)) |
| GET | `/en/` | SEO 랜딩 en |
| GET | `/sitemap.xml` | 사이트맵 |
| GET | `/robots.txt` | 크롤러 지시 |
| GET | `/llms.txt` | AI 에이전트 디스커버리 요약 |
| GET | `/llms-full.txt` | AI 에이전트 디스커버리 상세 |
| GET | `/app.html` | 앱 셸 (`noindex, follow`) |

## 미디어 타입 버저닝

공개 응답은 `application/vnd.sayaya.handbook.v1+json` 로 버저닝.
파괴적 스키마 변경 시 `v2` 신설, 기존 `v1` 일정 기간 병행.

## 인증 방식

| 용도 | 방식 |
|------|------|
| 브라우저 세션 | HTTP-only Secure 쿠키 JWT (RS256) |
| 외부 에이전트 / 스크립트 | API Key / Bearer Token (PAT) |
| OpenAPI 스펙 조회 | 인증 불필요 (공개) |

## JWT 클레임 구조 (Phase 1a — 2026-04-18)

발급된 JWT 는 다음 클레임을 포함한다. **소비자는 반드시 `sub` 를 사용자 식별자로 사용**해야 하며 `jti` 를 그 용도로 사용하면 토큰 재발행 시 격리가 깨진다.

| 클레임 | 의미 | 안정성 |
|--------|------|--------|
| `sub` | 내부 사용자 UUID (`user.id`) | **영구** — 재발급 시에도 불변. 서비스 간 `user_id` 참조의 유일한 원천. |
| `jti` | 토큰 고유 ID (`UUID.randomUUID()`) | 매 토큰마다 고유. 감사/블랙리스트/중복 탐지 용도. |
| `iss` | 발행자 (`JWT_PUBLISHER`, 기본 `handbook`) | 고정 |
| `iat` / `nbf` / `exp` | 발행·유효시작·만료 시각 (UTC epoch) | 토큰별 |
| `name` | 표시명 (OAuth provider 가 준 사람이 읽는 이름) | 사용자가 바꿀 수 있음 — 식별 용도 금지 |
| `authorities` | 역할 목록 (예: `["USER"]`, `["ADMIN","USER"]`) | 세션별 스냅샷 |

### 소비자 계약

- `UserAuthentication.sub` : 내부 사용자 UUID (Phase 1a 기준 nullable — 레거시 호환. Phase 1b 에서 non-null 로 강화)
- `UserAuthentication.id`  : `jti`. 신규 코드에서 사용자 식별 용도 사용 금지
- `@AuthenticationPrincipal UserAuthentication` 으로 주입받아 `.sub` 로 `user_id` 추출

### 레거시 호환 기간

Phase 1a 배포 전에 발급된 토큰은 `sub` 없이 `jti` 에 사용자 UUID 가 심어진 형태다. `UserController` `/auth/refresh` `TokenPublisher.validateRefreshToken` 은 `sub ?: id` 폴백 정책으로 양 포맷을 수용한다. 소비자 전환(Phase 1b workspace-command, 2a/2b search-*·shell-ui) 완료 후 폴백을 제거한다.

## Rate Limiting

- 인증 엔드포인트: 10회/분
- 일반 API: PAT 토큰 단위로 분당 한도
- 외부 AI 에이전트: 동일 규칙 적용 (`caller_type=external_agent`)
