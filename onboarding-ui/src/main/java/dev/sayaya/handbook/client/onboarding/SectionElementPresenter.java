package dev.sayaya.handbook.client.onboarding;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;

/**
 * SectionElement의 상태 관리 및 이벤트 로직을 담당하는 Presenter.
 */
public class SectionElementPresenter {
    private final SectionElement view;
    private final String modeName;
    private final CreateWorkspaceMode modeState;
    private final CreateWorkspaceParam param;

    @AssistedInject
    public SectionElementPresenter(
            @Assisted SectionElement view,
            @Assisted String modeName,
            CreateWorkspaceMode modeState,
            CreateWorkspaceParam param
    ) {
        this.view = view;
        this.modeName = modeName;
        this.modeState = modeState;
        this.param = param;

        view.onInputFocus(() -> modeState.next(CreateWorkspaceMode.Mode.valueOf(modeName)));
        view.onInputChanged(val -> {
            if (modeState.getValue() == CreateWorkspaceMode.Mode.valueOf(modeName)) param.next(val);
        });
        view.onRadioChanged(() -> {
            modeState.next(CreateWorkspaceMode.Mode.valueOf(modeName));
            view.focusInput();
        });

        modeState.subscribe(m -> {
            boolean active = (m.name().equals(modeName));
            view.setActive(active);
            if (!active) {
                view.clearInput();
                param.next(null);
            }
        });
    }

    @AssistedFactory
    public interface Factory {
        SectionElementPresenter create(SectionElement view, String modeName);
    }
}
