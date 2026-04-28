package dev.sayaya.handbook.domain;

/**
 * 알림 정보 값 객체.
 *
 * <p><b>책임:</b> notify 커맨드의 심각도(level)와 메시지를 UI에 전달한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 VO)</li></ul></p>
 */
public class NotifyInfo {
    private final String level;
    private final String message;

    public NotifyInfo(String level, String message) {
        this.level = level;
        this.message = message;
    }
    public String level() { return level; }
    public String message() { return message; }
}
