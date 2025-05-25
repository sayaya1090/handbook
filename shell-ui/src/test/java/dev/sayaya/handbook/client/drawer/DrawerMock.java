package dev.sayaya.handbook.client.drawer;

import dagger.Provides;
import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.domain.User;
import dev.sayaya.handbook.client.usecase.MenuRepository;
import dev.sayaya.handbook.client.usecase.UserRepository;
import dev.sayaya.rx.Observable;

import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@dagger.Module
public class DrawerMock {
    public static final Menu[] menu = new Menu[] {
            Menu.builder()
                .title("Menu 1")
                .supportingText("Supporting text 1")
                .trailingText("Trailing text 1")
                .order("B")
                .icon("fa-circle")
                .iconType("sharp")
                .script("js/scene1.js")
                .tools(Tool.builder().title("menu1-tool1").uriRegex("menu1-tool1").order("BA").icon("fa-circle").iconType("sharp").build())
                .build(),
            Menu.builder()
                .title("Menu 2")
                .supportingText("Supporting text 2")
                .trailingText("Trailing text 2")
                .order("C")
                .icon("fa-pen")
                .iconType("sharp")
                .script("js/scene2.js")
                .tools(
                    Tool.builder().title("menu2-tool1").uriRegex("menu2-tool1").order("AA").icon("fa-pen").iconType("sharp").build(),
                    Tool.builder().title("menu2-tool2").uriRegex("menu2-tool2").order("AB").icon("fa-user").iconType("sharp").build()
                ).build(),
            Menu.builder()
                .title("Menu 3")
                .order("1")
                .icon("fa-left-from-bracket")
                .iconType("sharp")
                .bottom(true)
                .tools(
                    Tool.builder().title("menu3-tool1").uriRegex("menu3-tool1").order("1A").icon("fa-user").iconType("sharp").build(),
                    Tool.builder().title("menu3-tool2").uriRegex("menu3-tool2").order("1B").icon("fa-user").iconType("sharp").build()
                ).build(),
            Menu.builder()
                .title("Menu 4")
                .order("0")
                .icon("fa-right-to-bracket")
                .iconType("sharp")
                .bottom(true)
                .tools(Tool.builder().title("menu4-tool1").uriRegex("menu4-tool1").order("0A").icon("fa-user").iconType("sharp").build())
                .build()
    };
    private static User user = new User();
    @Provides @Singleton MenuRepository provideMenuRepository() {
        return ()-> behavior(List.of(menu));
    }
    @Provides @Singleton UserRepository provideUserRepository() {
        return ()-> behavior(user);
    }
    @Provides @Singleton Observable<Tool[]> tools() { return ClientWindow.tools.asObservable(); }
}
