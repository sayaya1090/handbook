package dev.sayaya.handbook.client.domain;

/**
 * 사용자 확인 요청.
 * AwaitConfirmCommand를 UI에 전달하기 위한 값 객체.
 */
public class ConfirmRequest {
    private final String description;
    private final String[] options;

    public ConfirmRequest(String description, String[] options) {
        this.description = description;
        this.options = options;
    }
    public String description() { return description; }
    public String[] options() { return options; }
}
