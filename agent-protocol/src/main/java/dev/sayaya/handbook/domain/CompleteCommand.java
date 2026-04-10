package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 작업 완료 (요약 표시).
 *
 * <p>사용 상황: 에이전트의 전체 작업이 완료되었을 때. 항상 커맨드 시퀀스의 마지막에 전송된다.
 * 예) "고객 타입에 '전화번호' 필드를 추가하고 라벨을 설정했습니다."
 * 예) "워크스페이스 '영업팀'을 생성하고 기본 타입 3개를 구성했습니다."
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "complete", "seq": 10, "description": "작업 완료",
 *   "summary": "고객 타입에 전화번호 필드를 추가했습니다. 총 1개 타입, 1개 필드가 변경되었습니다." }
 * }</pre>
 *
 * <p>Shell 해석: 에이전트 세션을 종료하고, summary를 성공 토스트 또는 완료 패널로 표시한다.
 * SSE 연결을 정리하고, 에이전트 UI(프로그레스 바, 오버레이 등)를 모두 제거한다.
 * 세션 상태를 IDLE로 복원한다.
 */
public class CompleteCommand extends AgentCommand {
    @JsonProperty("summary")
    private String summary;

    public CompleteCommand() {}
    public CompleteCommand(int seq, String description, String summary) {
        super(seq, description);
        this.summary = summary;
    }
    public String summary() { return summary; }
}
