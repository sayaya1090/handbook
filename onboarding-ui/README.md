# Workspace-UI 모듈

워크스페이스 생성/참여 프론트엔드 (GWT). Shell이 ModuleScriptManager로 동적 로딩한다.

## Mount 패턴

`Application.onModuleLoad()` 은 `WindowRenderBridge.next(render)` 로 shell 의
`FrameUpdater` 에 Render 를 위임한다. body 직접 append 금지 — 계약은
[`docs/contracts/frame.md`](../docs/contracts/frame.md).

## 에이전트 연동

UI 전용 모듈로 백엔드 API 공급 없음. 에이전트는 shell 의 `AGENT_COMMAND navigate`
(URL 기반) 또는 MenuSelected 스트림(UC-12 빈 워크스페이스 온보딩) 으로 이 화면을
진입시킨다. mutate/highlight 는 후속.

## 계층 구조

```
client/
├── usecase/         CreateWorkspaceMode (CREATE/JOIN), CreateWorkspaceParam, WorkspaceRepository (포트)
└── interfaces/
    ├── api/         WorkspaceApi (POST/PUT/DELETE), ApiModule
    └── create/      ContentElement, DialogElement, SectionElement (@AssistedFactory),
                     SectionElementFactory, SubmitButton
```

## 기능

### 워크스페이스 생성 다이얼로그

두 가지 모드를 라디오 버튼으로 전환한다:

- **CREATE**: 새 워크스페이스 이름 입력 -> 생성 버튼 클릭 -> POST /workspace
- **JOIN**: 기존 워크스페이스 ID 입력 -> 참여 요청

### UI 구성

```
ContentElement (전체 화면, 중앙 정렬)
└── DialogElement (카드 스타일 다이얼로그)
    ├── SectionElement (CREATE - 라디오 + 라벨 + 이름 입력)
    ├── Divider ("or")
    ├── SectionElement (JOIN - 라디오 + 라벨 + ID 입력)
    └── SubmitButton (모드에 따라 라벨 변경)
```

### API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/workspace` | 워크스페이스 생성 |
| PUT | `/workspace/{id}` | 워크스페이스 수정 |
| DELETE | `/workspace/{id}` | 워크스페이스 삭제 |

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

## 모바일 지원

- **다이얼로그**: 모바일에서 전체 화면 다이얼로그로 전환. 입력 필드가 가상 키보드에 가려지지 않도록 스크롤 조정.
- **라디오 버튼(CREATE/JOIN)**: 터치 영역을 48px 이상으로 확보.
- **입력 필드**: 모바일에서 전체 너비로 확장.

## 의존성

activity (FetchApi, LabelProvider, Menu), ui-components
