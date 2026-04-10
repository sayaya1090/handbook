package dev.sayaya.handbook.client.domain;

/**
 * 알림 정보.
 * NotifyCommand를 UI에 전달하기 위한 값 객체.
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
