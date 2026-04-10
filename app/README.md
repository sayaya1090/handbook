# App 모듈

Shell-UI와 Agent-UI를 하나의 GWT 애플리케이션으로 조합하는 Composition Root.
비즈니스 로직 없이 Dagger Component 정의와 EntryPoint만 포함한다.

## 구조

```
client/
├── Component        Dagger 컴포넌트 (Shell + Agent 모듈 조합)
└── Application      GWT EntryPoint — 각 Initializer.initialize() 호출
```

## 조합 패턴

```java
Component component = DaggerComponent.create();
component.shell().initialize();   // Shell이 자기 DOM 배치
component.agent().initialize();   // Agent가 자기 DOM 배치
```

각 모듈의 Initializer가 `body()`에 자기 요소를 직접 배치한다.
App은 내부 구조를 알 필요 없다.

## 공유 상태

Shell-UI와 Agent-UI는 서로 직접 의존하지 않는다.
`HostSharedModule`의 `BehaviorSubject`를 통해 연결된다:

| 공유 상태 | 용도 |
|-----------|------|
| `Observable<Progress>` | API 로딩 + 에이전트 진행률 → 프로그레스 바 |
| `Observable<String> uri` | NavigateHandler → Shell 라우팅 |
| `Observable<Render>` | 도구 선택 → 프레임 렌더링 |

## 새 feature 모듈 추가 시

1. `Component.java`에 해당 모듈의 Dagger Module 추가
2. `Component` 인터페이스에 `XxxInitializer` 노출
3. `Application.java`에서 `component.xxx().initialize()` 호출

## PWA 지원

- `manifest.json`: PWA 매니페스트 (아이콘, 테마 등)
- `service-worker.js`: 정적 리소스 캐싱 (manifest.json 등)
- `app.html`에서 서비스 워커 자동 등록

## 실행

```bash
# DevMode
./gradlew :app:gwtDev

# 컴파일
./gradlew :app:compileGwt
```

## 개발 환경

프로젝트 루트의 `docker-compose.yml`을 사용하여 로컬 개발 환경을 구성할 수 있다.

```bash
docker compose up -d
```
