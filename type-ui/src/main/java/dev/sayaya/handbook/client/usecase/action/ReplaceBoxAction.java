package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;

class ReplaceBoxAction extends ComplexAction {
    @AssistedInject ReplaceBoxAction(@Assisted("before") Type before, @Assisted("after") Type after,
                                     DeleteBoxAction.DeleteActionFactory deleteActionFactory,
                                     CreateBoxAction.CreateActionFactory createActionFactory) {
        super(deleteActionFactory.deleteBox(before), createActionFactory.createBox(after));
    }

    @AssistedFactory interface ReplaceBoxActionFactory {
        ReplaceBoxAction _replaceBox(@Assisted("before") Type before, @Assisted("after") Type after);
        default Action replaceBox(Type before, Type after) {
            return _replaceBox(before, after);
        }
    }
}
