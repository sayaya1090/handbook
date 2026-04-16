package dev.sayaya.handbook.client.drawer;

import dagger.Provides;
import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.usecase.LanguageDetector;
import dev.sayaya.handbook.usecase.LanguagePackRepository;
import dev.sayaya.handbook.usecase.ViewportObserver;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

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
            .url("menu1-tool1")
            .tool(Tool.builder().title("menu1-tool1").order("BA").icon("fa-circle").iconType("sharp").build())
            .build(),
        Menu.builder()
            .title("Menu 2")
            .supportingText("Supporting text 2")
            .order("C")
            .icon("fa-pen")
            .iconType("sharp")
            .script("js/scene2.js")
            .url("menu2-tool1").url("menu2-tool2")
            .tool(Tool.builder().title("menu2-tool1").order("AA").icon("fa-pen").iconType("sharp").build())
            .tool(Tool.builder().title("menu2-tool2").order("AB").icon("fa-user").iconType("sharp").build())
            .build(),
        Menu.builder()
            .title("Menu 3")
            .order("1")
            .icon("fa-left-from-bracket")
            .iconType("sharp")
            .bottom(true)
            .url("menu3-tool1").url("menu3-tool2")
            .tool(Tool.builder().title("menu3-tool1").order("1A").icon("fa-user").iconType("sharp").build())
            .tool(Tool.builder().title("menu3-tool2").order("1B").icon("fa-user").iconType("sharp").build())
            .build(),
        Menu.builder()
            .title("Menu 4")
            .order("0")
            .icon("fa-right-to-bracket")
            .iconType("sharp")
            .url("menu4-tool1")
            .bottom(true)
            .tool(Tool.builder().title("menu4-tool1").order("0A").icon("fa-user").iconType("sharp").build())
            .build()
    };
    private static final User user = new User();
    @Provides @Singleton MenuRepository provideMenuRepository() {
        return () -> behavior(List.of(menu));
    }
    @Provides @Singleton UserRepository provideUserRepository() {
        return () -> behavior(user);
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
