package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 실제 값 변경 (필드 입력, 행 추가/삭제).
 *
 * <p>사용 상황: 에이전트가 사용자 대신 실제 데이터를 변경할 때.
 * 예) "고객 타입에 '전화번호' 필드를 추가합니다."
 * 예) "문서 #123의 '상태' 필드를 '완료'로 변경합니다."
 * 예) 일괄 데이터 보정: "누락된 우편번호 50건을 자동으로 채웁니다."
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "mutate", "seq": 7, "description": "고객 타입에 전화번호 필드 추가",
 *   "changes": [
 *     "ADD field:phone_number:type=STRING",
 *     "SET field:phone_number:label=전화번호",
 *     "SET field:phone_number:required=false"
 *   ] }
 * }</pre>
 *
 * <p>Shell 해석: changes 배열의 각 항목을 해석하여 Gateway API를 호출한다.
 * 변경이 적용되면 해당 UI 요소를 갱신(리렌더링)한다.
 * 실패 시 notify(level=error) 커맨드로 사용자에게 알린다.
 */
public class MutateCommand extends AgentCommand {
    @JsonProperty("changes")
    private String[] changes;

    public MutateCommand() {}
    public MutateCommand(int seq, String description, String[] changes) {
        super(seq, description);
        this.changes = changes;
    }
    public String[] changes() { return changes; }
}
