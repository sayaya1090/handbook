package dev.sayaya.handbook.client.components;

import dev.sayaya.handbook.domain.ToastLevel;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.div;

/**
 * 범용 토스트 메시지 컨테이너 컴포넌트.
 *
 * <p><b>책임:</b> ToastLevel별 토스트 메시지를 렌더링한다. INFO/SUCCESS는 3초 후 페이드아웃, WARNING/ERROR는 수동 닫기 버튼으로 닫는다.</p>
 * <p><b>의존관계:</b> <ul><li>{@link ToastLevel} — 토스트 심각도 열거형</li></ul></p>
 */
public class ToastContainer implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    public ToastContainer() {
        root = div().css("ui-toast-container").element();
    }

    /** 토스트를 표시한다. */
    public void show(ToastLevel level, String message) {
        HTMLDivElement toast = div().css("ui-toast", "ui-toast-" + level.name().toLowerCase()).element();
        toast.textContent = message;

        HTMLDivElement closeBtn = div().css("ui-toast-close")
                .on(EventType.click, e -> toast.remove())
                .element();
        closeBtn.textContent = "\u00D7";
        toast.appendChild(closeBtn);

        root.appendChild(toast);

        if (level == ToastLevel.INFO || level == ToastLevel.SUCCESS) {
            DomGlobal.setTimeout(e -> {
                toast.classList.add("ui-toast-fadeout");
                DomGlobal.setTimeout(e2 -> toast.remove(), 300);
            }, 3000);
        }
    }

    /** 특정 시간 후 자동 닫히는 토스트를 표시한다. */
    public void show(ToastLevel level, String message, int autoCloseMs) {
        HTMLDivElement toast = div().css("ui-toast", "ui-toast-" + level.name().toLowerCase()).element();
        toast.textContent = message;

        HTMLDivElement closeBtn = div().css("ui-toast-close")
                .on(EventType.click, e -> toast.remove())
                .element();
        closeBtn.textContent = "\u00D7";
        toast.appendChild(closeBtn);

        root.appendChild(toast);

        DomGlobal.setTimeout(e -> {
            toast.classList.add("ui-toast-fadeout");
            DomGlobal.setTimeout(e2 -> toast.remove(), 300);
        }, autoCloseMs);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
