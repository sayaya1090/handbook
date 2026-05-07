package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.interfaces.selection.SelectedBoxElement;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.handbook.usecase.ViewportObserver;
import elemental2.core.JsDate;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

@Singleton
public class StatusHeaderElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root = div().css("type-status-header").element();
    private final HTMLElement periodLabel = span().css("type-period-label").element();
    
    private final TypePropertyBar propertyBar;
    private final HTMLElement mobileInfoCapsule = div().css("type-floating-pill", "type-info").element();

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
        while (root.firstChild != null) root.removeChild(root.firstChild);
        
        if (isMobile) {
            root.style.display = "block";
            root.appendChild(mobileInfoCapsule);
            mobileInfoCapsule.appendChild(propertyBar.element());
        } else {
            root.style.display = "flex";
            root.appendChild(modeToggle.element());
            
            navGroup.appendChild(beforeBtn.element());
            navGroup.appendChild(periodLabel);
            navGroup.appendChild(afterBtn.element());
            root.appendChild(navGroup);
            
            root.appendChild(propertyBar.element());
            
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
        if (timestamp >= 253402214400000.0) return "∞";
        if (timestamp <= 0) return "-∞";
        JsDate date = new JsDate(timestamp);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate());
    }

    private String pad(int n) { return n < 10 ? "0" + n : "" + n; }

    @Override
    public HTMLDivElement element() { return root; }
}
