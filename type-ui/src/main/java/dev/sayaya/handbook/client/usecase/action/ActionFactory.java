package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Value;
import dev.sayaya.handbook.client.usecase.UpdatableBox;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActionFactory {
    @Delegate private final CreateBoxAction.CreateActionFactory createBox;
    @Delegate private final DeleteBoxAction.DeleteActionFactory deleteBox;
    @Delegate private final PushOutOverlapAction.PushOutOverlapActionFactory pushOutOverlap;
    @Delegate private final SearchAction.SearchActionFactory search;
    @Inject ActionFactory(
            CreateBoxAction.CreateActionFactory createBox,
            DeleteBoxAction.DeleteActionFactory deleteBox,
            PushOutOverlapAction.PushOutOverlapActionFactory pushOutOverlap,
            SearchAction.SearchActionFactory search
    ) {
        this.createBox = createBox;
        this.deleteBox = deleteBox;
        this.pushOutOverlap = pushOutOverlap;
        this.search = search;
    }
    public Action complex(Action... actions) {
        return new ComplexAction(actions);
    }
    public Action move(UpdatableBox element, int deltaX, int deltaY) {
        return new MoveBoxAction(element, deltaX, deltaY);
    }
    public Action addAttribute(UpdatableBox element, Value value) {
        return new AddAttributeAction(element, value);
    }
}
