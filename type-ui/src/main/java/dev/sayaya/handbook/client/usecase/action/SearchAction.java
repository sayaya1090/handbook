package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.*;

import java.util.Arrays;

public class SearchAction extends ComplexAction {
    @AssistedInject SearchAction(SearchProvider searchProvider, TypeRepository typeRepository, BoxList boxList, BoxTailor tailor, UpdatableBoxList updatableBoxList) {
        super(pipeline(searchProvider, typeRepository, boxList, tailor, updatableBoxList));
    }
    private static Action[] pipeline(SearchProvider searchProvider, TypeRepository typeRepository, BoxList boxList, BoxTailor tailor, UpdatableBoxList updatableBoxList) {
        return new Action[] {
                clear(boxList), search(searchProvider, typeRepository, boxList, tailor, updatableBoxList)
        };
    }
    private static Action clear(BoxList boxList) {
        return new DeleteBoxAction(boxList, boxList.getValue());
    }
    private static Action search(SearchProvider searchProvider, TypeRepository typeRepository, BoxList boxList, BoxTailor tailor, UpdatableBoxList updatableBoxList) {
        return new Action() {
            private CreateBoxAction[] creates;
            private PushOutOverlapAction pushOutOverlap;
            @Override
            public void execute() {
                var param = searchProvider.getValue();
                typeRepository.search(param).subscribe(page->{
                    var boxes = Arrays.stream(page.content())
                            .map(SearchAction::map)
                            .map(box->box.height(tailor.estimateBoxHeight(box)))
                            .toArray(Box[]::new);
                    creates = Arrays.stream(boxes).map(box->new CreateBoxAction(boxList, box)).toArray(CreateBoxAction[]::new);
                    for(var create:creates) create.execute();
                    pushOutOverlap = new PushOutOverlapAction(updatableBoxList);
                    pushOutOverlap.execute();
                });
            }
            @Override
            public void rollback() {
                pushOutOverlap.rollback();
                for(var create:creates) create.rollback();
            }
        };
    }
    private static Box map(Type type) {
        return Box.builder().type(type).id(type.id()).name(type.id())
                .width(200).height(1).x(500).y(500)
                .build();
    }
    @AssistedFactory
    interface SearchActionFactory {
        SearchAction search();
    }
}
