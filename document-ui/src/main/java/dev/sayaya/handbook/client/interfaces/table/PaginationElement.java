package dev.sayaya.handbook.client.interfaces.table;

import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.PageState;
import dev.sayaya.handbook.usecase.LabelProvider;
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
 * 버튼 클릭 시 {@link PageState}의 페이지 번호를 증감시켜 문서 목록 조회를 트리거한다.
 * 첫 페이지에서는 이전 버튼을, 마지막 페이지에서는 다음 버튼을 비활성화한다.
 * 검색 결과가 없으면 LabelProvider를 통해 안내 메시지를 표시한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link PageState} — 현재 검색/페이지 상태 구독 및 갱신</li>
 *   <li>{@link DocumentList} — 현재 문서 목록 (결과 수 기반 마지막 페이지 판단)</li>
 *   <li>{@link LabelProvider} — 다국어 레이블 (결과 없음 메시지)</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 페이지 번호는 0-base이며, UI에는 1-base로 표시된다.
 * 마지막 페이지 판단은 결과 수가 페이지 크기(limit)보다 작은지로 결정한다.</p>
 */
@Singleton
public class PaginationElement implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLDivElement element;
    private final elemental2.dom.HTMLElement pageInfo;
    private final elemental2.dom.HTMLElement prevButton;
    private final elemental2.dom.HTMLElement nextButton;
    private final elemental2.dom.HTMLElement emptyMessage;
    private final PageState pageState;
    private boolean hasMore = true;

    @Inject
    public PaginationElement(PageState pageState, DocumentList documentList, LabelProvider labelProvider) {
        this.pageState = pageState;
        this.pageInfo = span().css("doc-page-info").element();
        this.prevButton = button().css("doc-page-btn", "doc-page-prev").element();
        this.prevButton.textContent = "\u25C0";
        this.nextButton = button().css("doc-page-btn", "doc-page-next").element();
        this.nextButton.textContent = "\u25B6";
        this.emptyMessage = span().css("doc-page-empty").element();
        this.emptyMessage.textContent = "No results";
        this.emptyMessage.style.set("display", "none");
        this.element = div().css("doc-pagination")
                .add(prevButton)
                .add(pageInfo)
                .add(emptyMessage)
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
            if (hasMore) {
                current.page++;
                pageState.next(current);
            }
        });

        // 문서 목록이 변경되면 마지막 페이지 여부를 판단
        documentList.asObservable().subscribe(docs -> {
            Search search = pageState.getValue();
            int resultCount = docs != null ? docs.size() : 0;
            hasMore = resultCount >= search.limit;

            boolean isEmpty = resultCount == 0 && search.page == 0;
            emptyMessage.style.set("display", isEmpty ? "" : "none");
            pageInfo.style.set("display", isEmpty ? "none" : "");

            updatePageInfo(search);
        });

        pageState.asObservable().subscribe(this::updatePageInfo);

        // 다국어 레이블 갱신
        labelProvider.subscribe(labels -> {
            emptyMessage.textContent = labels.getOrDefault("document.pagination.empty", "No results");
        });
    }

    private void updatePageInfo(Search search) {
        pageInfo.textContent = "Page " + (search.page + 1);
        boolean isFirstPage = search.page <= 0;
        prevButton.classList.toggle("disabled", isFirstPage);
        prevButton.toggleAttribute("disabled", isFirstPage);
        nextButton.classList.toggle("disabled", !hasMore);
        nextButton.toggleAttribute("disabled", !hasMore);
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
