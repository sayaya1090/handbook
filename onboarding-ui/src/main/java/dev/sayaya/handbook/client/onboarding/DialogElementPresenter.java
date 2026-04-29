package dev.sayaya.handbook.client.onboarding;

import dev.sayaya.handbook.client.usecase.CreateWorkspaceMode;
import dev.sayaya.handbook.client.usecase.CreateWorkspaceParam;
import dev.sayaya.handbook.usecase.LabelProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * DialogElement의 상태 구독 및 초기화 로직을 담당하는 Presenter.
 */
@Singleton
public class DialogElementPresenter {
    private final DialogElement view;
    private final SectionBuilder sectionBuilder;
    private final CreateWorkspaceMode modeState;
    private final CreateWorkspaceParam param;
    private final LabelProvider labelProvider;

    @Inject
    public DialogElementPresenter(
            DialogElement view,
            SectionBuilder sectionBuilder,
            CreateWorkspaceMode modeState,
            CreateWorkspaceParam param,
            LabelProvider labelProvider
    ) {
        this.view = view;
        this.sectionBuilder = sectionBuilder;
        this.modeState = modeState;
        this.param = param;
        this.labelProvider = labelProvider;

        labelProvider.subscribe(labels -> {
            view.setTitle(labels.getOrDefault("workspace.dialog.title", "Start your workspace"));
            view.setSubtitle(labels.getOrDefault("workspace.dialog.subtitle", "Create a new workspace or join an existing one to get started."));
            view.setDividerLabel(labels.getOrDefault("workspace.or", "or"));

            SectionElement createSection = sectionBuilder.cssClass("ws-section-create")
                    .label(labels.getOrDefault("workspace.create", "Create a new workspace"))
                    .supportingText(labels.getOrDefault("workspace.create.hint", "Pick a name for your team or project."))
                    .placeholder(labels.getOrDefault("workspace.create.name", "New workspace name"))
                    .build(CreateWorkspaceMode.Mode.CREATE.name());

            SectionElement joinSection = sectionBuilder.cssClass("ws-section-join")
                    .label(labels.getOrDefault("workspace.join", "Join an existing workspace"))
                    .supportingText(labels.getOrDefault("workspace.join.hint", "Ask your administrator for the workspace ID."))
                    .placeholder(labels.getOrDefault("workspace.join.id", "Workspace ID to join"))
                    .build(CreateWorkspaceMode.Mode.JOIN.name());
            
            view.initSections(createSection, joinSection);
        });
    }
}
