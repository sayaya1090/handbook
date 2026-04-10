package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 특정 요소를 강조 (pulse 애니메이션).
 *
 * <p>사용 상황: 에이전트가 사용자에게 특정 UI 요소의 위치를 알려줄 때.
 * 예) "여기 '저장' 버튼을 클릭하면 변경사항이 적용됩니다."
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "highlight", "seq": 3, "description": "'저장' 버튼 강조",
 *   "target": "#save-button" }
 * }</pre>
 *
 * <p>Shell 해석: target CSS 선택자로 DOM 요소를 찾아 pulse 애니메이션 클래스를 추가한다.
 * 일정 시간(예: 3초) 후 또는 다음 커맨드 수신 시 애니메이션을 제거한다.
 */
public class HighlightCommand extends AgentCommand {
    @JsonProperty("target")
    private String target;

    public HighlightCommand() {}
    public HighlightCommand(int seq, String description, String target) {
        super(seq, description);
        this.target = target;
    }
    public String target() { return target; }
}
