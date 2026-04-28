# Onboarding-UI 모듈

워크스페이스 생성 및 참여를 담당하는 프론트엔드 모듈 (GWT). 
참여 중인 워크스페이스가 없는 신규 사용자나, 새로운 워크스페이스에 참여/생성하려는 사용자를 위한 진입점(온보딩) 역할을 수행합니다. Shell이 `ModuleScriptManager`를 통해 동적으로 로딩합니다.

## 역할 및 책임
- **워크스페이스 생성 (CREATE)**: 새로운 워크스페이스의 이름과 설명을 입력받아 생성합니다.
- **워크스페이스 참여 (JOIN)**: 기존 워크스페이스의 ID나 초대 코드를 입력받아 참여를 요청합니다.

## 도메인 모델 SSOT

이 모듈은 `Workspace`, `User` 등 공통 도메인 DTO를 직접 소유하지 않습니다. 모든 UI 공용 도메인 모델은 **`:activity`** 모듈에서 상속받아 사용하며, 이를 통해 프론트엔드 전체의 데이터 정합성을 유지합니다.

## Mount 패턴

`Application.onModuleLoad()` 은 `WindowRenderBridge.next(render)` 로 shell의 `FrameUpdater`에 Render를 위임합니다. body에 직접 append하지 않습니다. (계약 참조: `docs/contracts/frame.md`)

## 에이전트 연동

에이전트는 shell의 `AGENT_COMMAND navigate`를 통해 `/workspaces` URL로 이 화면에 진입시킬 수 있습니다.
또한 `AgentMutation`를 통해 `WS_MODE`, `WS_INPUT`, `WS_SUBMIT`, `WS_CREATE` 등의 명령어로 생성 UI를 원격 제어할 수 있습니다.

## 계층 구조

```text
client/
├── usecase/         CreateWorkspaceMode (CREATE/JOIN), CreateWorkspaceParam, WorkspaceRepository (포트)
└── onboarding/      ContentElement, DialogElement, SectionElement (@AssistedFactory),
                     SectionElementFactory, SubmitButton
└── interfaces/
    └── api/         WorkspaceApi (POST /workspace, POST /workspace/{id}/join), ApiModule
```

## 기능

### 워크스페이스 생성/참여 다이얼로그

두 가지 모드를 라디오 버튼으로 전환합니다:

- **CREATE**: 새 워크스페이스 이름 입력 -> 생성 버튼 클릭 -> `POST /workspace`
- **JOIN**: 기존 워크스페이스 ID 입력 -> 참여 요청 -> `POST /workspace/{id}/join`

### UI 구성

```text
ContentElement (전체 화면, 중앙 정렬)
└── DialogElement (카드 스타일 다이얼로그)
    ├── SectionElement (CREATE - 라디오 + 라벨 + 이름 입력)
    ├── Divider ("or")
    ├── SectionElement (JOIN - 라디오 + 라벨 + ID 입력)
    └── SubmitButton (모드에 따라 라벨 변경)
```

### API

온보딩 모듈은 워크스페이스의 CUD 중 생성(Create)과 참여(Join)에만 관여합니다. 수정(PUT)과 삭제(DELETE)는 `workspace-ui` 모듈이 담당합니다.

| Method | Path | 설명 |
|--------|------|------|
| POST | `/workspace` | 새로운 워크스페이스 생성 |
| POST | `/workspace/{id}/join` | 기존 워크스페이스 참여 요청 |

> 상세 유스케이스는 [USECASE.md](USECASE.md) 참조.

## 설계 결정

| 결정 | 이유 |
|------|------|
| SectionElementFactory (@AssistedFactory) | CREATE/JOIN 섹션을 같은 컴포넌트로 파라미터만 바꿔 생성 |
| CreateWorkspaceMode BehaviorSubject | 라디오 버튼 + 입력 필드 + 버튼 라벨이 모드에 반응적으로 동기화 |
| CreateWorkspaceParam BehaviorSubject | 입력값이 null/빈 문자열이면 버튼 자동 disabled |
| i18n LabelProvider 적용 | 모든 레이블 다국어 지원 |

## 실행

```bash
# DevMode
./gradlew :onboarding-ui:gwtDev

# 컴파일
./gradlew :onboarding-ui:compileJava
```
