package dev.sayaya.handbook.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class TestApplication implements EntryPoint {
    @Override
    public void onModuleLoad() {
        var components = DaggerTestComponent.create();
        var api = components.api();
        //LabelProvider labels = components.labelProvider();

        // 탭 컨테이너 구조를 직접 구현하여 테스트
        var container = div().css("workspace-mgmt-container")
                .add(div().css("mgmt-tabs")
                        .add(div().css("mgmt-tab", "mgmt-tab-active").text("General"))
                        .add(div().css("mgmt-tab").text("Groups & Members"))
                        .add(div().css("mgmt-tab").text("Roles & Permissions")))
                .add(div().css("mgmt-content"));
                       // .add(new InfoTabElement(api, labels).element()));

        body().add(container);
        DomGlobal.console.log("!!! TestApplication Render SUCCESS !!!");
    }
}
