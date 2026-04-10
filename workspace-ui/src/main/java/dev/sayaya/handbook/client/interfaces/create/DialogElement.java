package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;

/**
 * 워크스페이스 생성 다이얼로그.
 * CREATE(새로 만들기) / JOIN(기존 참여) 두 섹션과 제출 버튼으로 구성.
 */
@Singleton
public class DialogElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private final SectionElement createSection;
    private final SectionElement joinSection;

    @Inject
    DialogElement(SectionElementFactory factory, SubmitButton submitButton, LabelProvider labelProvider) {
        createSection = factory.create(Mode.CREATE);
        joinSection = factory.create(Mode.JOIN);

        HTMLElement divider = div().css("ws-divider").element();
        divider.textContent = "or";

        root = div().css("ws-dialog")
                .add(createSection)
                .add(divider)
                .add(joinSection)
                .add(submitButton)
                .element();

        labelProvider.subscribe(labels -> {
            createSection.label(labels.getOrDefault("workspace.create", "Create a new workspace."))
                    .placeholder(labels.getOrDefault("workspace.create.name", "New workspace name"));
            divider.textContent = labels.getOrDefault("workspace.or", "or");
            joinSection.label(labels.getOrDefault("workspace.join", "Join an existing workspace."))
                    .placeholder(labels.getOrDefault("workspace.join.id", "Workspace ID to join"));
        });
    }

    @Override
    public HTMLDivElement element() { return root; }
}
