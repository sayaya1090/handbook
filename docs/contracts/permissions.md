# RBAC Permission 계약

`리소스:동작` 형식의 권한 표현 규약, 와일드카드, Role 계층.

## 공급자 (Providers)

- **login** (authentication 라이브러리) — JWT 토큰 발급 시 Role 을 claim 으로 포함
  - `authentication/src/main/kotlin/.../JwtAuthentication.kt`
- **workspace-command** — 그룹·역할 할당 관리
  - 워크스페이스 생성 시 Admin 그룹 자동 생성
- **workspace domain** — Permission 매트릭스 소유

## 소비자 (Consumers)

- **gateway** — 라우트별 권한 필터 (Spring Security)
- **command-*** / **query-*** — 각 API 핸들러에서 Permission 검증
- **assistant** — 에이전트 실행 시 사용자 권한 승계해 적용
- **shell-ui** — 메뉴 표시 필터링 (읽기 권한 기반)

## 변경 시 체크 대상

| 변경 | 체크 항목 |
|------|----------|
| 신규 Permission 패턴 추가 | authentication 라이브러리 와일드카드 매칭 로직 + 모든 적용 지점 |
| Role 계층 변경 | JWT claim 포맷 + 감사 로그 포맷 + shell-ui 메뉴 필터 |
| 필드 레벨 권한 (3.20) | `type_attributes.read_roles/write_roles` JSONB 스키마 + document-ui 마스킹 |

---

## Permission 형식

```
<scope>:<action>
<scope>:<resource>:<action>
<scope>:<resource>:<id>:<action>
```

와일드카드(`*`) 지원.

### 주요 Permission 목록

| Permission | 설명 |
|-----------|------|
| `system:audit-logs` | 시스템 감사 로그 조회 |
| `{workspace}:role:assign` | 역할 부여 |
| `{workspace}:group:create` / `delete` / `view` | 그룹 관리 |
| `{workspace}:user:assign` / `view` | 사용자 관리 |
| `{workspace}:type:create` / `delete` | 타입 생성/삭제 |
| `{workspace}:type:{type}:view` / `edit` | 특정 타입 조회/편집 |
| `{workspace}:type:{type}:document:view` / `edit` | 문서 조회/편집 |
| `{workspace}:type:{type}:attribute:{attr}:read` / `write` | 필드 레벨 권한 |

## Role 계층

| 레벨 | 역할 | 설명 |
|------|------|------|
| System | `ADMIN` | 전체 시스템 접근 |
| Workspace | `ADMIN` | 워크스페이스 전체 권한 |
| Workspace | `GROUP_MANAGER` | 그룹 관리 |
| Workspace | `USER_MANAGER` | 사용자 배정 |
| Workspace | `TYPE_MANAGER` | 타입 관리 |
| Workspace | `VIEWER` | 읽기 전용 |
| Workspace | `USER` | 기본 멤버 |
| Type | `MANAGER` / `VIEWER` | 타입별 권한 |
| Document | `MANAGER` / `VIEWER` | 문서별 권한 |

## 필드 레벨 권한 (3.20)

```json
{
  "attributes": [
    {
      "name": "salary",
      "type": "Number",
      "read_roles": ["ADMIN", "HR_MANAGER"],
      "write_roles": ["HR_MANAGER"]
    }
  ]
}
```

- `type_attributes.read_roles` / `write_roles` : JSONB 배열
- 빈 배열 = 제한 없음 (기본값)
- 값 포함 = 해당 Role 만 접근

### 적용 지점

- **API**: `document-command` / `document-query` 가 응답 시 마스킹/필터링
- **UI**: document-ui 스프레드시트가 셀 단위로 읽기 전용 / 마스킹 렌더링

## Workspace 자동 Admin 할당

워크스페이스 생성 시:
1. Admin 그룹 자동 생성
2. 생성자 → Admin 그룹 자동 배정
3. Admin 그룹에 `{workspace}:*:*` Permission 부여

## 외부 AI 에이전트 (PAT)

- PAT(Personal Access Token) 발급 시 워크스페이스·Role 범위 바인딩
- 기존 RBAC 그대로 적용 — 외부 에이전트도 권한 밖 작업 거부
- 감사 로그에 `caller_type=external_agent` 로 구분 ([audit.md](audit.md) 참조)
