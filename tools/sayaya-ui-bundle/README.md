# sayaya-ui.js 번들 설치

`dev.sayaya:ui` jar 에는 Java builder 만 있고 커스텀 엘리먼트 JS 구현은 들어 있지 않다.
앱과 테스트 HTML 은 저장소에 포함된 `*/js/sayaya-ui.js`(classic script)를 로드하므로,
jar 버전을 올릴 때는 **같은 버전의 번들**로 모든 복사본을 교체해야 한다.

번들은 **sayaya-ui 가 공개 산출물로 제공하는 파일을 그대로 쓴다.** handbook 은 sayaya-ui 저장소를
직접 읽거나 빌드하지 않는다(다른 컴패니언의 작업공간 경계). 번들 형태·배포 위치는 sayaya-ui 에 요청해 정한다.

```sh
tools/sayaya-ui-bundle/install.sh <번들 경로|https URL>            # 검증만
tools/sayaya-ui-bundle/install.sh <번들 경로|https URL> --install  # 추적 중인 복사본 전부 교체
```

- 검증: 최상위 import/export 없음(classic script), `node --check`, 주요 태그
  (md-dialog, md-elevation, md-toast, md-navigation-rail, md-top-app-bar, md-bottom-sheet) 존재.
- `--install` 후 복사본 md5 가 모두 같은지 출력한다. 이어서 `./gradlew test` 를 기준선과 비교한다.
- 필요: node, curl(URL 일 때).
