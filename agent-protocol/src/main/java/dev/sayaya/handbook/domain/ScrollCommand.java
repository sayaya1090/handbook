package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 특정 위치로 스크롤/포커스 이동.
 *
 * <p>사용 상황: 에이전트가 사용자 시야를 특정 위치로 유도할 때.
 * 예) 긴 테이블에서 "변경된 행이 아래쪽에 있습니다. 스크롤하겠습니다."
 * 예) 특정 입력 필드로 포커스를 이동시킬 때.
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "scroll", "seq": 2, "description": "변경된 행으로 스크롤",
 *   "target": "tr[data-row-id='row-42']" }
 * }</pre>
 *
 * <p>Shell 해석: target CSS 선택자로 요소를 찾아 scrollIntoView({behavior: 'smooth'})를 호출한다.
 * 입력 요소라면 추가로 focus()를 호출한다.
 */
public class ScrollCommand extends AgentCommand {
    @JsonProperty("target")
    private String target;

    public ScrollCommand() {}
    public ScrollCommand(int seq, String description, String target) {
        super(seq, description);
        this.target = target;
    }
    public String target() { return target; }
}
