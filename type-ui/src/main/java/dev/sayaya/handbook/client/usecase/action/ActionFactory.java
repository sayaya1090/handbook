package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Attribute;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActionFactory {
    @Delegate private final CreateBoxAction.CreateActionFactory createBox;
    @Delegate private final DeleteBoxAction.DeleteActionFactory deleteBox;
    @Delegate private final EditBoxAction.EditBoxActionFactory editBox;
    @Delegate private final MoveBoxAction.MoveBoxActionFactory moveBox;
    @Delegate private final PushOutOverlapAction.PushOutOverlapActionFactory pushOutOverlap;
    @Delegate private final LoadAction.LoadActionFactory load;
    @Delegate private final SaveAction.SaveActionFactory save;
    @Delegate private final ChangeLayoutAction.ChangeLayoutActionFactory changeLayout;

    @Inject ActionFactory(
            CreateBoxAction.CreateActionFactory createBox,
            DeleteBoxAction.DeleteActionFactory deleteBox,
            EditBoxAction.EditBoxActionFactory editBox,
            MoveBoxAction.MoveBoxActionFactory moveBox,
            PushOutOverlapAction.PushOutOverlapActionFactory pushOutOverlap,
            LoadAction.LoadActionFactory load,
            SaveAction.SaveActionFactory save,
            ChangeLayoutAction.ChangeLayoutActionFactory changeLayout
    ) {
        this.createBox = createBox;
        this.deleteBox = deleteBox;
        this.editBox = editBox;
        this.moveBox = moveBox;
        this.pushOutOverlap = pushOutOverlap;
        this.load = load;
        this.save = save;
        this.changeLayout = changeLayout;
    }
    public Action complex(Action... actions) {
        return new ComplexAction(actions);
    }
    public Action addAttribute(UpdatableBox element, Attribute value) {
        return new AddAttributeAction(element, value);
    }
}
