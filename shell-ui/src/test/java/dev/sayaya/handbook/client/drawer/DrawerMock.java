package dev.sayaya.handbook.client.drawer;

import dagger.Provides;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.domain.*;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@dagger.Module
public class DrawerMock {
    public static final Menu[] menu = new Menu[] {
        Menu.builder()
            .title("Menu 1")
            .supportingText("Supporting text 1")
            .order("B")
            .icon("fa-circle")
            .iconType("sharp")
            .script("js/scene1.js")
            .url("/menu1-tool1")
            .urls("^/menu1-tool1")
            .tool(Tool.builder().title("menu1-tool1").order("BA").icon("fa-circle").iconType("sharp").build())
            .build(),
        Menu.builder()
            .title("Menu 2")
            .supportingText("Supporting text 2")
            .order("C")
            .icon("fa-pen")
            .iconType("sharp")
            .script("js/scene2.js")
            .url("/menu2-tool1")
            .urls("^/menu2-tool1", "^/menu2-tool2")
            .tool(Tool.builder().title("menu2-tool1").order("AA").icon("fa-pen").iconType("sharp").build())
            .tool(Tool.builder().title("menu2-tool2").order("AB").icon("fa-user").iconType("sharp").build())
            .build(),
        Menu.builder()
            .title("Menu 3")
            .order("1")
            .icon("fa-left-from-bracket")
            .iconType("sharp")
            .bottom(true)
            .url("/menu3-tool1")
            .urls("^/menu3-tool1", "^/menu3-tool2")
            .tool(Tool.builder().title("menu3-tool1").order("1A").icon("fa-user").iconType("sharp").build())
            .tool(Tool.builder().title("menu3-tool2").order("1B").icon("fa-user").iconType("sharp").build())
            .build(),
        Menu.builder()
            .title("Menu 4")
            .order("0")
            .icon("fa-right-to-bracket")
            .iconType("sharp")
            .url("/menu4-tool1")
            .urls("^/menu4-tool1")
            .bottom(true)
            .tool(Tool.builder().title("menu4-tool1").order("0A").icon("fa-user").iconType("sharp").build())
            .build(),
        // appBarSlot="trailing" — AppBar 로 승격되는 세션 액션 (login 의 Sign In/Out 을 mock).
        // MenuRail / MobileTabs 렌더에서는 제외되고 ShellAppBarElement trailing 에 아이콘 버튼으로 노출.
        Menu.builder()
            .title("Menu 5")
            .order("Z")
            .icon("fa-right-to-bracket")
            .iconType("solid")
            .script("js/login/login.nocache.js")
            .bottom(true)
            .appBarSlot("trailing")
            .build(),
        Menu.builder()
            .title("Dynamic Menu")
            .order("D")
            .icon("fa-cubes")
            .url("/workspace/{workspaceId}/type")
            .urls("^/workspace/\\{workspaceId\\}/type$")
            .build()
    };
    private static final User user = new User();
    @Provides @Singleton MenuRepository provideMenuRepository() {
        return () -> behavior(List.of(menu));
    }
    @Provides @Singleton UserRepository provideUserRepository() {
        return () -> behavior(user);
    }
    @Provides @Singleton WorkspaceRepository provideWorkspaceRepository() {
        var ws1 = Js.<Workspace>cast(JsPropertyMap.of());
        Js.asPropertyMap(ws1).set("id", "ws-1");
        Js.asPropertyMap(ws1).set("name", "Workspace 1");
        var ws2 = Js.<Workspace>cast(JsPropertyMap.of());
        Js.asPropertyMap(ws2).set("id", "ws-2");
        Js.asPropertyMap(ws2).set("name", "Workspace 2");
        return () -> behavior(List.of(ws1, ws2));
    }
    @Provides @Singleton BehaviorSubject<String> provideUri() {
        return behavior(null);
    }
    @Provides @Singleton Observable<String> provideUriObservable(BehaviorSubject<String> uri) {
        return uri.asObservable();
    }
    @Provides @Singleton ViewportObserver provideViewport() {
        return new ViewportObserver();
    }
    @Provides @Singleton LanguageDetector provideLanguageDetector() {
        return () -> "en";
    }
    @Provides @Singleton LanguagePackRepository provideLanguagePackRepository() {
        return lang -> BehaviorSubject.behavior(Labels.empty());
    }
    @Provides @Singleton BehaviorSubject<Progress> provideProgress() {
        return BehaviorSubject.behavior(Progress.hide());
    }
    @Provides @Singleton Observer<Progress> provideProgressObserver(BehaviorSubject<Progress> p) {
        return p;
    }
    @Provides @Singleton Observable<Progress> provideProgressObservable(BehaviorSubject<Progress> p) {
        return p.asObservable();
    }
}
