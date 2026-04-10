package dev.sayaya.handbook.client.api;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * UC-S8 테스트 EntryPoint.
 * FetchMock을 통해 UserApi가 사용자 정보를 정상적으로 가져오는지 확인한다.
 */
public class Application implements EntryPoint {
    @Override
    public void onModuleLoad() {
        ApiTestComponent component = DaggerApiTestComponent.create();

        HTMLDivElement statusDiv = (HTMLDivElement) DomGlobal.document.createElement("div");
        statusDiv.id = "api-test-status";
        statusDiv.textContent = "loading";
        body().add(statusDiv);

        // UserApi를 통해 사용자 정보 조회
        component.userRepository().find().subscribe(user -> {
            if (user != null) {
                HTMLElement userIdSpan = (HTMLElement) DomGlobal.document.createElement("span");
                userIdSpan.id = "user-id";
                userIdSpan.textContent = user.id();

                HTMLElement userNameSpan = (HTMLElement) DomGlobal.document.createElement("span");
                userNameSpan.id = "user-name";
                userNameSpan.textContent = user.name();

                HTMLDivElement userDiv = (HTMLDivElement) DomGlobal.document.createElement("div");
                userDiv.id = "user-info";
                userDiv.appendChild(userIdSpan);
                userDiv.appendChild(userNameSpan);
                DomGlobal.document.body.appendChild(userDiv);

                DomGlobal.document.getElementById("api-test-status").textContent = "user-loaded";
                DomGlobal.console.log("[UC-S8] User loaded: " + user.name());
                DomGlobal.console.log("[UC-S8] Periodic refresh is configured");
            } else {
                DomGlobal.document.getElementById("api-test-status").textContent = "user-null";
            }
        });

        // MenuApi를 통해 메뉴 정보 조회
        component.menuRepository().findAll().subscribe(menus -> {
            if (menus != null && !menus.isEmpty()) {
                HTMLElement menuCountSpan = (HTMLElement) DomGlobal.document.createElement("span");
                menuCountSpan.id = "menu-count";
                menuCountSpan.textContent = String.valueOf(menus.size());

                HTMLDivElement menuDiv = (HTMLDivElement) DomGlobal.document.createElement("div");
                menuDiv.id = "menu-info";
                menuDiv.appendChild(menuCountSpan);
                DomGlobal.document.body.appendChild(menuDiv);

                String status = DomGlobal.document.getElementById("api-test-status").textContent;
                if ("user-loaded".equals(status)) {
                    DomGlobal.document.getElementById("api-test-status").textContent = "all-loaded";
                }
                DomGlobal.console.log("[UC-S8] Menus loaded: " + menus.size());
            }
        });
    }
}
