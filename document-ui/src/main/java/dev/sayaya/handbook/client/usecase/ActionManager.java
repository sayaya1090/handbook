package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Action;
import dev.sayaya.handbook.client.domain.Document;
import dev.sayaya.handbook.client.usecase.action.ActionFactory;
import elemental2.dom.DomGlobal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class ActionManager {
    private static final int MAX_STACK_SIZE = 100;
    private final LinkedList<Action> undo = new LinkedList<>();
    private final LinkedList<Action> redo = new LinkedList<>();
    private final ActionFactory factory;
    private final TypeProvider type;
    @Inject ActionManager(ActionFactory factory, TypeProvider type) {
        this.factory = factory;
        this.type = type;
    }
    public void create() {
        var type = this.type.getValue();
        var document = Document.builder().id("Document-" + generateUniqueString())
                .type(type.id())
                .effectDateTime(type.effectDateTime()).expireDateTime(type.expireDateTime())
                .build();
        var action = factory.add(document);
        push(action);
        action.execute();
    }
    public void edit(Document before, Document after) {
        var action = factory.edit(before, after);
        push(action);
        action.execute();
    }
    public void remove(List<Document> docs) {
        var action = factory.delete(docs);
        push(action);
        action.execute();
    }
    private void push(Action action) {
        if (undo.size() >= MAX_STACK_SIZE) undo.removeFirst(); // 가장 오래된 작업 제거
        undo.add(action);
        redo.clear();
    }
    public synchronized void undo() {
        if(undo.isEmpty()) return;
        var last = undo.removeLast();
        last.rollback();
        redo.add(last);
    }
    public synchronized void redo() {
        if(redo.isEmpty()) return;
        var last = redo.removeLast();
        last.execute();
        undo.add(last);
    }
    public void save() {
        var action = factory.save();
        action.execute();
    }
    private static String generateUniqueString() {
        return Double.toString(Math.random()).substring(2, 7); // 랜덤 숫자 (문자열)
    }
}
