package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.*;

import java.util.List;
import java.util.stream.Collectors;

public class LoadAction extends ComplexAction {
    @AssistedInject LoadAction(TypeRepository typeRepository, LayoutProvider layoutProvider, TypeListEditing typeListEditing, TypeListToUpsert toUpsert, BoxTailor tailor, DeleteBoxAction.DeleteActionFactory deleteActionFactory) {
        super(pipeline(typeRepository, layoutProvider, typeListEditing, toUpsert, tailor, deleteActionFactory));
    }
    private static Action[] pipeline(TypeRepository typeRepository, LayoutProvider layoutProvider, TypeListEditing typeListEditing, TypeListToUpsert toUpsert, BoxTailor tailor, DeleteBoxAction.DeleteActionFactory deleteActionFactory) {
        return new Action[] {
                clear(typeListEditing, deleteActionFactory),
                search(typeRepository, layoutProvider, typeListEditing, toUpsert, tailor)
        };
    }
    private static Action clear(TypeListEditing typeListEditing, DeleteBoxAction.DeleteActionFactory deleteActionFactory) {
        return deleteActionFactory.deleteBox(typeListEditing.getValue());
    }
    private static Action search(TypeRepository typeRepository, LayoutProvider layoutProvider, TypeListEditing typeListEditing, TypeListToUpsert toUpsert, BoxTailor tailor) {
        return new Action() {
            private List<CreateBoxAction> creates;
            @Override
            public void execute() {
                typeRepository.list(layoutProvider.getValue()).subscribe(list->{
                    creates = list.stream().map(box->new CreateBoxAction(typeListEditing, toUpsert, box)).collect(Collectors.toList());
                    for(var create:creates) create.execute();
                });
            }
            @Override
            public void rollback() {
                for(var create:creates) create.rollback();
            }
        };
    }
    private static Type map(Type type, BoxTailor tailor) {
        return type.height(tailor.estimateBoxHeight(type));
    }
    @AssistedFactory
    interface LoadActionFactory {
        LoadAction load();
    }
}
