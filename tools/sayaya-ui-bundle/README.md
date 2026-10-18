# sayaya-ui.js 번들 설치

`dev.sayaya:ui` jar 에는 Java builder 만 있고 커스텀 엘리먼트 JS 구현은 들어 있지 않다.
앱과 테스트 HTML 은 저장소에 포함된 `*/js/sayaya-ui.js`(classic script)를 로드하므로,
jar 버전을 올릴 때는 **같은 버전의 번들**로 모든 복사본을 교체해야 한다.

번들은 **sayaya-ui 가 공개 산출물로 제공하는 파일을 그대로 쓴다.** handbook 은 sayaya-ui 저장소를
직접 읽거나 빌드하지 않는다(다른 컴패니언의 작업공간 경계). 번들 형태·배포 위치는 sayaya-ui 에 요청해 정한다.

## 공개 번들 위치 (sayaya-ui 2.5.0.3~)

sayaya-ui 가 GitHub Release 에 단일 classic-script 번들(@material/web all.js IIFE + labs IIFE, Material Web 본체 포함)과
sha256 체크섬을 첨부한다(sayaya-ui 컴패니언 회신 기준).

```
https://github.com/sayaya1090/sayaya-ui/releases/download/v<version>/sayaya-ui-<version>.js
https://github.com/sayaya1090/sayaya-ui/releases/download/v<version>/sayaya-ui-<version>.js.sha256
```

- 번들 헤더 주석에 sayaya-ui 버전·빌드 커밋·@material/web 버전이 적혀 있다.
- 2.5.0.2 이하는 공개 번들이 없다(현재 커밋된 2.5.0.2 번들의 빌드 커밋은 미확인).
- 받은 뒤 `shasum -a 256` 결과가 `.sha256` 파일과 같은지 확인한다.

```sh
tools/sayaya-ui-bundle/install.sh <번들 경로|https URL>            # 검증만
tools/sayaya-ui-bundle/install.sh <번들 경로|https URL> --install  # 추적 중인 복사본 전부 교체
```

- 검증: 최상위 import/export 없음(classic script), `node --check`, 주요 태그
  (md-dialog, md-elevation, md-toast, md-navigation-rail, md-top-app-bar, md-bottom-sheet) 존재.
- `--install` 후 복사본 md5 가 모두 같은지 출력한다. 이어서 `./gradlew test` 를 기준선과 비교한다.
- 필요: node, curl(URL 일 때).
