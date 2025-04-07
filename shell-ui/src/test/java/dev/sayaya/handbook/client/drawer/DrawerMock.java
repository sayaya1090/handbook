package dev.sayaya.handbook.client.drawer;

import dagger.Provides;
import dev.sayaya.handbook.client.domain.Tool;
import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.handbook.client.usecase.MenuRepository;

import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@dagger.Module
public class DrawerMock {
    public static final Menu[] menu;
    static {
        var menu1 = new Menu(); {
            menu1.title = "Menu 1";
            menu1.supportingText = "Supporting text 1";
            menu1.trailingText = "Trailing text 1";
            menu1.order = "C";
            menu1.icon = "fa-pen";
            menu1.iconType = "sharp";
            var tool1 = new Tool().title("menu1-tool1").uri("menu1-tool1").uriRegex("menu1-tool1").order("AA").icon("fa-pen").iconType("sharp").script("js/scene1.js");
            var tool2 = new Tool().title("menu1-tool2").uri("menu1-tool2").uriRegex("menu1-tool2").order("AB").icon("fa-user").iconType("sharp").script("js/scene1.js");
            menu1.tools = new Tool[] { tool1, tool2 };
        }
        var menu2 = new Menu(); {
            menu2.title = "Menu 2";
            menu2.supportingText = "Supporting text 2";
            menu2.trailingText = "Trailing text 2";
            menu2.order = "B";
            menu2.icon = "fa-circle";
            menu2.iconType = "sharp";
            var tool1 = new Tool().title("menu2-tool1").uri("menu2-tool1").uriRegex("menu2-tool1").order("BA").icon("fa-circle").iconType("sharp").script("js/scene2.js");;
            menu2.tools = new Tool[] { tool1 };
        }
        var menu3 = new Menu(); {
            menu3.title = "Menu 3";
            menu3.order = "1";
            menu3.icon = "fa-left-from-bracket";
            menu3.iconType = "sharp";
            menu3.bottom = true;
            var tool1 = new Tool().title("menu3-tool1").uri("menu3-tool1").uriRegex("menu3-tool1").order("1A").icon("fa-user").iconType("sharp");
            var tool2 = new Tool().title("menu3-tool2").uri("menu3-tool2").uriRegex("menu3-tool2").order("1B").icon("fa-user").iconType("sharp");
            menu3.tools = new Tool[] { tool1, tool2 };
        }
        var menu4 = new Menu(); {
            menu4.title = "Menu 4";
            menu4.order = "0";
            menu4.icon = "fa-right-to-bracket";
            menu4.iconType = "sharp";
            menu4.bottom = true;
            var tool1 = new Tool().title("menu4-tool1").uri("menu4-tool1").uriRegex("menu4-tool1").order("0A").icon("fa-user").iconType("sharp");
            menu4.tools = new Tool[] { tool1 };
        }
        menu = new Menu[] { menu2, menu1, menu3, menu4 };
    }
    @Provides @Singleton MenuRepository provideMenuRepository() {
        return ()-> behavior(List.of(menu));
    }
}
