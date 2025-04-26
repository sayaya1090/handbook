package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.usecase.BoxList;
import dev.sayaya.handbook.client.usecase.TypeRepository;
import elemental2.dom.DomGlobal;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SaveAction implements Action {
    private final TypeRepository typeRepository;
    private final BoxList boxList;
    @AssistedInject SaveAction(TypeRepository typeRepository, BoxList boxList) {
        this.typeRepository = typeRepository;
        this.boxList = boxList;
    }
    @Override
    public void execute() {
        var list = Arrays.stream(boxList.getValue()).collect(Collectors.toList());
        typeRepository.save(list).subscribe(complete->{
            DomGlobal.alert("저장되었습니다.");
        });
    }
    @Override
    public void rollback() {
        throw new UnsupportedOperationException("rollback");
    }
    @AssistedFactory
    interface SaveActionFactory {
        SaveAction save();
    }
}
