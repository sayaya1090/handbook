package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.handbook.client.domain.Label;
import dev.sayaya.rx.Observable;
import dev.sayaya.ui.elements.ButtonElementBuilder.FilledButtonElementBuilder;
import dev.sayaya.ui.elements.RadioElementBuilder;
import dev.sayaya.ui.elements.TextFieldElementBuilder.OutlinedTextFieldElementBuilder;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLLabelElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.ui.elements.ButtonElementBuilder.button;
import static dev.sayaya.ui.elements.RadioElementBuilder.radio;
import static dev.sayaya.ui.elements.TextFieldElementBuilder.textField;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.label;

@Singleton
public class DialogElement implements IsElement<HTMLDivElement> {
    @Delegate private final HTMLContainerBuilder<HTMLDivElement> div = div().css("dialog");
    private final RadioElementBuilder selectCreateWorkspace = radio().name("create-workspace").value("create");
    private final RadioElementBuilder selectJoinWorkspace = radio().name("create-workspace").value("join");
    private final HTMLContainerBuilder<HTMLLabelElement> lblCreateWorkspace = label();
    private final HTMLContainerBuilder<HTMLLabelElement> lblOr = label();
    private final HTMLContainerBuilder<HTMLLabelElement> lblJoinWorkspace = label();
    private final OutlinedTextFieldElementBuilder iptCreateWorkspace = textField().outlined().css("text-field").label("새로운 워크스페이스 이름");
    private final OutlinedTextFieldElementBuilder iptJoinWorkspace = textField().outlined().css("text-field").label("가입할 워크스페이스 ID");
    private final FilledButtonElementBuilder btn = button().filled().add("생성").style("margin-top: 2rem;");
    @Inject DialogElement(Observable<Label> labels) {
        div.add(div().css("span")
                        .add(selectCreateWorkspace)
                        .add(div().style("display: flex;" +
                                "    flex-direction: column;" +
                                "    margin-left: 1rem;     gap: 1rem;" +
                                "    width: 100%;").add(lblCreateWorkspace).add(iptCreateWorkspace)))
                .add(lblOr.css("divider"))
                .add(div().css("span")
                        .add(selectJoinWorkspace)
                        .add(div().style("display: flex;" +
                                "    flex-direction: column;" +
                                "    margin-left: 1rem;     gap: 1rem;" +
                                "    width: 100%;").add(lblJoinWorkspace).add(iptJoinWorkspace)))
                .add(btn);
        labels.subscribe(this::update);
    }
    private void update(Label label) {
        lblCreateWorkspace.element().innerHTML = "새로운 워크스페이스를 생성하세요.";
        lblOr.element().innerHTML = "또는";
        lblJoinWorkspace.element().innerHTML = "운영 중인 워크스페이스에 가입하세요. 워크스페이스 ID는 워크스페이스 관리자에게 문의하세요.";
        //headline.element().innerHTML = findLabelOrDefault(label, menu.title()).toUpperCase();
        //supportingText.element().innerHTML = findLabelOrDefault(label, menu.supportingText());
    }
}
