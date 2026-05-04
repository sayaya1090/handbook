# App 모듈

정적 자산 호스트. HTML, CSS, vendor JS, i18n 만 포함한다. GWT 컴파일 없음. Java 코드 없음 (`plugins { war }` 만 적용).

shell-ui 와 agent-ui 는 각각 독립 GWT 모듈로 컴파일·S3 배포되며, `app.html` 이 `shell/shell.nocache.js` + `agent/agent.nocache.js` 를 별도 `<script>` 로 로드한다. 모듈 간 통신은 agent-bridge 의 window 브릿지(`WindowProgressBridge`, `WindowUriBridge`, `WindowLabelBridge`)를 통해 이루어진다.

## 구조

```
src/main/webapp/
├── app.html               # 엔트리 HTML — shell·agent nocache.js 로드
├── css/                    # 전역 스타일시트
├── js/                     # vendor JS + 머지된 i18n (language.{locale}.json)
├── manifest.json           # PWA 매니페스트
└── service-worker.js       # 정적 리소스 캐싱
```

## 빌드

```bash
# i18n 머지 + WAR 패키징
./gradlew :app:war
```

## CI/배포

`app-deploy.yaml` 워크플로가 HTML/CSS/i18n 변경 시 트리거된다. GWT 모듈(`_frontend-deploy.yaml`)과는 별도 파이프라인이다.

## PWA 지원

- `manifest.json`: PWA 매니페스트 (아이콘, 테마 등)
- `service-worker.js`: 정적 리소스 캐싱 (manifest.json 등)
- `app.html`에서 서비스 워커 자동 등록

## 에이전트 연동
**에이전트 연동: 없음 (정적 자산 호스트).**
실제 에이전트와의 상호작용은 본 HTML에 포함된 `shell-ui` 및 `agent-ui` 모듈에서 담당한다.

## 개발 환경

프로젝트 루트의 `docker-compose.yml`을 사용하여 로컬 개발 환경을 구성할 수 있다.

```bash
docker compose up -d
```
