package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.usecase.GridSnap;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.IconButtonElementBuilder;
import dev.sayaya.ui.elements.IconElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 그리드 스냅 활성화/비활성화 버튼.
 *
 * <p><b>책임:</b> MD3 Outlined IconButton을 통해 {@link GridSnap}의 활성화 상태를 토글한다.
 * 스냅이 활성화되면 박스 이동/리사이즈 시 그리드 단위로 정렬된다.</p>
 */
@Singleton
public class SnapButton implements IsElement<HTMLElement> {
    private final HTMLElement root;

    @Inject
    SnapButton(GridSnap gridSnap, LabelProvider labelProvider) {
        var button = new IconButtonElementBuilder.OutlinedIconButtonElementBuilder()
                .icon(IconElementBuilder.icon().css("fa-sharp", "fa-light", "fa-magnet"))
                .css("type-ctrl-btn", "type-snap-button")
                .toggle(true)
                .on(EventType.click, e -> gridSnap.setEnabled(!gridSnap.isEnabled()));

        gridSnap.enabled().subscribe(enabled -> button.element().toggleAttribute("selected", enabled));

        labelProvider.subscribe(labels ->
                button.element().title = labels.getOrDefault("type.snap", "Snap to Grid"));

        root = div().css("type-ctrl-group").add(button).element();
    }

    @Override
    public HTMLElement element() { return root; }
}
