package dev.sayaya.handbook.client.drawer;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.usecase.WindowProgressBridge;
import dev.sayaya.handbook.usecase.WindowUriBridge;
import jsinterop.base.Js;
import org.jboss.elemento.EventType;

import static org.jboss.elemento.Elements.*;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        components.historyManager().initialize();
        components.urlBasedToolResolver().initialize();
        components.toolBasedMenuResolver().initialize();
        components.script().initialize();
        // Composition Root — 본 테스트 셸도 프로덕션 ShellInitializer 와 동일 순서로 조립.
        // Presenter 참조만으로 구독 트리거 — Dagger 가 @Singleton 인스턴스를 생성하며 생성자 내부에서 구독 시작.
        components.mobileTabsPresenter();
        body().add(components.shellAppBar())
            .add(components.mobileTabs())
            .add(components.drawer())
            .add(div().id("test-controls").style("position: fixed; top: 0; right: 0; z-index: 9999;")
                .add(button("URL 1").id("url1")
                    .on(EventType.click, evt -> components.uri().next("menu1-tool1")))
                .add(button("URL 2").id("url2")
                    .on(EventType.click, evt -> components.uri().next("menu3-tool1")))
                .add(button("URL 3").id("url3")
                    .on(EventType.click, evt -> components.uri().next("menu3-tool2")))
            );
        WindowUriBridge.register(url -> components.uri().next(url));
        WindowProgressBridge.register(value -> {
            dev.sayaya.handbook.domain.Progress p = Js.cast(value);
            components.progressObserver().next(p);
        });
    }
}
