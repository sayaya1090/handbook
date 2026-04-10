package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.handbook.client.usecase.PageState;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;

/**
 * 페이지네이션 UI 컴포넌트.
 *
 * <p><b>책임:</b> 이전/다음 버튼과 현재 페이지 번호를 표시하며,
 * 버튼 클릭 시 {@link PageState}의 페이지 번호를 증감시켜 문서 목록 조회를 트리거한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link PageState} — 현재 검색/페이지 상태 구독 및 갱신</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 페이지 번호는 0-base이며, UI에는 1-base로 표시된다.
 * 첫 페이지에서는 이전 버튼에 "disabled" CSS 클래스가 추가된다.</p>
 */
@Singleton
public class PaginationElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;
    private final elemental2.dom.HTMLElement pageInfo;
    private final elemental2.dom.HTMLElement prevButton;
    private final elemental2.dom.HTMLElement nextButton;
    private final PageState pageState;

    @Inject
    public PaginationElement(PageState pageState) {
        this.pageState = pageState;
        this.pageInfo = span().css("doc-page-info").element();
        this.prevButton = button().css("doc-page-btn", "doc-page-prev").element();
        this.prevButton.textContent = "\u25C0";
        this.nextButton = button().css("doc-page-btn", "doc-page-next").element();
        this.nextButton.textContent = "\u25B6";
        this.element = div().css("doc-pagination")
                .add(prevButton)
                .add(pageInfo)
                .add(nextButton)
                .element();

        prevButton.addEventListener("click", e -> {
            Search current = pageState.getValue();
            if (current.page > 0) {
                current.page--;
                pageState.next(current);
            }
        });

        nextButton.addEventListener("click", e -> {
            Search current = pageState.getValue();
            current.page++;
            pageState.next(current);
        });

        pageState.asObservable().subscribe(search -> updatePageInfo(search));
    }

    private void updatePageInfo(Search search) {
        pageInfo.textContent = "Page " + (search.page + 1);
        prevButton.classList.toggle("disabled", search.page <= 0);
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
