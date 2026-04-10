package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 설명을 동반한 UI 안내 (coachmark, spotlight, arrow, badge).
 *
 * <p>사용 상황: 에이전트가 특정 UI 요소에 대해 설명이 필요한 안내를 할 때.
 * 예) 온보딩 시 "이 영역은 문서 속성을 편집하는 곳입니다."
 * 예) 작업 중 "이 필드의 값을 변경해야 합니다. 아래 미리보기를 확인하세요."
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "attention", "seq": 4, "description": "속성 편집 영역 안내",
 *   "target": ".property-panel", "style": "COACHMARK",
 *   "message": "이 영역에서 문서 속성을 편집할 수 있습니다.",
 *   "position": "bottom", "dismissable": true }
 * }</pre>
 *
 * <p>Shell 해석: style에 따라 다른 오버레이를 렌더링한다.
 * <ul>
 *   <li>COACHMARK — 반투명 배경 + 말풍선 팝업</li>
 *   <li>SPOTLIGHT — 대상 외 영역을 어둡게 처리</li>
 *   <li>PULSE — 대상 요소에 반복 펄스 효과</li>
 *   <li>ARROW — 대상을 가리키는 화살표 표시</li>
 *   <li>BADGE — 대상 모서리에 알림 뱃지 표시</li>
 * </ul>
 * position은 메시지 표시 위치(top, bottom, left, right).
 * dismissable이 true이면 사용자가 클릭/닫기로 해제 가능.
 */
public class AttentionCommand extends AgentCommand {
    @JsonProperty("target")
    private String target;
    @JsonProperty("style")
    private AttentionStyle style;
    @JsonProperty("message")
    private String message;
    @JsonProperty("position")
    private String position;
    @JsonProperty("dismissable")
    private boolean dismissable;

    public AttentionCommand() {}
    public AttentionCommand(int seq, String description, String target, AttentionStyle style, String message, String position, boolean dismissable) {
        super(seq, description);
        this.target = target;
        this.style = style;
        this.message = message;
        this.position = position;
        this.dismissable = dismissable;
    }
    public String target() { return target; }
    public AttentionStyle style() { return style; }
    public String message() { return message; }
    public String position() { return position; }
    public boolean dismissable() { return dismissable; }
}
