package dev.sayaya.handbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 일괄 작업 진행률 표시.
 *
 * <p>사용 상황: 에이전트가 여러 건의 데이터를 처리하는 장시간 작업의 진행 상황을 알릴 때.
 * 예) "50건의 우편번호 보정 중... (12/50)"
 * 예) "문서 일괄 내보내기 진행 중... (80%)"
 *
 * <p>JSON 예시:
 * <pre>{@code
 * { "type": "progress", "seq": 9, "description": "우편번호 보정 진행",
 *   "value": 12, "max": 50 }
 * }</pre>
 *
 * <p>Shell 해석: 프로그레스 바를 표시하거나 갱신한다.
 * value/max로 백분율을 계산하여 UI에 반영.
 * value == max이면 프로그레스 바를 완료 상태로 전환 후 일정 시간 뒤 제거한다.
 */
public class ProgressCommand extends AgentCommand {
    @JsonProperty("value")
    private double value;
    @JsonProperty("max")
    private double max;

    public ProgressCommand() {}
    public ProgressCommand(int seq, String description, double value, double max) {
        super(seq, description);
        this.value = value;
        this.max = max;
    }
    public double value() { return value; }
    public double max() { return max; }
}
