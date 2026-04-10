package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.DocumentList;
import dev.sayaya.handbook.client.usecase.DocumentRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.usecase.LabelProvider;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.jboss.elemento.Elements.button;

/**
 * 변경사항 저장 버튼.
 *
 * <p><b>책임:</b> 클릭 시 {@link DocumentRepository#save}를 호출하여 현재 문서 목록을
 * 서버에 저장하고, 성공 시 {@link ActionManager#clear()}로 Undo/Redo 스택을 초기화한 뒤
 * 성공 토스트를 표시한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link ActionManager} — 저장 성공 후 액션 스택 초기화</li>
 *   <li>{@link DocumentList} — 저장할 문서 목록 조회</li>
 *   <li>{@link DocumentRepository} — 서버 저장 API 호출</li>
 *   <li>{@link ToastContainer} — 성공 피드백 토스트 표시</li>
 *   <li>{@link dev.sayaya.handbook.usecase.LabelProvider} — 버튼 레이블 다국어 처리</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 저장은 비동기로 수행되며, 저장 실패 시 액션 스택은 유지된다.</p>
 */
@Singleton
public class SaveButton implements IsElement<elemental2.dom.HTMLElement> {
    private final elemental2.dom.HTMLElement element;
    private Labels currentLabels = Labels.empty();

    @Inject
    public SaveButton(ActionManager actionManager, DocumentList documentList,
                      DocumentRepository documentRepository, ToastContainer toastContainer,
                      LabelProvider labelProvider) {
        this.element = button().css("doc-ctrl-btn", "doc-ctrl-btn-save").element();
        labelProvider.subscribe(labels -> {
            currentLabels = labels;
            element.textContent = labels.getOrDefault("document.save", "Save");
        });
        element.addEventListener("click", e ->
            documentRepository.save(documentList.getValue()).subscribe(v -> {
                actionManager.clear();
                toastContainer.show(ToastLevel.SUCCESS,
                        currentLabels.getOrDefault("toast.save.success", "Save completed"));
            })
        );
    }

    @Override
    public elemental2.dom.HTMLElement element() { return element; }
}
