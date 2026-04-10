package dev.sayaya.handbook.client.domain;

/**
 * 사용자 확인 요청 값 객체.
 *
 * <p><b>책임:</b> await_confirm 커맨드의 설명(description)과 선택지(options)를 UI에 전달한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 VO)</li></ul></p>
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
