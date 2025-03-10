package dev.sayaya.handbook.client.usecase.action;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Box;
import dev.sayaya.handbook.client.usecase.BoxList;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;

import java.util.Arrays;

public class CreateBoxAction implements Action {
    private final Box box;
    private final BoxList subject;
    public CreateBoxAction(BoxList boxList, double x, double y) {
        DomGlobal.console.log("Create Box:" + x + ", " + y);
        box = new Box("Untitle", null, (int)x, (int)y, 100, 100);
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
