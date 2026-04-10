#!/bin/bash
set -e

FILE_PATH=$(jq -r '.file_path // empty' 2>/dev/null || echo "$1")
[ -z "$FILE_PATH" ] && exit 0

# Skip non-source files
echo "$FILE_PATH" | grep -qE '\.(java|kt)$' || exit 0

# Skip test, i18n, docs
echo "$FILE_PATH" | grep -qE '(src/test/|/i18n/|\.md$|build/)' && exit 0

# Skip if file doesn't exist
[ -f "$FILE_PATH" ] || exit 0

# Detect Korean in string literals (quotes containing Hangul)
MATCHES=$(grep -nE '"[^"]*[가-힣][^"]*"|'\''[^'\'']*[가-힣][^'\'']*'\''' "$FILE_PATH" 2>/dev/null | grep -v '^\s*//' | grep -v 'require(' | grep -v 'KDoc\|Javadoc\|@param\|@return\|@throws' | head -3 || true)

if [ -n "$MATCHES" ]; then
  jq -n \
    --arg file "$(basename "$FILE_PATH")" \
    --arg matches "$MATCHES" \
    '{
      hookSpecificOutput: {
        hookEventName: "PostToolUse",
        additionalContext: ("WARNING: " + $file + "에 한국어 하드코딩 감지. LabelProvider 사용 필요.\n" + $matches)
      }
    }'
fi

exit 0
