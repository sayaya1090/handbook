package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import elemental2.dom.DomGlobal;

public class ChangeLayoutAction implements Action {
    private final LayoutProvider layout;
    private Period before;
    private final Period next;
    @AssistedInject ChangeLayoutAction(LayoutProvider layout, @Assisted Period next) {
        this.layout = layout;
        this.next = next;
    }
    @Override
    public void execute() {
        DomGlobal.console.log("layout changed to "+next);
        if(before == null) before = layout.getValue();
        layout.next(next);
    }
    @Override
    public void rollback() {
        layout.next(before);
    }
    @AssistedFactory
    interface ChangeLayoutActionFactory {
        ChangeLayoutAction changeLayout(Period next);
    }
}
