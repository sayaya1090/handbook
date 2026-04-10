package dev.sayaya.handbook.client.domain;

import dev.sayaya.handbook.domain.AttentionStyle;

/**
 * 오버레이 렌더링 요청.
 * AttentionCommand를 UI에 전달하기 위한 값 객체.
 */
public class OverlayRequest {
    private final String target;
    private final AttentionStyle style;
    private final String message;
    private final String position;
    private final boolean dismissable;

    public OverlayRequest(String target, AttentionStyle style, String message, String position, boolean dismissable) {
        this.target = target;
        this.style = style;
        this.message = message;
        this.position = position;
        this.dismissable = dismissable;
    }
    public String target() { return target; }
    public AttentionStyle style() { return style; }
    public String message() { return message; }
    public String position() { return position; }
    public boolean dismissable() { return dismissable; }
}
