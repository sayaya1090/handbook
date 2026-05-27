#!/bin/bash
# UserPromptSubmit hook — 서브에이전트 라우팅 체크포인트 리마인더
#
# GEMINI.md "작업 착수 전 체크포인트" 를 Gemini 가 잊지 않도록
# 사용자 프롬프트에 규모있는 작업 키워드가 있으면 stdout 으로 리마인더를 뿜는다.
# Gemini Code 가 stdout 을 추가 컨텍스트로 대화에 주입한다.
#
# 2026-04-17 search-workspace 작업에서 Gemini 본인이 §3/§9/§10 규칙을
# 전부 무시한 사례 이후 도입.
set -euo pipefail

INPUT=$(cat)

# jq 가 있으면 prompt 필드 추출, 없으면 전체 JSON 을 grep
if command -v jq >/dev/null 2>&1; then
  PROMPT=$(echo "$INPUT" | jq -r '.prompt // empty' 2>/dev/null || echo "")
else
  PROMPT="$INPUT"
fi

# 빈 프롬프트면 조용히 종료
[ -z "$PROMPT" ] && exit 0

# 규모있는 작업 키워드 (한글 + 영문) — 감지되면 체크포인트 리마인더 삽입
PATTERN='구현|신설|신규|추가|배포|리팩|만들어|고쳐|변경|업데이트|수정|계약|매트릭스|모듈|클러스터|helm|jib|deploy|implement|refactor|contract|module'

if echo "$PROMPT" | grep -qiE "$PATTERN"; then
  cat <<'EOF'
[route-checkpoint] 이 요청은 도메인·계약·배포 범위 작업일 가능성이 있다.
`GEMINI.md` "작업 착수 전 체크포인트" 5개 평가 후 서브에이전트 위임 여부를
결정한다. 스킵한다면 응답 앞부분에 이유를 한 줄 명시.

- 도메인 수정         → `<domain>-expert`
- 계약 touch          → 매트릭스 OWNER·O 전원 **병렬**
- charts/ or 신규 백엔드 → `cluster-ops` **필수 1회 이상**
- 광역 탐색 (≥3파일)   → 내장 `Explore`
- 패턴 이식           → 원 패턴 소유 에이전트에 "최소 의존성·설정 템플릿" 선제 질의

커밋 직전 자기 감사: 서브에이전트 호출 0회면 왜 0회였는지 정당화 가능한가?
EOF
fi

exit 0
