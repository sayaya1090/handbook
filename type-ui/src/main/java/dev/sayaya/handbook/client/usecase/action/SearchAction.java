package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.*;

import java.util.Arrays;

public class SearchAction extends ComplexAction {
    @AssistedInject SearchAction(SearchProvider searchProvider, TypeRepository typeRepository, BoxList boxList, BoxTailor tailor) {
        super(pipeline(searchProvider, typeRepository, boxList, tailor));
    }
    private static Action[] pipeline(SearchProvider searchProvider, TypeRepository typeRepository, BoxList boxList, BoxTailor tailor) {
        return new Action[] {
                clear(boxList), search(searchProvider, typeRepository, boxList, tailor)
        };
    }
    private static Action clear(BoxList boxList) {
        return new DeleteBoxAction(boxList, boxList.getValue());
    }
    private static Action search(SearchProvider searchProvider, TypeRepository typeRepository, BoxList boxList, BoxTailor tailor) {
        return new Action() {
            private CreateBoxAction[] creates;
            @Override
            public void execute() {
                var param = searchProvider.getValue();
                typeRepository.search(param).subscribe(page->{
                    var boxes = Arrays.stream(page.content())
                            .map(type->map(type, tailor))
                            .toArray(Box[]::new);
                    creates = Arrays.stream(boxes).map(box->new CreateBoxAction(boxList, box)).toArray(CreateBoxAction[]::new);
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
        var box = Box.builder().type(type).id(type.id()).name(type.id())
                .width(250).height(1).x(1).y(1)
                .build();
        return box.height(tailor.estimateBoxHeight(box));
    }
    @AssistedFactory
    interface SearchActionFactory {
        SearchAction search();
    }
}
