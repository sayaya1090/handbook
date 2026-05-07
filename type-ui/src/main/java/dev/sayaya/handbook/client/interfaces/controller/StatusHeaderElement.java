package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.interfaces.api.LayoutApi;
import dev.sayaya.handbook.client.interfaces.api.TypeApi;
import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
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
    private final HTMLDivElement root = div().css("type-status-header").element();
    private final HTMLElement periodLabel = span().css("type-period-label").element();
    
    // 모바일 전용 요소들 (Body에 직접 붙거나 fixed로 작동하도록 CSS에서 처리)
    private final HTMLElement mobileCapsule = div().css("type-floating-pill").element();
    private final SpeedDialElement actionDial = new SpeedDialElement("fa-bolt", "action-dial");
    private final SpeedDialElement settingsDial = new SpeedDialElement("fa-gear", "settings-dial").css("settings");

    // 타입 속성 바
    private final TypePropertyBar propertyBar;
    private final HTMLElement mobileInfoCapsule = div().css("type-floating-pill", "type-info").element();

    // 그룹 컨테이너들
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
    private boolean mobile = false;

    private final HTMLElement centerArea = div().css("type-header-center").element();

    @Inject
    StatusHeaderElement(ModeToggleButton modeToggle,
                        BeforeButton beforeBtn, AfterButton afterBtn,
                        UndoButton undoBtn, RedoButton redoBtn,
                        SaveButton saveBtn, ReloadButton reloadBtn,
                        SnapButton snapButton,
                        LayoutProvider layoutProvider,
                        ViewportObserver viewport,
                        TypePropertyBar propertyBar,
                        SelectedBoxElement selection) {
        this.modeToggle = modeToggle;
        this.beforeBtn = beforeBtn;
        this.afterBtn = afterBtn;
        this.undoBtn = undoBtn;
        this.redoBtn = redoBtn;
        this.saveBtn = saveBtn;
        this.reloadBtn = reloadBtn;
        this.snapButton = snapButton;
        this.propertyBar = propertyBar;

        propertyBar.element().classList.add("type-fade-item");
        navGroup.classList.add("type-fade-item");
        mobileInfoCapsule.classList.add("type-fade-item");

        layoutProvider.subscribe(this::updatePeriod);
        viewport.isMobile().subscribe(isMobile -> {
            this.mobile = isMobile;
            this.updateLayout(isMobile);
            updateMobileInfoVisibility(selection.getValue().size() == 1);
        });
        
        selection.subscribe(selected -> {
            boolean hasOne = selected.size() == 1;
            if (hasOne) {
                navGroup.classList.remove("type-fade-in");
                navGroup.classList.add("type-fade-out");
                propertyBar.element().classList.remove("type-fade-out");
                propertyBar.element().classList.add("type-fade-in");
            } else {
                navGroup.classList.remove("type-fade-out");
                navGroup.classList.add("type-fade-in");
                propertyBar.element().classList.remove("type-fade-in");
                propertyBar.element().classList.add("type-fade-out");
            }
            updateMobileInfoVisibility(hasOne);
        });
    }

    private void updateMobileInfoVisibility(boolean hasOne) {
        if (hasOne && mobile) {
            mobileInfoCapsule.classList.remove("type-fade-out");
            mobileInfoCapsule.classList.add("type-fade-in");
            mobileInfoCapsule.style.display = "flex";
        } else {
            mobileInfoCapsule.classList.remove("type-fade-in");
            mobileInfoCapsule.classList.add("type-fade-out");
            elemental2.dom.DomGlobal.setTimeout(e -> {
                if (!mobileInfoCapsule.classList.contains("type-fade-in")) {
                    mobileInfoCapsule.style.display = "none";
                }
            }, 300);
        }
    }

    private void updateLayout(boolean isMobile) {
        // 기존 자식들 제거
        while (root.firstChild != null) root.removeChild(root.firstChild);
        
        if (isMobile) {
            root.style.display = "block"; 
            
            actionDial.element().style.display = ""; 
            settingsDial.element().style.display = "";
            mobileCapsule.style.display = "flex";

            // 모바일 캡슐 내부로 요소 이동. absolute(type-fade-item) 클래스는 제거해야 레이아웃이 안 깨짐
            propertyBar.element().classList.remove("type-fade-item");
            navGroup.classList.remove("type-fade-item");

            root.appendChild(mobileCapsule);
            root.appendChild(mobileInfoCapsule);
            root.appendChild(actionDial.element());
            root.appendChild(settingsDial.element());

            mobileCapsule.appendChild(beforeBtn.element());
            mobileCapsule.appendChild(periodLabel);
            mobileCapsule.appendChild(afterBtn.element());
            
            mobileInfoCapsule.appendChild(propertyBar.element());
            
            actionDial.clearItems();
            actionDial.addItem(undoBtn).addItem(redoBtn).addItem(saveBtn).addItem(reloadBtn);
            settingsDial.clearItems();
            settingsDial.addItem(modeToggle).addItem(snapButton);
        } else {
            root.style.display = "flex";
            
            actionDial.element().style.display = "none";
            settingsDial.element().style.display = "none";
            mobileCapsule.style.display = "none";
            mobileInfoCapsule.style.display = "none";

            // 데스크톱 크로스페이드를 위해 absolute(type-fade-item) 다시 추가
            propertyBar.element().classList.add("type-fade-item");
            navGroup.classList.add("type-fade-item");

            root.appendChild(modeToggle.element());
            
            // 중앙 영역에 navGroup, propertyBar를 포개어 삽입
            while (centerArea.firstChild != null) centerArea.removeChild(centerArea.firstChild);
            navGroup.appendChild(beforeBtn.element());
            navGroup.appendChild(periodLabel);
            navGroup.appendChild(afterBtn.element());
            centerArea.appendChild(navGroup);
            centerArea.appendChild(propertyBar.element());
            root.appendChild(centerArea);
            
            historyGroup.appendChild(undoBtn.element());
            historyGroup.appendChild(redoBtn.element());
            root.appendChild(historyGroup);
            
            persistenceGroup.appendChild(saveBtn.element());
            persistenceGroup.appendChild(reloadBtn.element());
            root.appendChild(persistenceGroup);
            
            root.appendChild(snapButton.element());
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
