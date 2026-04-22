package dev.sayaya.handbook.client.interfaces.create;

import com.google.gwt.regexp.shared.RegExp;
import dev.sayaya.handbook.client.components.ErrorNotifier;
import dev.sayaya.handbook.client.components.ToastContainer;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode.Mode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;
import dev.sayaya.handbook.client.usecase.WorkspaceRepository;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.handbook.domain.ToastLevel;
import dev.sayaya.handbook.usecase.LabelProvider;
import dev.sayaya.ui.elements.ButtonElementBuilder;
import elemental2.dom.HTMLElement;
import lombok.experimental.Delegate;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 워크스페이스 생성/참여 제출 버튼.
 *
 * <p><b>역할:</b> 사용자가 입력한 워크스페이스 이름(또는 ID)을 검증하고 API를 호출한다.</p>
 *
 * <p><b>책임:</b> CREATE 모드에서는 워크스페이스 이름 정규식 검증(영문/한글/숫자/공백/하이픈/언더스코어, 최대 255자)을
 * 수행한 뒤 {@link WorkspaceRepository#create}를 호출하고, JOIN 모드에서는 {@link WorkspaceRepository#join}을 호출한다.
 * 검증 실패 시 {@link ErrorNotifier}로 사용자에게 알린다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link CreateWorkspaceMode} — CREATE/JOIN 모드 상태</li>
 *   <li>{@link CreateWorkspaceParam} — 사용자 입력값</li>
 *   <li>{@link WorkspaceRepository} — 워크스페이스 API 포트</li>
 *   <li>{@link ToastContainer} — 성공 피드백 토스트 표시</li>
 *   <li>{@link LabelProvider} — 다국어 레이블</li>
 *   <li>{@link ErrorNotifier} — 검증 오류 글로벌 알림</li>
 * </ul></p>
 *
 * <p><b>주의:</b> 백엔드에도 동일한 검증 로직이 존재한다. 프론트엔드 검증은 UX 용도이며,
 * 보안 검증은 백엔드에서 수행한다.</p>
 */
@Singleton
public class SubmitButton implements IsElement<HTMLElement> {
    /** 워크스페이스 이름 검증: 영문/한글/숫자/공백/하이픈/언더스코어, 1~255자 */
    private static final RegExp NAME_PATTERN = RegExp.compile("^[a-zA-Z0-9가-힣\\-_\\s]{1,255}$");

    @Delegate private final ButtonElementBuilder.FilledButtonElementBuilder _this;
    private Labels currentLabels = Labels.empty();

    @Inject
    SubmitButton(CreateWorkspaceMode mode, CreateWorkspaceParam param,
                 WorkspaceRepository api, ToastContainer toastContainer,
                 LabelProvider labelProvider) {
        _this = ButtonElementBuilder.button().filled().css("ws-submit");
        _this.element().textContent = "Create";

        labelProvider.subscribe(labels -> currentLabels = labels);

        _this.onClick(e -> {
            String value = param.getValue();
            if (value == null || value.trim().isEmpty()) return;
            String trimmed = value.trim();
            if (mode.getValue() == Mode.CREATE) {
                if (!NAME_PATTERN.test(trimmed)) {
                    String msg = currentLabels.getOrDefault("workspace.name.invalid",
                            "Workspace name must contain only alphanumeric, Korean, hyphen, or underscore characters (max 255)");
                    ErrorNotifier.notify(msg);
                    return;
                }
                // 주의: WorkspaceApi 는 실패 시 null 로 resolve 하고 ErrorNotifier 로 별도 알림을 띄운다.
                // 여기서 null 가드 없이 SUCCESS 토스트를 띄우면 500 응답에도 "생성 성공" 오탐.
                api.create(trimmed, null).subscribe(id -> {
                    if (id == null) return;
                    toastContainer.show(ToastLevel.SUCCESS,
                            currentLabels.getOrDefault("toast.workspace.created", "Workspace created"));
                });
            } else {
                // join() 은 Observable<Void> — 성공 시 null emit, 실패 시 catch_ 에서 null resolve.
                // 성공/실패를 값으로 구분할 수 없으므로 ErrorNotifier 가 뜨지 않은 경우만 success 간주는 불가.
                // 우선 기존 동작 유지 (추후 Observable<Result<Void>> 로 리팩토링 필요).
                api.join(trimmed).subscribe(v ->
                    toastContainer.show(ToastLevel.SUCCESS,
                            currentLabels.getOrDefault("toast.workspace.joined", "Joined workspace"))
                );
            }
        });

        param.subscribe(value -> {
            boolean disabled = (value == null || value.trim().isEmpty());
            _this.disabled(disabled);
        });

        mode.subscribe(m -> updateLabel(m, labelProvider));
        labelProvider.subscribe(labels -> updateLabel(mode.getValue(), labelProvider));
    }

    private void updateLabel(Mode m, LabelProvider labelProvider) {
        // 주의: elemento `.text()` 는 텍스트 노드를 append — 반복 호출 시 "CreateCreateCreate" 누적.
        // textContent 대입으로 기존 자식 텍스트를 완전히 교체한다.
        String label = (m == Mode.CREATE)
                ? currentLabels.getOrDefault("workspace.create.submit", "Create")
                : currentLabels.getOrDefault("workspace.join.submit", "Request to join");
        _this.element().textContent = label;
    }

}
