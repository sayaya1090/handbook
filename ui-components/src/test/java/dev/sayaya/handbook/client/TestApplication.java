package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.components.*;
import dev.sayaya.handbook.domain.OverlayStyle;
import dev.sayaya.handbook.domain.ToastLevel;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.*;

public class TestApplication implements EntryPoint {
    private final ToastContainer toast = new ToastContainer();
    private final OverlayContainer overlay = new OverlayContainer();
    private final HighlightEffect highlight = new HighlightEffect();
    private final ScrollEffect scroll = new ScrollEffect();
    private final ConfirmDialog confirm = new ConfirmDialog();
    private final DiffPanel diff = new DiffPanel();

    @Override
    public void onModuleLoad() {
        HTMLDivElement target = div().id("target")
                .style("width: 200px; height: 50px; background: #e3f2fd; border: 1px solid #90caf9; border-radius: 8px; margin: 80px 20px 20px;")
                .element();
        target.textContent = "대상 요소";

        HTMLDivElement scrollTarget = div().id("scroll-target")
                .style("margin-top: 400px; width: 200px; height: 50px; background: #fff3e0; border: 1px solid #ffcc80; border-radius: 8px;")
                .element();
        scrollTarget.textContent = "스크롤 타겟";

        body()
            .add(target)
            .add(scrollTarget)
            .add(overlay)
            .add(confirm)
            .add(diff)
            .add(toast)
            .add(div().style("position: fixed; top: 0; left: 0; right: 0; z-index: 9999; display: flex; flex-wrap: wrap; gap: 5px; padding: 8px; background: rgba(255,255,255,0.95); border-bottom: 1px solid #e0e0e0;")
                .add(button("Toast info").id("btn-toast-info")
                    .on(EventType.click, e -> toast.show(ToastLevel.INFO, "정보 메시지입니다.")))
                .add(button("Toast error").id("btn-toast-error")
                    .on(EventType.click, e -> toast.show(ToastLevel.ERROR, "오류가 발생했습니다.")))
                .add(button("Overlay coachmark").id("btn-overlay")
                    .on(EventType.click, e -> overlay.show("#target", OverlayStyle.COACHMARK, "이 영역을 확인하세요", "bottom", true)))
                .add(button("Highlight").id("btn-highlight")
                    .on(EventType.click, e -> highlight.highlight("#target")))
                .add(button("Clear highlight").id("btn-clear")
                    .on(EventType.click, e -> highlight.clear()))
                .add(button("Scroll").id("btn-scroll")
                    .on(EventType.click, e -> scroll.scrollTo("#scroll-target")))
                .add(button("Confirm").id("btn-confirm")
                    .on(EventType.click, e -> confirm.show("삭제하시겠습니까?", new String[]{"삭제", "취소"}, opt -> toast.show(ToastLevel.SUCCESS, "선택: " + opt))))
                .add(button("Diff").id("btn-diff")
                    .on(EventType.click, e -> diff.show(new String[]{"이름 → 고객명", "상태 → 활성"})))
                .add(button("Hide diff").id("btn-hide-diff")
                    .on(EventType.click, e -> diff.hide()))
            );
    }
}
