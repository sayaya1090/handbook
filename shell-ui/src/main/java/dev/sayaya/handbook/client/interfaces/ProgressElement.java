package dev.sayaya.handbook.client.interfaces;

import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.elements.ProgressElementBuilder;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLDivElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 공용 프로그레스 바. API 로딩과 에이전트 진행률 모두 지원한다.
 * <ul>
 *   <li>indeterminate — API 호출 시 무한 로딩</li>
 *   <li>value/max — 에이전트 일괄 작업 진행률</li>
 *   <li>description — 진행 중인 작업 설명 (에이전트용)</li>
 * </ul>
 */
@Singleton
public class ProgressElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    @Delegate
    private final ProgressElementBuilder.LinearProgressElementBuilder bar = ProgressElementBuilder.progress().linear();
    private final HTMLDivElement label;

    @Inject
    ProgressElement(Observable<Progress> progress) {
        label = div().css("progress-label").element();
        root = div().css("progress-container")
                .add(bar)
                .add(label)
                .element();
        root.style.set("transition", "opacity 0.3s ease");
        root.style.opacity = CSSProperties.OpacityUnionType.of("0");
        progress.subscribe(this::update);
    }

    private void update(Progress value) {
        if (value == null || !value.enabled()) {
            root.style.opacity = CSSProperties.OpacityUnionType.of("0");
            label.style.set("display", "none");
        } else {
            root.style.opacity = CSSProperties.OpacityUnionType.of("1");
            bar.indeterminate(value.intermediate());
            if (!value.intermediate()) {
                bar.max(value.max());
                bar.value(value.value());
            }
            if (value.description() != null && !value.description().isEmpty()) {
                label.textContent = value.description();
                label.style.set("display", "block");
            } else {
                label.style.set("display", "none");
            }
        }
    }

    @Override
    public HTMLDivElement element() { return root; }
}
