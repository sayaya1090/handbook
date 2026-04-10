package dev.sayaya.handbook.client.progress;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.domain.Progress;
import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.*;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        body()
            .add(components.progressElement())
            .add(div().style("position: fixed; top: 60px; left: 0; right: 0; display: flex; justify-content: center; gap: 10px; padding: 10px; z-index: 9999;")
                .add(button("Indeterminate").id("btn-indeterminate")
                    .on(EventType.click, evt -> components.progressObserver().next(Progress.indeterminate())))
                .add(button("30%").id("btn-30")
                    .on(EventType.click, evt -> components.progressObserver().next(Progress.of(3, 10, "처리 중..."))))
                .add(button("70%").id("btn-70")
                    .on(EventType.click, evt -> components.progressObserver().next(Progress.of(7, 10, "거의 완료..."))))
                .add(button("100%").id("btn-100")
                    .on(EventType.click, evt -> components.progressObserver().next(Progress.of(10, 10, "완료!"))))
                .add(button("Hide").id("btn-hide")
                    .on(EventType.click, evt -> components.progressObserver().next(Progress.hide())))
            );
    }
}
