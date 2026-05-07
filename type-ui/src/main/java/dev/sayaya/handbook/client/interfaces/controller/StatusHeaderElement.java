package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.usecase.LayoutList;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.core.JsDate;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.*;

/**
 * 타입 편집기 상단바 및 모바일 플로팅 컨트롤.
 * 
 * <p><b>책임:</b> 데스크톱에서는 가로 바 형태를, 모바일에서는 Speed Dial 및 플로팅 캡슐 형태의
 * UI를 렌더링하여 글로벌 액션과 설정을 제공한다.</p>
 */
@Singleton
public class StatusHeaderElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root = div().element();
    private final HTMLContainerBuilder<HTMLDivElement> desktopBar = div().css("type-status-header");
    private final HTMLElement periodLabel = span().css("type-period-label").element();
    
    // 모바일 전용 요소들
    private final HTMLElement mobileCapsule = div().css("type-floating-pill").element();
    private final SpeedDialElement actionDial = new SpeedDialElement("fa-bolt", "action-dial");
    private final SpeedDialElement settingsDial = new SpeedDialElement("fa-gear", "settings-dial").css("settings");

    // 그룹 컨테이너들 (데스크톱 용)
    private final HTMLElement historyGroup = div().css("type-ctrl-group").element();
    private final HTMLElement persistenceGroup = div().css("type-ctrl-group").element();
    private final HTMLElement navGroup = div().css("type-ctrl-group").element();

    private final ModeToggleButton modeToggle;
    private final BeforeButton beforeBtn;
    private final AfterButton afterBtn;
    private final UndoButton undoBtn;
    private final RedoButton redoBtn;
    private final SaveButton saveBtn;
    private final ReloadButton reloadBtn;
    private final SnapButton snapButton;

    @Inject
    StatusHeaderElement(ModeToggleButton modeToggle,
                        BeforeButton beforeBtn, AfterButton afterBtn,
                        UndoButton undoBtn, RedoButton redoBtn,
                        SaveButton saveBtn, ReloadButton reloadBtn,
                        SnapButton snapButton,
                        LayoutProvider layoutProvider,
                        ViewportObserver viewport) {
        this.modeToggle = modeToggle;
        this.beforeBtn = beforeBtn;
        this.afterBtn = afterBtn;
        this.undoBtn = undoBtn;
        this.redoBtn = redoBtn;
        this.saveBtn = saveBtn;
        this.reloadBtn = reloadBtn;
        this.snapButton = snapButton;

        desktopBar.add(modeToggle)
             .add(navGroup)
             .add(historyGroup)
             .add(persistenceGroup)
             .add(snapButton);
        
        root.appendChild(desktopBar.element());
        root.appendChild(mobileCapsule);
        root.appendChild(actionDial.element());
        root.appendChild(settingsDial.element());
        
        layoutProvider.subscribe(this::updatePeriod);
        viewport.isMobile().subscribe(this::updateLayout);
    }

    private void updateLayout(boolean isMobile) {
        if (isMobile) {
            desktopBar.element().style.display = "none";
            mobileCapsule.style.display = "flex";
            actionDial.element().style.display = "flex";
            settingsDial.element().style.display = "flex";

            mobileCapsule.appendChild(beforeBtn.element());
            mobileCapsule.appendChild(periodLabel);
            mobileCapsule.appendChild(afterBtn.element());
            
            actionDial.clearItems();
            actionDial.addItem(undoBtn).addItem(redoBtn).addItem(saveBtn).addItem(reloadBtn);
            
            settingsDial.clearItems();
            settingsDial.addItem(modeToggle).addItem(snapButton);
        } else {
            desktopBar.element().style.display = "flex";
            mobileCapsule.style.display = "none";
            actionDial.element().style.display = "none";
            settingsDial.element().style.display = "none";

            desktopBar.element().insertBefore(modeToggle.element(), navGroup);
            navGroup.appendChild(beforeBtn.element());
            navGroup.appendChild(periodLabel);
            navGroup.appendChild(afterBtn.element());
            
            historyGroup.appendChild(undoBtn.element());
            historyGroup.appendChild(redoBtn.element());
            
            persistenceGroup.appendChild(saveBtn.element());
            persistenceGroup.appendChild(reloadBtn.element());
            
            desktopBar.element().appendChild(snapButton.element());
        }
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
    public HTMLDivElement element() { return root; }
}
