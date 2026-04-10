package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 토스트/배너 메시지 표시.
 *
 * <p>사용 상황: 에이전트가 사용자에게 상태 정보, 경고, 오류를 알릴 때.
 * 예) 작업 시작: "문서 일괄 업데이트를 시작합니다."
 * 예) 경고: "3건의 문서에 권한이 부족하여 건너뜁니다."
 * 예) 오류: "Gateway 연결에 실패했습니다. 잠시 후 다시 시도해주세요."
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "notify", "seq": 8, "description": "권한 부족 경고",
 *   "level": "warning", "message": "3건의 문서에 권한이 부족하여 건너뜁니다." }
 * }</pre>
 *
 * <p>Shell 해석: level에 따라 토스트 UI 스타일을 결정한다.
 * <ul>
 *   <li>"info" — 파란색 정보 토스트, 3초 후 자동 닫힘</li>
 *   <li>"success" — 녹색 성공 토스트, 3초 후 자동 닫힘</li>
 *   <li>"warning" — 주황색 경고 토스트, 사용자가 닫을 때까지 유지</li>
 *   <li>"error" — 빨간색 오류 배너, 사용자가 닫을 때까지 유지</li>
 * </ul>
 */
public class NotifyCommand extends AgentCommand {
    @JsonProperty("level")
    private String level;
    @JsonProperty("message")
    private String message;

    public NotifyCommand() {}
    public NotifyCommand(int seq, String description, String level, String message) {
        super(seq, description);
        this.level = level;
        this.message = message;
    }
    public String level() { return level; }
    public String message() { return message; }
}
