package dev.sayaya.handbook.client.domain;

/**
 * 진행률 정보 값 객체.
 *
 * <p><b>책임:</b> progress 커맨드의 그룹 진행 정보(currentGroup, totalGroups, parallel, stepCount)를 UI에 전달하고,
 * 백분율 및 완료 여부를 계산한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 VO)</li></ul></p>
 */
public class ProgressInfo {
    private final String description;
    private final double value;
    private final double max;
    private final int parallel;
    private final int stepCount;

    public ProgressInfo(String description, double value, double max, int parallel, int stepCount) {
        this.description = description;
        this.value = value;
        this.max = max;
        this.parallel = parallel;
        this.stepCount = stepCount;
    }

    public String description() { return description; }
    public double value() { return value; }
    public double max() { return max; }
    /** 현재 그룹에서 병렬 실행 중인 스텝 수 (0이면 병렬 정보 없음) */
    public int parallel() { return parallel; }
    /** 현재 그룹의 총 스텝 수 */
    public int stepCount() { return stepCount; }
    public double percentage() { return max > 0 ? (value / max) * 100 : 0; }
    public boolean isComplete() { return value >= max; }
}
