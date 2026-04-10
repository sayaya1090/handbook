#!/bin/bash
set -e

COMMAND=$(jq -r '.tool_input.command // empty')

# Check if this is a git commit
if ! echo "$COMMAND" | grep -q '^git commit'; then
  exit 0
fi

# Get list of staged files
STAGED_FILES=$(git diff --cached --name-only 2>/dev/null || echo "")

GWT_PATTERNS=('\.cache\.js$' '\.nocache\.js$' '\.devmode\.js$' 'compilation-mappings\.txt$' 'clear\.cache\.gif$')
FOUND_GWT=0

while IFS= read -r file; do
  [ -z "$file" ] && continue
  for pattern in "${GWT_PATTERNS[@]}"; do
    if echo "$file" | grep -qE "$pattern"; then
      echo "ERROR: GWT cache file staged for commit: $file" >&2
      FOUND_GWT=1
      break
    fi
  done
done <<< "$STAGED_FILES"

if [ $FOUND_GWT -eq 1 ]; then
  jq -n '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: "GWT 캐시 파일 커밋 금지. git reset HEAD <file>로 스테이징 해제 필요."
    }
  }'
  exit 2
else
  exit 0
fi
