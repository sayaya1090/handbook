package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.BoxList;
import dev.sayaya.handbook.client.usecase.BoxTailor;
import dev.sayaya.handbook.client.usecase.LayoutProvider;
import dev.sayaya.handbook.client.usecase.TypeRepository;

import java.util.List;
import java.util.stream.Collectors;

public class LoadAction extends ComplexAction {
    @AssistedInject LoadAction(TypeRepository typeRepository, LayoutProvider layoutProvider, BoxList boxList, BoxTailor tailor) {
        super(pipeline(typeRepository, layoutProvider, boxList, tailor));
    }
    private static Action[] pipeline(TypeRepository typeRepository, LayoutProvider layoutProvider, BoxList boxList, BoxTailor tailor) {
        return new Action[] {
                clear(boxList), search(typeRepository, layoutProvider, boxList, tailor)
        };
    }
    private static Action clear(BoxList boxList) {
        return new DeleteBoxAction(boxList, boxList.getValue());
    }
    private static Action search(TypeRepository typeRepository, LayoutProvider layoutProvider, BoxList boxList, BoxTailor tailor) {
        return new Action() {
            private List<CreateBoxAction> creates;
            @Override
            public void execute() {
                typeRepository.list(layoutProvider.getValue()).subscribe(list->{
                    creates = list.stream().map(box->new CreateBoxAction(boxList, box)).collect(Collectors.toList());
                    for(var create:creates) create.execute();
                });
            }
            @Override
            public void rollback() {
                for(var create:creates) create.rollback();
            }
        };
    }
    private static Box map(Type type, BoxTailor tailor) {
        var box = Box.builder().type(type).width(250).height(1).x(1).y(1).build();
        return box.height(tailor.estimateBoxHeight(box));
    }
    @AssistedFactory
    interface LoadActionFactory {
        LoadAction load();
    }
}
