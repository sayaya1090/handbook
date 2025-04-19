package dev.sayaya.handbook.client.interfaces.create;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

@Singleton
public class DialogElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div = div().css("dialog");
    private final HTMLContainerBuilder<HTMLLabelElement> lblCreateWorkspace = label();
    private final HTMLContainerBuilder<HTMLLabelElement> lblOr = label();
    private final HTMLContainerBuilder<HTMLLabelElement> lblJoinWorkspace = label();
    @Inject DialogElement() {
        div.add(lblCreateWorkspace).add(lblOr).add(lblJoinWorkspace);
        //labels.subscribe(this::update);
        update();
    }
    private void update() {
        lblCreateWorkspace.element().innerHTML = "새로운 워크스페이스를 생성하세요.";
        lblOr.element().innerHTML = "또는";
        lblJoinWorkspace.element().innerHTML = "운영 중인 워크스페이스에 가입하세요. 워크스페이스 ID는 워크스페이스 관리자에게 문의하세요.";
        //headline.element().innerHTML = findLabelOrDefault(label, menu.title()).toUpperCase();
        //supportingText.element().innerHTML = findLabelOrDefault(label, menu.supportingText());
    }
}
