package dev.sayaya.handbook.client.domain;

/**
 * 진행률 정보 값 객체.
 *
 * <p><b>책임:</b> progress 커맨드의 설명, 현재 값, 최대 값을 UI에 전달하고, 백분율 및 완료 여부를 계산한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 VO)</li></ul></p>
 */
public class ProgressInfo {
    private final String description;
    private final double value;
    private final double max;

    public ProgressInfo(String description, double value, double max) {
        this.description = description;
        this.value = value;
        this.max = max;
    }
    public String description() { return description; }
    public double value() { return value; }
    public double max() { return max; }
    public double percentage() { return max > 0 ? (value / max) * 100 : 0; }
    public boolean isComplete() { return value >= max; }
}
