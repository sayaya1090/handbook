package dev.sayaya.handbook.domain;

import dev.sayaya.handbook.domain.AttentionStyle;

/**
 * 오버레이 렌더링 요청 값 객체.
 *
 * <p><b>책임:</b> attention 커맨드의 대상(target), 스타일, 메시지, 위치, 해제 가능 여부를 UI에 전달한다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link dev.sayaya.handbook.domain.AttentionStyle} — 오버레이 스타일 열거형</li></ul></p>
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
