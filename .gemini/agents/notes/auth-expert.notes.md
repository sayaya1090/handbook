# auth-expert Operational Notes

에이전트 자신이 갱신하는 업무 노트. 작업 패턴·반복 함정·내부 체크리스트.
이 파일은 에이전트가 직접 편집한다. 정의 파일(`auth-expert.md`) 은 건드리지 않는다.

## 요청 로그
- 2025-05-15: 권한 체크 로직 검증 → 문서간 일관성 확인, 구현체 괴리 발견

## 탐색 패턴
- **인증 구현체 검증 패턴**: `authentication` 모듈의 `Converter`와 `Authentication` 객체에서 `authorities` 추출 여부를 먼저 확인한다. (2025-05-15 발견: 현재 누락됨)
- **컨트롤러 권한 검증 패턴**: Command-side 컨트롤러에서 `@PreAuthorize` 또는 `DocumentService` 수준의 수동 검증 여부를 확인한다. (2025-05-15 발견: 현재 누락됨)
- **감사 로그 계약 준수 확인**: `assistant` 모듈의 `AuditEntry` 도메인이 `docs/contracts/audit.md`와 일치하는지 확인한다. (2025-05-15 발견: 필드 및 Enum 불일치)

## 반복 함정
(미확보)

## 내부 체크리스트
(미확보)

## 과거 실수
(미확보)

## 원칙 갱신 제안
- **계약 우선 구현 강제**: 새로운 계약(`permissions.md`, `audit.md`)이 정의된 후에는 관련 모듈의 `domain` 및 `interface` 클래스가 이를 즉시 반영하도록 하는 '계약 준수 테스트' 추가를 제안한다.

## 아카이브 요약
- 2025-05-15: RBAC 권한 체크 로직 검증 수행. `docs/requirements.md` §3.3, `docs/contracts/permissions.md`, `docs/usecases.md` 간의 논리적 일관성은 확보되었으나, 실제 백엔드 코드(`authentication`의 JWT 변환 로직, `document-command`/`type-command` 컨트롤러, `assistant`의 `AuditEntry`)에서 계약 미준수 및 구현 누락이 다수 발견됨.

---
마지막 감사: — (신규)
