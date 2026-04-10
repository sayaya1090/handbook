package dev.sayaya.handbook.client.frame;

import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;
import dev.sayaya.handbook.domain.Render;
import dev.sayaya.rx.Observable;
import dev.sayaya.rx.Observer;
import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Named;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;
import static org.jboss.elemento.Elements.div;

@dagger.Module
public abstract class FrameMock {
    @Binds abstract FrameContainer frameContainerProvider(FrameContainerImpl impl);
    @Provides @Singleton static BehaviorSubject<Render> renderSubject() {
        return behavior(null);
    }
    @Provides @Singleton static Observable<Render> renderObservable(BehaviorSubject<Render> subject) {
        return subject.asObservable();
    }
    @Provides @Singleton static Observer<Render> renderObserver(BehaviorSubject<Render> subject) {
        return subject;
    }
    @Provides @Named("renderer1") static Render provideRender1() {
        return elem -> {
            elem.append("Hello, World!!");
            return true;
        };
    }
    @Provides @Named("renderer2") static Render provideRender2() {
        return elem -> {
            elem.append(div().css("color", "blue").add("2nd Renderer rendered").element());
            return true;
        };
    }
}
