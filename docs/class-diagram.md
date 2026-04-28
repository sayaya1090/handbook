# Handbook - 클래스 다이어그램

각 모듈의 클래스 다이어그램은 해당 모듈 디렉토리의 `CLASS-DIAGRAM.md`에 위치한다.

## 도메인 모듈

- [workspace](../workspace/CLASS-DIAGRAM.md) — 워크스페이스, 사용자, 그룹
- [schema](../schema/CLASS-DIAGRAM.md) — 타입 시스템, 속성, 검증기
- [document](../document/CLASS-DIAGRAM.md) — 문서, 타입 레이아웃, 정합성
- [event](../event/CLASS-DIAGRAM.md) — 이벤트 시스템

## 백엔드 서비스 모듈

- [authentication](../authentication/CLASS-DIAGRAM.md) — JWT 인증
- [login](../login/CLASS-DIAGRAM.md) — OAuth2 로그인 + JWT 발행
- [gateway](../gateway/CLASS-DIAGRAM.md) — API 게이트웨이
- [workspace-command](../workspace-command/CLASS-DIAGRAM.md) — 워크스페이스 CRUD
- [type-command](../type-command/CLASS-DIAGRAM.md) — 타입 CRUD
- [document-command](../document-command/CLASS-DIAGRAM.md) — 문서 CRUD
- [type-query](../type-query/CLASS-DIAGRAM.md) — 타입 검색 (읽기 전용)
- [document-query](../document-query/CLASS-DIAGRAM.md) — 문서 검색 (읽기 전용)
- [assistant](../assistant/CLASS-DIAGRAM.md) — AI 비서
- [event-broadcaster](../event-broadcaster/CLASS-DIAGRAM.md) — 이벤트 브로드캐스트

## 프론트엔드 공유 모듈

- [activity](../activity/CLASS-DIAGRAM.md) — 공유 도메인 (Menu, Tool, Labels)
- [agent-bridge](../agent-bridge/CLASS-DIAGRAM.md) — 에이전트 ↔ 편집 모듈 브릿지
- [agent-protocol](../agent-protocol/CLASS-DIAGRAM.md) — 에이전트 커맨드 프로토콜
- [ui-components](../ui-components/CLASS-DIAGRAM.md) — 공통 UI 컴포넌트

## 프론트엔드 UI 모듈

- [login-ui](../login-ui/CLASS-DIAGRAM.md) — 로그인/로그아웃 UI
- [shell-ui](../shell-ui/CLASS-DIAGRAM.md) — SPA 프레임 (Drawer, Menu Rail, Tool Rail)
- [agent-ui](../agent-ui/CLASS-DIAGRAM.md) — 에이전트 UI (SSE, 커맨드 핸들러)
- [type-ui](../type-ui/CLASS-DIAGRAM.md) — 타입 스키마 캔버스 편집기
- [document-ui](../document-ui/CLASS-DIAGRAM.md) — 스프레드시트 문서 편집기
- [workspace-ui](../workspace-ui/CLASS-DIAGRAM.md) — 워크스페이스 생성/참여
- [app](../app/CLASS-DIAGRAM.md) — 메인 GWT 엔트리포인트
