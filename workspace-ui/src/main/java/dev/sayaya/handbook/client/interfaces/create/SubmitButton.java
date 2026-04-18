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
 * <p><b>책임:</b> CREATE 모드에서는 워크스페이스 이름 정규식 검증(영문/한글/숫자/하이픈/언더스코어, 최대 255자)을
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
    /** 워크스페이스 이름 검증: 영문/한글/숫자/하이픈/언더스코어, 1~255자 */
    private static final RegExp NAME_PATTERN = RegExp.compile("^[a-zA-Z0-9가-힣\\-_]{1,255}$");

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
                api.create(trimmed, null).subscribe(v ->
                    toastContainer.show(ToastLevel.SUCCESS,
                            currentLabels.getOrDefault("toast.workspace.created", "Workspace created"))
                );
            } else {
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
