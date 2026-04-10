package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 사용자 확인 대기 (계속/취소/수정 선택지).
 *
 * <p>사용 상황: 에이전트가 위험한 작업 전에 사용자 동의를 구하거나, 다음 단계 선택을 요청할 때.
 * 예) preview 후: "위 변경사항을 적용하시겠습니까?"
 * 예) 분기 선택: "A안(필드 추가)과 B안(기존 필드 변환) 중 선택해주세요."
 * 예) 온보딩: "이 구조로 워크스페이스를 생성할까요?"
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "await_confirm", "seq": 6, "description": "변경사항 적용 확인",
 *   "options": ["적용", "수정", "취소"] }
 * }</pre>
 *
 * <p>Shell 해석: options 배열을 버튼으로 렌더링한 확인 다이얼로그를 표시한다.
 * SSE 스트림은 이 커맨드 수신 시점에 일시 정지(서버에서 응답 대기)된다.
 * 사용자가 선택하면 POST /assistant/respond로 선택값을 전송하고,
 * 서버가 다음 커맨드 스트림을 재개한다.
 */
public class AwaitConfirmCommand extends AgentCommand {
    @JsonProperty("options")
    private String[] options;

    public AwaitConfirmCommand() {}
    public AwaitConfirmCommand(int seq, String description, String[] options) {
        super(seq, description);
        this.options = options;
    }
    public String[] options() { return options; }
}
