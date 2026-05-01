# Workspace 모듈

**에이전트 연동: 없음 (내부 전용).**

백엔드(JVM)와 프론트엔드(GWT)가 공유하는 워크스페이스, 사용자, 그룹 및 권한 체계를 관리하는 공용 도메인 모듈.

## 핵심 역할
- **공용 도메인 모델**: `Workspace`, `User`, `Group` 등 시스템 전반의 조직 구조를 정의.
- **권한 체계 (RBAC)**: `Role`, `Permission`을 통한 세밀한 접근 제어 로직 제공.
- **통합 API 계약**: `WorkspaceApi` 인터페이스를 통해 서버와 클라이언트 간의 관리 기능을 추상화.

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

## 개발 및 테스트
- **GWT 라이브러리**: JAR에 Java 소스를 포함하여 UI 모듈에서 상속 및 제로 카피 사용 가능.
- **테스트 전략**: 
    - 백엔드: 도메인 유효성 체크 및 권한 매칭 로직 검증 (`WorkspaceTest`, `PermissionTest` 등).
    - 프론트엔드: 런타임 빌더 및 필드 접근성 검증 (`WorkspaceDomainTest`).
