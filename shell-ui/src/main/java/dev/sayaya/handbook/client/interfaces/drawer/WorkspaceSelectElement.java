package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.SessionContext;
import dev.sayaya.handbook.client.usecase.WorkspaceEventListener;
import dev.sayaya.handbook.client.usecase.WorkspaceList;
import dev.sayaya.rx.subject.BehaviorSubject;
import dev.sayaya.ui.elements.SelectElementBuilder.OutlinedSelectElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;

/**
 * 워크스페이스 선택 드롭다운.
 *
 * <p><b>책임:</b>
 * <ul>
 *   <li>{@link WorkspaceList} 를 구독해 사용자 소유 워크스페이스 목록을 select option 으로 렌더</li>
 *   <li>사용자가 워크스페이스 변경 시 {@link SessionContext} 의 {@code workspaceId} 를 갱신</li>
 *   <li>현재 URL에서 이전 워크스페이스 ID를 새 ID로 교체하여 {@code uri} 스트림에 반영</li>
 *   <li>{@link SessionContext} 를 구독하여 외부(URL 등)에서 변경된 워크스페이스 ID를 UI에 동기화</li>
 * </ul></p>
 */
@Singleton
public class WorkspaceSelectElement implements IsElement<HTMLElement> {
    private final OutlinedSelectElementBuilder _this = select().outlined().label("Workspace").required(true).menuPositioning("popover");
    private List<Workspace> workspaces;

    @Inject
    WorkspaceSelectElement(WorkspaceList workspaces, SessionContext sessionContext, BehaviorSubject<String> uri) {
        workspaces.subscribe(this::update);
        sessionContext.subscribe(ctx -> {
            String wsId = ctx.get("workspaceId");
            if (wsId != null && !wsId.equals(_this.element().value)) {
                _this.element().value = wsId;
            }
        });
        _this.on(EventType.change, evt -> {
            String wsId = _this.element().value;
            String currentUri = uri.getValue();
            if (currentUri != null) {
                // WorkspaceEventListener 의 정적 메서드를 활용해 현재 ID 추출
                String oldWsId = WorkspaceEventListener.extractWorkspaceId(currentUri);
                if (oldWsId != null && !oldWsId.equals(wsId)) {
                    String newUri = currentUri.replace("/workspace/" + oldWsId, "/workspace/" + wsId);
                    uri.next(newUri);
                } else if (oldWsId == null) {
                    // 워크스페이스 컨텍스트가 없는 URL에서 선택한 경우 대시보드로 이동
                    uri.next("/workspace/" + wsId + "/dashboard");
                }
            }
            sessionContext.set("workspaceId", wsId);
        });
    }

    private void update(List<Workspace> workspaces) {
        this.workspaces = workspaces;
        if (workspaces == null || workspaces.isEmpty()) {
            _this.disabled(true);
            _this.element().style.display = "none";
            return;
        }
        _this.disabled(false);
        _this.element().style.display = "";
        _this.removeAllOptions();
        for (var workspace : workspaces) _this.option().value(workspace.id()).headline(workspace.name());
    }

    public WorkspaceSelectElement css(String css) {
        _this.css(css);
        return this;
    }

    @Override
    public HTMLElement element() {
        return _this.element();
    }
}
