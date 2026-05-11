package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.domain.TypeLayout;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.core.JsDate;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 편집기의 상단 상태바 컴포넌트.
 *
 * <p><b>책임:</b> 데스크톱에서는 가로 바 형태를, 모바일에서는 Speed Dial 및 플로팅 캡슐 형태의 UI를 렌더링하여 
 * 현재 선택된 레이아웃의 기간 표시, Undo/Redo/Save/Reload 등의 글로벌 액션 버튼 배치,
 * 그리고 뷰포트에 따른 동적 버튼 재배치(Reparenting)를 총괄한다.</p>
 */
@Singleton
public class StatusHeaderElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root = div().css("type-status-header").element();
    private final HTMLElement periodLabel = span().css("type-period-label").element();
    
    private final HTMLDivElement historyGroup = div().css("type-ctrl-group").element();
    private final HTMLDivElement persistenceGroup = div().css("type-ctrl-group").element();
    private final HTMLDivElement navGroup = div().css("type-ctrl-group").element();
    private final HTMLElement centerArea = div().css("type-header-center").element();

    // Reparenting 대상 버튼들 (Singleton 인스턴스 주입)
    private final ModeToggleButton modeToggle;
    private final BeforeButton beforeBtn;
    private final AfterButton afterBtn;
    private final UndoButton undoBtn;
    private final RedoButton redoBtn;
    private final SaveButton saveBtn;
    private final ReloadButton reloadBtn;
    private final SnapButton snapButton;
    private final ActionDialElement actionDial;
    private final SettingsDialElement settingsDial;

    @Inject
    StatusHeaderElement(ModeToggleButton modeToggle,
                        BeforeButton beforeBtn, AfterButton afterBtn,
                        UndoButton undoBtn, RedoButton redoBtn,
                        SaveButton saveBtn, ReloadButton reloadBtn,
                        SnapButton snapButton,
                        ActionDialElement actionDial,
                        SettingsDialElement settingsDial,
                        LayoutProvider layoutProvider,
                        SelectedBoxElement selection,
                        ViewportObserver viewportObserver) {
        this.modeToggle = modeToggle;
        this.beforeBtn = beforeBtn;
        this.afterBtn = afterBtn;
        this.undoBtn = undoBtn;
        this.redoBtn = redoBtn;
        this.saveBtn = saveBtn;
        this.reloadBtn = reloadBtn;
        this.snapButton = snapButton;
        this.actionDial = actionDial;
        this.settingsDial = settingsDial;

        initDesktopLayout();
        initMobileLayout();

        viewportObserver.isMobile().subscribe(this::updateLayout);
        selection.subscribe(keys -> {
            boolean hasSelection = keys != null && !keys.isEmpty();
            navGroup.classList.toggle("type-fade-out", hasSelection);
            navGroup.classList.toggle("type-fade-in", !hasSelection);
        });

        layoutProvider.subscribe(this::updatePeriod);
    }

    private void initDesktopLayout() {
        navGroup.appendChild(beforeBtn.element());
        navGroup.appendChild(periodLabel);
        navGroup.appendChild(afterBtn.element());
        navGroup.classList.add("type-nav-group", "type-fade-in");

        centerArea.appendChild(navGroup);

        root.appendChild(persistenceGroup);
        root.appendChild(historyGroup);
        root.appendChild(centerArea);
        root.appendChild(div().css("type-ctrl-group").add(modeToggle).add(snapButton).element());
        
        updateLayout(false);
    }

    private void initMobileLayout() {
        // updateLayout에서 처리되므로 여기선 생략
    }

    private void updateLayout(boolean isMobile) {
        root.classList.toggle("mobile", isMobile);
        root.style.display = isMobile ? "none" : "flex";
        if (isMobile) {
            actionDial.addItem(saveBtn);
            actionDial.addItem(reloadBtn);
            actionDial.addItem(undoBtn);
            actionDial.addItem(redoBtn);
            actionDial.addItem(beforeBtn);
            actionDial.addItem(afterBtn);
            
            settingsDial.addItem(modeToggle);
            settingsDial.addItem(snapButton);
        } else {
            persistenceGroup.appendChild(saveBtn.element());
            persistenceGroup.appendChild(reloadBtn.element());
            historyGroup.appendChild(undoBtn.element());
            historyGroup.appendChild(redoBtn.element());
            navGroup.insertBefore(beforeBtn.element(), periodLabel);
            navGroup.appendChild(afterBtn.element());
            
            centerArea.appendChild(navGroup);
        }
    }

    private void updatePeriod(TypeLayout layout) {
        if (layout == null) {
            periodLabel.textContent = "-";
            return;
        }
        periodLabel.textContent = format(layout.effectDateTime()) + " ~ " + format(layout.expireDateTime());
    }

    private String format(double timestamp) {
        if (timestamp >= 253402214400000.0) return "∞";
        JsDate date = new JsDate(timestamp);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate());
    }

    private String pad(int n) { return n < 10 ? "0" + n : "" + n; }

    @Override
    public HTMLDivElement element() { return root; }
}
