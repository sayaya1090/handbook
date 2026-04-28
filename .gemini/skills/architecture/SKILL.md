---
name: architecture
description: Handbook 프로젝트 아키텍처 및 모듈 구조 지침
---

# 아키텍처 상세

## 프론트엔드 (GWT 2.13.0)
- **ui-components**: 공유 컴포넌트 (Action, ActionManager, ChangeTracker, ToastContainer, ConfirmDialog 등)
- **shell-ui**: SPA 프레임 (Drawer, MenuRail, ToolRail, 동적 모듈 로딩)
- **document-ui**: Handsontable 6.2.4 MIT 스프레드시트 편집기
- **type-ui**: 캔버스 기반 타입 스키마 편집기
- **agent-ui**: AI 에이전트 채팅 인터페이스
- **dashboard-ui**: 워크스페이스 현황 대시보드
- **workspace-ui**: 워크스페이스 생성/조인
- **login-ui**: 터미널 스타일 로그인

## 백엔드 (Spring Boot 4.0.1 + Kotlin 2.3.0)
- **gateway**: API 게이트웨이, 메뉴 집계
- **login**: OAuth2 인증, JWT 발급
- **document-command / type-command / workspace-command**: CUD + Kafka 이벤트 발행
- **document-query / type-query**: 읽기 전용 CQRS
- **event-broadcaster**: Kafka → SSE 실시간 브로드캐스트
- **assistant**: AI 에이전트 백엔드

## 공통 패턴
- **Action + ActionManager**: Command 패턴 Undo/Redo (ui-components에 정의)
- **ChangeTracker**: 키 기반 더티 트래킹 (ui-components에 정의)
- **BehaviorSubject**: 반응형 상태 관리
- **AgentMutation**: GWT 모듈 간 CustomEvent 통신 (agent-bridge)
- **Port & Adapter**: usecase 포트를 API 어댑터가 구현 (헥사고날)

## 기술 스택
- Kotlin 2.3.0, Spring Boot 4.0.1, GWT 2.13.0, Gradle 9.3.0
- PostgreSQL 17, Kafka
- Java 21+, Playwright 1.52.0, Kotest 6.1.3, Testcontainers
