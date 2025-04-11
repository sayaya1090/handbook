package dev.sayaya.handbook.client.interfaces.frame;

import dev.sayaya.handbook.client.usecase.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.subject.BehaviorSubject;
import elemental2.dom.DomGlobal;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class FrameUpdater {
    @Delegate private final BehaviorSubject<FrameElement> frame = behavior(null);
    private final FrameContainer parent;
    private final FrameFactory factory;
    private final Observable<Render> render;
    @Inject FrameUpdater(FrameContainer parent, FrameFactory factory, Observable<Render> render) {
        this.parent = parent;
        this.factory = factory;
        this.render = render;
    }
    public void initialize() {
        render.distinctUntilChanged().subscribe(r -> {
            if(r==null) return;
            var frame = factory.frame();
            r.onInvoke(frame.element());
            next(frame);
        });
    }
    public void next(FrameElement next) {
        var prev = frame.getValue();
        if(prev!=null) remove(prev);
        frame.next(next);
        append(next);
    }
    private void remove(FrameElement prev) {
        prev.fadeOut();
        DomGlobal.setTimeout(a -> prev.element().remove(), 100);
    }
    private void append(FrameElement next) {
        parent.add(next);
        next.fadeIn();
    }
}
