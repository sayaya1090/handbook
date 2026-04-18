package dev.sayaya.handbook.client.interfaces.create;

import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;
import dev.sayaya.handbook.usecase.LabelProvider;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.header;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;

/**
 * 워크스페이스 생성 다이얼로그.
 *
 * <p><b>역할:</b> 사용자가 최초 진입 시 워크스페이스를 새로 만들거나(CREATE) 기존 워크스페이스에
 * 참여(JOIN) 를 요청할 수 있는 카드형 다이얼로그 UI.</p>
 *
 * <p><b>구성:</b>
 * <ul>
 *   <li>헤더 — 타이틀(headline-small) + 서브타이틀(body-medium) 로 시각적 계층 제공</li>
 *   <li>{@code .ws-section.ws-section-create} — 새 워크스페이스 이름 입력 카드</li>
 *   <li>{@code .ws-divider} — "or" 가 가운데 박혀 있는 수평 디바이더</li>
 *   <li>{@code .ws-section.ws-section-join} — 기존 워크스페이스 ID 입력 카드</li>
 *   <li>{@link SubmitButton} — 하단 full-width 제출 버튼</li>
 * </ul></p>
 *
 * <p><b>의존관계:</b> {@link SectionElementFactory} 로 모드별 섹션 생성, {@link LabelProvider} 로
 * 다국어 텍스트 주입.</p>
 *
 * <p><b>주의:</b> 기존 테스트(WorkspaceCreateTest, WorkspaceJoinTest) 가 `.ws-dialog`,
 * `.ws-section` (2개), `.ws-section-input`, `.ws-submit` selector 를 사용하므로 CSS 클래스명은
 * 절대 변경하지 않는다. 시각적 리디자인은 CSS 와 부가 요소(header/supporting text) 로만 수행.</p>
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
        createSection.element().classList.add("ws-section-create");
        joinSection.element().classList.add("ws-section-join");

        HTMLElement title = h(1).css("ws-title").element();
        HTMLElement subtitle = p().css("ws-subtitle").element();
        // <header> 시맨틱 태그로 유지. <div> 로 래핑하면 `.ws-section:first-of-type` (WorkspaceTest.kt:47)
        // 셀렉터가 .ws-header 를 첫 div 로 잡아 CREATE 섹션을 매치하지 못한다.
        HTMLElement headerEl = header().css("ws-header")
                .add(title)
                .add(subtitle)
                .element();

        HTMLElement dividerLabel = span().css("ws-divider-label").element();
        HTMLElement divider = div().css("ws-divider")
                .add(dividerLabel)
                .element();
        dividerLabel.textContent = "or";

        root = div().css("ws-dialog")
                .add(headerEl)
                .add(createSection)
                .add(divider)
                .add(joinSection)
                .add(submitButton)
                .element();

        labelProvider.subscribe(labels -> {
            title.textContent = labels.getOrDefault("workspace.dialog.title", "Start your workspace");
            subtitle.textContent = labels.getOrDefault("workspace.dialog.subtitle",
                    "Create a new workspace or join an existing one to get started.");
            createSection.label(labels.getOrDefault("workspace.create", "Create a new workspace"))
                    .supportingText(labels.getOrDefault("workspace.create.hint",
                            "Pick a name for your team or project."))
                    .placeholder(labels.getOrDefault("workspace.create.name", "New workspace name"));
            dividerLabel.textContent = labels.getOrDefault("workspace.or", "or");
            joinSection.label(labels.getOrDefault("workspace.join", "Join an existing workspace"))
                    .supportingText(labels.getOrDefault("workspace.join.hint",
                            "Ask your administrator for the workspace ID."))
                    .placeholder(labels.getOrDefault("workspace.join.id", "Workspace ID to join"));
        });
    }

    @Override
    public HTMLDivElement element() { return root; }
}
