#!/bin/sh
# sayaya-ui.js 번들 재현 빌드.
# sayaya-ui jar 에는 JS 가 없으므로, 같은 버전 태그의 labs-bundle 소스에서
# @material/web all.js 와 labs 번들을 각각 IIFE 로 빌드해 이어 붙인다(CDN 없음, classic script 로드).
# 사용법: tools/sayaya-ui-bundle/build.sh <태그 예: v2.5.0.2> [sayaya-ui 저장소 경로] [--install]
#   --install 을 주면 저장소에 추적 중인 */js/sayaya-ui.js 복사본을 모두 교체한다.
set -eu
TAG="${1:?태그를 지정하세요 (예: v2.5.0.2)}"
SRC="${2:-../sayaya-ui}"
INSTALL="${3:-}"
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
git -C "$SRC" archive "$TAG" | tar -x -C "$WORK"
LB="$WORK/src/main/webapp/labs-bundle"
cp "$HERE/handbook.config.mjs" "$LB/"
(cd "$LB" && npm install --no-audit --no-fund >/dev/null && npx rollup -c handbook.config.mjs >/dev/null)
OUT="$WORK/src/main/webapp/handbook"
COMMIT="$(git -C "$SRC" rev-parse --short "$TAG^{commit}")"
{ printf '/* sayaya-ui %s (%s %s) — @material/web all.js + labs.bundle (IIFE) */\n' "${TAG#v}" "$TAG" "$COMMIT"
  cat "$OUT/material-web.iife.js"; printf '\n'; cat "$OUT/labs.iife.js"; } > "$OUT/sayaya-ui.js"
node --check "$OUT/sayaya-ui.js"
for t in md-dialog md-elevation md-toast md-navigation-rail md-top-app-bar md-bottom-sheet; do
  grep -q "$t" "$OUT/sayaya-ui.js" || { echo "missing tag: $t" >&2; exit 1; }
done
cp "$OUT/sayaya-ui.js" "$HERE/sayaya-ui.js.out"
echo "built: $HERE/sayaya-ui.js.out ($(wc -c < "$OUT/sayaya-ui.js") bytes, md5 $(md5 -q "$OUT/sayaya-ui.js" 2>/dev/null || md5sum "$OUT/sayaya-ui.js" | cut -d' ' -f1))"
if [ "$INSTALL" = "--install" ]; then
  cd "$ROOT"
  git ls-files '*sayaya-ui.js' | while read -r f; do cp "$HERE/sayaya-ui.js.out" "$f"; echo "installed: $f"; done
fi
