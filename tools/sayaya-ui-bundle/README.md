# sayaya-ui.js 번들 빌드

`dev.sayaya:ui` jar 에는 Java builder 만 있고 커스텀 엘리먼트 JS 구현은 들어 있지 않다.
앱과 테스트 HTML 은 저장소에 포함된 `*/js/sayaya-ui.js`(classic script)를 로드하므로,
jar 버전을 올릴 때는 **같은 태그**에서 이 번들을 다시 만들어 모든 복사본을 교체해야 한다.

```sh
# sayaya-ui 저장소가 ../sayaya-ui 에 있다고 가정
tools/sayaya-ui-bundle/build.sh v2.5.0.2 ../sayaya-ui            # 빌드만 (sayaya-ui.js.out 생성)
tools/sayaya-ui-bundle/build.sh v2.5.0.2 ../sayaya-ui --install  # 추적 중인 복사본 전부 교체
```

- 필요: git, node/npm. CDN 은 쓰지 않는다.
- `handbook.config.mjs` 는 sayaya-ui 의 `labs-bundle/rollup.config.js` 를 재사용해
  ① `@material/web/all.js` 를 elevation 제거 없이 IIFE 로, ② labs `src/index.js` 를 원래 플러그인 그대로 IIFE 로 빌드한다.
  두 결과를 이 순서로 이어 붙인다(본체가 먼저 등록돼야 labs 의 중복 define 가드가 맞게 동작).
- 스크립트는 `node --check` 와 주요 태그(md-dialog, md-elevation, md-toast, md-navigation-rail, md-top-app-bar, md-bottom-sheet) 존재를 검사한다.
- 교체 후 `md5` 로 복사본이 모두 같은지 확인하고 `./gradlew test` 를 기준선과 비교한다.
