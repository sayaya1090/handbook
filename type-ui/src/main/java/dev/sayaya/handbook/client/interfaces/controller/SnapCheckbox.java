package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 그리드 스냅 활성화/비활성화 체크박스.
 *
 * <p><b>책임:</b> HTML checkbox를 통해 {@link GridSnap}의 활성화 상태를 토글한다.
 * 스냅이 활성화되면 박스 이동/리사이즈 시 그리드 단위로 정렬된다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link GridSnap} — 스냅 활성화 상태 관리</li>
 *   <li>{@link LabelProvider} — 다국어 레이블 텍스트</li>
 * </ul></p>
 * <p><b>주의:</b> 네이티브 HTML input[type=checkbox]를 사용한다(MD3 컴포넌트 아님).</p>
 */
@Singleton
public class SnapCheckbox implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    SnapCheckbox(GridSnap gridSnap, LabelProvider labelProvider) {
        HTMLInputElement checkbox = (HTMLInputElement) DomGlobal.document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.id = "snap-toggle";
        checkbox.checked = gridSnap.isEnabled();
        checkbox.addEventListener("change", e -> gridSnap.setEnabled(checkbox.checked));

        HTMLElement label = (HTMLElement) DomGlobal.document.createElement("label");
        label.setAttribute("for", "snap-toggle");
        label.textContent = "Snap";
        label.style.setProperty("font-size", "13px");
        label.style.setProperty("cursor", "pointer");
        label.style.setProperty("user-select", "none");

        labelProvider.subscribe(labels ->
                label.textContent = labels.getOrDefault("type.snap", "Snap"));

        root = div().css("type-ctrl-group").element();
        root.appendChild(checkbox);
        root.appendChild(label);
    }

    @Override
    public HTMLDivElement element() { return root; }
}
