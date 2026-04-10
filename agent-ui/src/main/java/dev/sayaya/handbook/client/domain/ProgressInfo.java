package dev.sayaya.handbook.client.domain;

/**
 * 진행률 정보.
 * ProgressCommand를 UI에 전달하기 위한 값 객체.
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
