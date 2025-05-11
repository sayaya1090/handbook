package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Period;
import dev.sayaya.handbook.client.usecase.*;

import java.util.List;
import java.util.stream.Collectors;

// 현재 레이아웃의 정보를 삭제하고 DB에서 가져온다.
public class LoadAction implements Action {
    @AssistedInject LoadAction(TypeRepository typeRepository, LayoutProvider layoutProvider,
                               TypeList typeListEditing,
                               CreateBoxAction.CreateActionFactory createActionFactory,
                               DeleteBoxAction.DeleteActionFactory deleteActionFactory, PeriodRecalculationService layoutRecalculateService) {

    }
    @Override
    public void execute() {

    }
    @Override
    public void rollback() {

    }
    @AssistedFactory
    interface LoadActionFactory {
        LoadAction load();
    }
}
