package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.CheckboxElementBuilder;
import elemental2.dom.HTMLDivElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 그리드 스냅 활성화/비활성화 체크박스.
 *
 * <p><b>책임:</b> MD3 Checkbox를 통해 {@link GridSnap}의 활성화 상태를 토글한다.
 * 스냅이 활성화되면 박스 이동/리사이즈 시 그리드 단위로 정렬된다.</p>
 * <p><b>의존관계:</b> <ul>
 *   <li>{@link GridSnap} — 스냅 활성화 상태 관리</li>
 *   <li>{@link LabelProvider} — 다국어 레이블 텍스트</li>
 *   <li>{@link CheckboxElementBuilder} — MD3 Checkbox (sayaya-ui)</li>
 * </ul></p>
 */
@Singleton
public class SnapCheckbox implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;

    @Inject
    SnapCheckbox(GridSnap gridSnap, LabelProvider labelProvider) {
        var checkbox = CheckboxElementBuilder.checkbox()
                .select(gridSnap.isEnabled())
                .onChange(e -> gridSnap.setEnabled(!gridSnap.isEnabled()));

        labelProvider.subscribe(labels ->
                checkbox.ariaLabel(labels.getOrDefault("type.snap", "Snap")));

        root = div().css("type-ctrl-group").css("type-snap-checkbox").element();
        root.appendChild(checkbox.element());
    }

    @Override
    public HTMLDivElement element() { return root; }
}
