package dev.sayaya.handbook.client.interfaces.drawer;

import dev.sayaya.handbook.client.domain.Workspace;
import dev.sayaya.handbook.client.usecase.WorkspaceList;
import dev.sayaya.ui.elements.SelectElementBuilder.OutlinedSelectElementBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static dev.sayaya.ui.elements.SelectElementBuilder.select;

/**
 * 워크스페이스 선택 드롭다운.
 *
 * <p><b>책임:</b> {@link WorkspaceList} 를 구독해 사용자 소유 워크스페이스 목록을 select
 * option 으로 렌더한다. 빈 목록일 때는 자동 disabled.</p>
 *
 * <p><b>배치:</b> 2026-04 AppBar 도입 이후 {@link ShellAppBarElement} 의 center slot 에 상시
 * 노출된다. 이전에는 Drawer header 에 있어 {@link dev.sayaya.handbook.client.usecase.MenuRailMode}
 * 상태(COLLAPSE/HIDE) 에 따라 자체 opacity/width 로 숨기는 로직이 있었으나, 전역 AppBar 로
 * 이관되면서 MenuRailMode 종속 숨김 로직은 제거됨. 워크스페이스는 언제나 변경 가능.</p>
 */
@Singleton
public class WorkspaceSelectElement implements IsElement<HTMLElement> {
    private final OutlinedSelectElementBuilder _this = select().outlined().label("Workspace").required(true).menuPositioning("popover");
    private List<Workspace> workspaces;

    @Inject
    WorkspaceSelectElement(WorkspaceList workspaces) {
        workspaces.subscribe(this::update);
    }

    private void update(List<Workspace> workspaces) {
        this.workspaces = workspaces;
        if (workspaces == null || workspaces.isEmpty()) {
            _this.disabled(true);
            return;
        }
        _this.removeAllOptions();
        _this.disabled(false);
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
