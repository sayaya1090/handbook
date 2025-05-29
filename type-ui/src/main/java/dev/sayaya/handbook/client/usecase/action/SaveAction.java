package dev.sayaya.handbook.client.usecase.action;

import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.handbook.client.usecase.*;
import elemental2.dom.DomGlobal;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SaveAction implements Action {
    private final TypeRepository typeRepository;
    private final TypeList typeList;

    @AssistedInject SaveAction(TypeRepository typeRepository, TypeList typeList) {
        this.typeRepository = typeRepository;
        this.typeList = typeList;
    }
    @Override
    public void execute() {
        var deletes = typeList.getValue().stream().filter(t->t.state() == Type.TypeState.DELETE).collect(Collectors.toUnmodifiableSet());
        var upserts = typeList.getValue().stream().filter(t->t.state() == Type.TypeState.CHANGE).collect(Collectors.toUnmodifiableSet());
        typeRepository.delete(deletes).concatWith(typeRepository.save(upserts)).subscribe(complete->{
            DomGlobal.alert("저장되었습니다.");
            typeList.reset();
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
