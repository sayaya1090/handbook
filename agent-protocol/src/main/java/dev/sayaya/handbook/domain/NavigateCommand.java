package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 특정 화면으로 이동 (메뉴/도구 선택, URL 변경).
 *
 * <p>사용 상황: 에이전트가 사용자를 특정 메뉴나 도구 화면으로 안내할 때.
 * 예) "고객 타입 정의를 수정하려면 먼저 타입 관리 화면으로 이동합니다."
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "navigate", "seq": 1, "description": "타입 관리 화면으로 이동",
 *   "menu": "타입", "tool": "타입 관리", "url": "/workspace/ws-1/type" }
 * }</pre>
 *
 * <p>Shell 해석: menu/tool이 주어지면 MenuRail/ToolRail 선택 상태를 변경하고,
 * url이 주어지면 History.pushState로 URL을 변경하여 해당 프레임을 로드한다.
 * 필드가 null이면 해당 항목은 변경하지 않는다.
 */
public class NavigateCommand extends AgentCommand {
    @JsonProperty("menu")
    private String menu;
    @JsonProperty("tool")
    private String tool;
    @JsonProperty("url")
    private String url;

    public NavigateCommand() {}
    public NavigateCommand(int seq, String description, String menu, String tool, String url) {
        super(seq, description);
        this.menu = menu;
        this.tool = tool;
        this.url = url;
    }
    public String menu() { return menu; }
    public String tool() { return tool; }
    public String url() { return url; }
}
