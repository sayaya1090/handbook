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
| 파일 | 역할 |
|------|------|
| README.md | 모듈 요약 (목적, 컴포넌트, API, 실행) |
| DESIGN.md | 모듈 전용 설계 — 설계 결정이 복잡한 모듈에만 작성 |
| USECASE.md | 모듈 유스케이스 + 시퀀스 다이어그램 |
| CLASS-DIAGRAM.md | 클래스 구조 (mermaid) |

## 크로스체크 매트릭스
| 변경 유형 | 체크 대상 |
|-----------|-----------|
| 클래스/패키지 경로 변경 | docs/architecture.md, 모듈/CLASS-DIAGRAM.md, 모듈/README.md |
| 요구사항 추가/변경 | docs/requirements.md, docs/usecases.md, 모듈/USECASE.md, 모듈/DESIGN.md |
| 디자인 토큰/시각 상태 변경 | docs/design.md, docs/design-patterns.md, 모듈/DESIGN.md |
| API 엔드포인트 변경 | docs/requirements.md (4. API 엔드포인트), 모듈/README.md |
| 유스케이스 추가 | docs/usecases.md (글로벌 UC), 모듈/USECASE.md (트레이서빌리티 매트릭스) |
