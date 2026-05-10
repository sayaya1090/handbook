package dev.sayaya.handbook.client.onboarding;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.*;

/**
 * 워크스페이스 생성 다이얼로그.
 *
 * <p><b>역할:</b> 사용자가 최초 진입 시 워크스페이스를 새로 만들거나(CREATE) 기존 워크스페이스에
 * 참여(JOIN) 를 요청할 수 있는 카드형 다이얼로그 UI.</p>
 *
 * <p><b>구성:</b>
 * <ul>
 *   <li>헤더 — 타이틀(headline-small) + 서브타이틀(body-medium)</li>
 *   <li>{@code .ws-section.ws-section-create} — 새 워크스페이스 입력 카드</li>
 *   <li>{@code .ws-divider} — "or" 가 박혀 있는 수평 디바이더</li>
 *   <li>{@code .ws-section.ws-section-join} — 기존 워크스페이스 ID 입력 카드</li>
 *   <li>{@link SubmitButton} — 하단 full-width 제출 버튼</li>
 * </ul></p>
 *
 * <p><b>Dumb View 책임:</b> 레이아웃 구성 및 뷰 요소 접근자 제공.
 * 초기화 및 다국어 로직은 {@link DialogElementPresenter} 로 위임됨.</p>
 */
@Singleton
public class DialogElement implements IsElement<HTMLDivElement> {
    private final HTMLDivElement root;
    private SectionElement createSection;
    private SectionElement joinSection;
    private final HTMLElement title;
    private final HTMLElement subtitle;
    private final HTMLElement dividerLabel;

    @Inject
    DialogElement(SubmitButton submitButton) {
        title = h(1).css("ws-title").element();
        subtitle = p().css("ws-subtitle").element();
        HTMLElement headerEl = header().css("ws-header")
                .add(title)
                .add(subtitle)
                .element();

        dividerLabel = span().css("ws-divider-label").element();
        HTMLElement divider = div().css("ws-divider")
                .add(dividerLabel)
                .element();

        root = div().css("ws-dialog")
                .add(headerEl)
                .add(divider)
                .add(submitButton)
                .element();
    }

    public void initSections(SectionElement create, SectionElement join) {
        if (this.createSection != null) this.createSection.element().remove();
        if (this.joinSection != null) this.joinSection.element().remove();
        this.createSection = create;
        this.joinSection = join;
        root.insertBefore(create.element(), root.childNodes.item(1));
        root.insertBefore(join.element(), root.childNodes.item(3));
    }


    public void setTitle(String text) { title.textContent = text; }
    public void setSubtitle(String text) { subtitle.textContent = text; }
    public void setDividerLabel(String text) { dividerLabel.textContent = text; }
    public SectionElement getCreateSection() { return createSection; }
    public SectionElement getJoinSection() { return joinSection; }

    @Override
    public HTMLDivElement element() { return root; }
}
