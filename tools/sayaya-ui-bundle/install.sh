#!/bin/sh
# sayaya-ui.js 번들 검증·설치.
# 번들은 sayaya-ui 가 제공하는 공개 산출물(파일 경로 또는 URL)을 그대로 쓴다.
# handbook 은 sayaya-ui 저장소를 직접 읽거나 빌드하지 않는다.
# 사용법: tools/sayaya-ui-bundle/install.sh <번들 파일 경로 | https URL> [--install]
#   --install 이 없으면 검증만 한다. 있으면 저장소에 추적 중인 */js/sayaya-ui.js 를 모두 교체한다.
set -eu
SRC="${1:?번들 파일 경로 또는 URL 을 지정하세요}"
INSTALL="${2:-}"
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
F="$TMP/sayaya-ui.js"
case "$SRC" in
  https://*) curl -fsSL "$SRC" -o "$F" ;;
  *) cp "$SRC" "$F" ;;
esac
# classic script 로 로드하므로 최상위 import/export 가 없어야 한다
if grep -qE '^(import|export) ' "$F"; then echo 'ES module 형식입니다 — classic script(IIFE) 번들이 필요합니다' >&2; exit 1; fi
node --check "$F"
for t in md-dialog md-elevation md-toast md-navigation-rail md-top-app-bar md-bottom-sheet; do
  grep -q "$t" "$F" || { echo "필수 태그 없음: $t" >&2; exit 1; }
done
sum() { md5 -q "$1" 2>/dev/null || md5sum "$1" | cut -d' ' -f1; }
echo "ok: $(wc -c < "$F" | tr -d ' ') bytes, md5 $(sum "$F"), header: $(head -1 "$F" | cut -c1-120)"
if [ "$INSTALL" = "--install" ]; then
  cd "$ROOT"
  git ls-files '*sayaya-ui.js' | while read -r p; do cp "$F" "$p"; done
  echo "installed: $(git ls-files '*sayaya-ui.js' | wc -l | tr -d ' ') copies"
  git ls-files '*sayaya-ui.js' | while read -r p; do sum "$p"; done | sort | uniq -c
fi
