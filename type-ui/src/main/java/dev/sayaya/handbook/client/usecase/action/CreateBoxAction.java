package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.BoxList;
import elemental2.core.JsArray;

import java.util.Arrays;

public class CreateBoxAction implements Action {
    private final Box box;
    private final BoxList subject;
    public CreateBoxAction(BoxList boxList, Box box) {
        this.box = box;
        subject = boxList;
    }
    @Override
    public void execute() {
        var array = JsArray.asJsArray(subject.getValue());
        array.push(box);
        var next = array.asList().stream().toArray(Box[]::new);
        subject.next(next);
    }
    @Override
    public void rollback() {
        var next = Arrays.stream(subject.getValue()).filter(s->s!=box).toArray(Box[]::new);
        subject.next(next);
    }
}
