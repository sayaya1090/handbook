#!/usr/bin/env bash
# .gemini/hooks/context-loader.sh
# This hook dynamically reads all project documentation and adds it to the Gemini CLI session context.

CONTEXT="# Project Development Context\n\nThis context was automatically loaded by the '.gemini/hooks/context-loader.sh' hook.\n"

# 1. Load Essential Architectural Documents
ESSENTIAL_DOCS=(
  "docs/architecture.md"
  "docs/system-overview.md"
  "docs/engineering-standards.md"
  "docs/database-schema.md"
  "docs/design-patterns.md"
  "docs/error-handling.md"
  "docs/discrepancies.md"
)

for FILE in "${ESSENTIAL_DOCS[@]}"; do
  if [ -f "$FILE" ]; then
    CONTENT=$(cat "$FILE")
    CONTEXT="$CONTEXT\n\n## $FILE\n$CONTENT"
  fi
done

# 2. Load All Contracts
CONTEXT="$CONTEXT\n\n# Interface Contracts\n"
CONTRACT_FILES=$(find docs/contracts -name "*.md")
for FILE in $CONTRACT_FILES; do
  CONTENT=$(cat "$FILE")
  CONTEXT="$CONTEXT\n\n## $FILE\n$CONTENT"
done

# 3. Load Summaries/Indexes of Large Documents
if [ -f "docs/requirements.md" ]; then
  REQ_INDEX=$(cat "docs/requirements/README.md" 2>/dev/null || head -n 100 docs/requirements.md)
  CONTEXT="$CONTEXT\n\n## docs/requirements.md (Index & Overview)\n$REQ_INDEX\n\n... [Full content available via 'read_file docs/requirements.md'] ..."
fi

if [ -f "docs/usecases.md" ]; then
  UC_INDEX=$(cat "docs/usecases/README.md" 2>/dev/null || head -n 100 docs/usecases.md)
  CONTEXT="$CONTEXT\n\n## docs/usecases.md (Index & Overview)\n$UC_INDEX\n\n... [Full content available via 'read_file docs/usecases.md'] ..."
fi

# 4. Load Other Misc Docs
OTHER_DOCS=("docs/ingress-options.md" "docs/design.md" "docs/development.md")
for FILE in "${OTHER_DOCS[@]}"; do
  if [ -f "$FILE" ]; then
    CONTENT=$(cat "$FILE")
    CONTEXT="$CONTEXT\n\n## $FILE\n$CONTENT"
  fi
done

# Escape for JSON using python3
ESCAPED_CONTEXT=$(echo -e "$CONTEXT" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))')

# Output JSON
echo "{\"hookSpecificOutput\": {\"additionalContext\": $ESCAPED_CONTEXT}}"
