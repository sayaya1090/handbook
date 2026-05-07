package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.core.JsDate;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 타입 편집기 상단바.
 * 
 * <p><b>책임:</b> 시스템 전반의 글로벌 액션(Undo, Save 등)과 
 * 뷰 설정(스냅, 기간 이동)을 한데 모아 제공한다.</p>
 */
@Singleton
public class StatusHeaderElement implements IsElement<HTMLDivElement> {
    private final HTMLContainerBuilder<HTMLDivElement> _this = div().css("type-status-header");
    private final HTMLElement periodLabel = span().css("type-period-label").element();

    @Inject
    StatusHeaderElement(ModeToggleButton modeToggle,
                        BeforeButton beforeBtn, AfterButton afterBtn,
                        UndoButton undoBtn, RedoButton redoBtn,
                        SaveButton saveBtn, ReloadButton reloadBtn,
                        SnapButton snapButton,
                        LayoutProvider layoutProvider) {
        _this.add(modeToggle)
             .add(div().css("type-ctrl-group").add(beforeBtn).add(periodLabel).add(afterBtn))
             .add(div().css("type-ctrl-group").add(undoBtn).add(redoBtn))
             .add(div().css("type-ctrl-group").add(saveBtn).add(reloadBtn))
             .add(snapButton);
        
        layoutProvider.subscribe(this::updatePeriod);
    }

    private void updatePeriod(LayoutPeriod period) {
        if (period == null) {
            periodLabel.textContent = "";
            return;
        }
        String start = format(period.effectDateTime());
        String end = format(period.expireDateTime());
        periodLabel.textContent = start + " ~ " + end;
    }

    private String format(double timestamp) {
        if (timestamp >= 253402214400000.0) return "∞"; // 9999-12-31 근처
        if (timestamp <= 0) return "-∞";
        JsDate date = new JsDate(timestamp);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate());
    }

    private String pad(int n) { return n < 10 ? "0" + n : "" + n; }

    @Override
    public HTMLDivElement element() { return _this.element(); }
}
