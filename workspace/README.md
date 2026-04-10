# Workspace 모듈

워크스페이스, 사용자, 그룹, 권한 도메인을 정의한다.

## 도메인 구조

```
dev.sayaya.handbook.domain/
├── Workspace          # 워크스페이스 (테넌트 단위)
├── User               # 사용자 (여러 워크스페이스 참여 가능)
├── Group              # 그룹 (사용자 조직화 + 역할 부여 단위)
├── Permission         # 리소스 접근 권한 (값 객체, 와일드카드 지원)
├── Role               # 역할 (Permission 집합)
└── AuditLog           # 감사 로그 (불변)
```

## RBAC

구체적인 권한 체크를 위해 Permission을 생성하여 체크하고, Role 기반(hasRole, Role Hierarchy 등) 인증 매커니즘은 사용하지 않는다.

```
User ---belongs to---> Group ---has---> Role ---has---> Permission
```

### Permission 형식

`리소스:동작` 또는 `리소스:세부리소스:동작` 형태로 구성한다.
와일드카드를 지원한다: `type:*:view`

### Role 계층

- **System Role**: ADMIN (전체 시스템 접근)
- **Workspace Role**: ADMIN, GROUP_MANAGER, USER_MANAGER, TYPE_MANAGER, VIEWER, USER
- **Type Role**: MANAGER, VIEWER
- **Document Role**: MANAGER, VIEWER

### Permission 목록

| Permission | 설명 |
|---|---|
| `system:audit-logs` | 시스템 감사 로그 |
| `{workspace}:role:assign` | 역할 부여 |
| `{workspace}:group:create/delete/view` | 그룹 관리 |
| `{workspace}:user:assign/view` | 사용자 배정/조회 |
| `{workspace}:type:create/delete` | 타입 생성/삭제 |
| `{workspace}:type:{type}:view/edit` | 타입 조회/편집 |
| `{workspace}:type:{type}:document:view/edit` | 문서 조회/편집 |

## 테스트

```bash
./gradlew :workspace:test
./gradlew :workspace:koverVerify  # 커버리지 80% 이상 필수
```
