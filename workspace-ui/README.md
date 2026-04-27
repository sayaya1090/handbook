# Workspace-UI 모듈

워크스페이스 관리 기능을 제공하는 프론트엔드 모듈 (GWT).
사용자가 워크스페이스에 진입한 후의 메타데이터(이름, 설명), 그룹(조직), 사용자 할당, 권한(역할)을 관리하는 전용 대시보드 및 도구 모음입니다.

## 역할 및 책임
- **워크스페이스 정보 (Info)**: 워크스페이스 메타데이터 수정 및 삭제 (Delete Workspace).
- **그룹 관리 (Groups)**: 그룹 생성/삭제 및 사용자 할당 (Assign).
- **권한 관리 (Permissions)**: 그룹에 대한 역할(Role) 및 권한 부여 (Grant).

## 라우팅
- 진입점 URL: `/workspace/{workspaceId}` (기본 탭) 또는 `/workspace/{workspaceId}/groups` 등
- Shell이 `js/workspace/workspace.nocache.js`를 동적 로딩하여 프레임에 주입합니다.

## 모듈 분리 배경
초기 설계에서는 워크스페이스 생성/참여 기능을 함께 담당하였으나, 단일 책임 원칙(SRP) 및 초기 로딩 성능 최적화를 위해 생성/참여 기능은 `onboarding-ui` 모듈로 분리되었습니다. 현재 본 모듈은 **활성화된 워크스페이스 컨텍스트 내에서의 관리 작업**에만 집중합니다.
