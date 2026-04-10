package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 변경 전후 diff를 인라인으로 표시.
 *
 * <p>사용 상황: 에이전트가 실제 변경을 적용하기 전에 사용자에게 미리보기를 보여줄 때.
 * 예) "고객 타입의 '이름' 필드를 '고객명'으로 변경하겠습니다. 미리보기를 확인하세요."
 * 예) 여러 행의 데이터를 일괄 수정하기 전 diff 표시.
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "preview", "seq": 5, "description": "필드명 변경 미리보기",
 *   "changes": [
 *     "field:name:label: 이름 → 고객명",
 *     "field:name:description: 고객 이름 → 고객의 정식 명칭"
 *   ] }
 * }</pre>
 *
 * <p>Shell 해석: changes 배열을 파싱하여 인라인 diff UI를 렌더링한다.
 * 변경 전 값은 취소선+빨간색, 변경 후 값은 녹색으로 표시.
 * 보통 await_confirm 커맨드가 뒤따라 사용자 확인을 대기한다.
 */
public class PreviewCommand extends AgentCommand {
    @JsonProperty("changes")
    private String[] changes;

    public PreviewCommand() {}
    public PreviewCommand(int seq, String description, String[] changes) {
        super(seq, description);
        this.changes = changes;
    }
    public String[] changes() { return changes; }
}
