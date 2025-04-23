## 도메인

### RBAC
구체적인 권한 체크를 위해 Permission을 생성하여 체크하고, Role 기반(hasRole, Role Hierarchy 등) 인증 매커니즘은 사용하지 않는다.

```
User ---belongs to---> Group ---has---> Role ---has---> Permission
```

```kotlin
@PreAuthorize("hasPermission(#workspace, 'group:edit')")

@PreAuthorize("hasPermission(#workspace, #type, 'edit')") 
```

#### Permissions
`리소스:동작` 또는 `리소스:세부리소스:동작` 형태로 구성\
예시: `workspace:manage-users`, `type:create`, `type:customer:edit`\
와일드카드 지원: `type:*:view` (모든 타입에 대한 조회 권한)


  - system:audit-logs
  - {workspace}:role:assign
  - {workspace}:group:create
  - {workspace}:group:edit
  - {workspace}:group:delete
  - {workspace}:group:view
  - {workspace}:user:assign
  - {workspace}:user:view
  - {workspace}:type:create
  - {workspace}:type:delete
  - {workspace}:type:{type}:view 
  - {workspace}:type:{type}:edit

#### System Role
시스템 사용과 관련된 Role
  - ADMIN: System Role을 부여할 수 있다. 모든 Role을 포함한다.
    - system:audit-logs
    - *:role:assign
    - *:group:create
    - *:group:edit
    - *:group:delete
    - *:user:assign
    - *:user:view
    - *:group:view
    - *:type:create
    - *:type:delete
    - \*:type:*:view
    - \*:type:*:edit
    - \*:type:*:document:view
    - \*:type:*:document:edit

#### Workspace Role
워크스페이스 관리
  - {WORKSPACE}_ADMIN: 워크스페이스에 Workspace Role을 부여할 수 있다. 모든 워크스페이스 Role을 포함한다.
    - {workspace}:role:assign
    - {workspace}:edit
    - {workspace}:view
    - {workspace}:group:create
    - {workspace}:group:edit
    - {workspace}:group:delete
    - {workspace}:user:assign
    - {workspace}:user:view
    - {workspace}:group:view
  - {WORKSPACE}_GROUP_MANGER: 워크스페이스에 그룹을 생성할 수 있다. 유저 매니저 권한을 포함한다.
    - {workspace}:view
    - {workspace}:group:create
    - {workspace}:group:edit
    - {workspace}:group:delete
    - {workspace}:user:assign
    - {workspace}:user:view
    - {workspace}:group:view
  - {WORKSPACE}_USER_MANAGER: 워크스페이스 그룹에 사용자를 배정할 수 있다.
    - {workspace}:view
    - {workspace}:user:assign
    - {workspace}:user:view
    - {workspace}:group:view
  - {WORKSPACE}_TYPE_MANAGER: 워크스페이스에 타입을 생성, 편집할 수 있다. 모든 타입에 대한 타입 에디터 권한을 포함한다.
    - {workspace}:view
    - {workspace}:type:create
    - {workspace}:type:delete
    - {workspace}:type:*:view
    - {workspace}:type:*:edit
  - {WORKSPACE}_VIEWER: 워크스페이스에 있는 모든 타입을 볼 수 있다.
    - {workspace}:view
    - {workspace}:type:*:view
  - {WORKSPACE}_USER: 워크스페이스에 포함된 일반 사용자
    - {workspace}:view

#### Type Role
  - {WORKSPACE}_{TYPE}_MANAGER: 특정 타입을 수정할 수 있다. 뷰어 권한을 포함한다.
    - {workspace}:type:{type}:view
    - {workspace}:type:{type}:edit
  - {WORKSPACE}_{TYPE}_VIEWER: 특정 타입을 읽을 수 있다.
    - {workspace}:type:{type}:view

#### Document Role
  - {WORKSPACE}_DOCUMENT_MANAGER: 문서를 수정할 수 있다. 
    - {workspace}:type:{type}:document:view
    - {workspace}:type:{type}:document:edit
    - {WORKSPACE}_DOCUMENT_VIEWER: 문서를 읽을 수 있다.
    - {workspace}:type:{type}:document:view