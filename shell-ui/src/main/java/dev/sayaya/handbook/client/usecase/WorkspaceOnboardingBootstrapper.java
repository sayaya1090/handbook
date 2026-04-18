package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 사용자의 워크스페이스 목록이 비어 있을 때 workspace-ui 모듈을 자동 선택해
 * Create/Join 온보딩 화면을 표시한다.
 *
 * <p><b>책임:</b> {@link WorkspaceList} 를 구독해 빈 목록이 방출되면 가상 onboarding
 * {@link Menu} 를 {@link MenuSelected} 에 push 한다. 이후의 script 주입과 프레임 렌더는
 * shell 의 기존 파이프라인({@link ModuleScriptManager} + frame bridge)이 담당한다.</p>
 *
 * <p><b>주의:</b> onboarding menu 는 MenuRail/MobileTabs 에는 표시되지 않는 가상 엔트리다
 * — MenuList 에 없고 오직 MenuSelected 에만 흘러간다. 사용자가 워크스페이스 생성 후
 * WorkspaceList 가 non-empty 로 바뀌면 UrlBasedMenuResolver 의 정상 경로로 복귀한다.
 * 한 번 push 된 뒤에는 중복 실행되지 않도록 loaded 플래그로 가드한다.</p>
 */
@Singleton
public class WorkspaceOnboardingBootstrapper {
    private static final Menu ONBOARDING_MENU = Menu.builder()
            .title("workspace.onboarding")
            .script("js/workspace/workspace.nocache.js")
            .icon("fa-circle-plus")
            .iconType("solid")
            .order("0")
            .build();

    private final MenuSelected menuSelected;
    private boolean loaded = false;

    @Inject
    WorkspaceOnboardingBootstrapper(WorkspaceList workspaces, MenuSelected menuSelected) {
        this.menuSelected = menuSelected;
        workspaces.distinctUntilChanged().subscribe(list -> {
            if (list == null || !list.isEmpty()) return;
            triggerOnce();
        });
    }

    public void initialize() {
        // eager 보장용 — Dagger 주입으로 subscribe 가 이미 시작되어 있다.
    }

    private void triggerOnce() {
        if (loaded) return;
        loaded = true;
        menuSelected.next(ONBOARDING_MENU);
    }
}
