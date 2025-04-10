package dev.sayaya.handbook.client.frame;

import dagger.Binds;
import dagger.Provides;
import dev.sayaya.handbook.client.interfaces.frame.FrameContainer;
import dev.sayaya.handbook.client.usecase.Render;

import javax.inject.Named;

import static org.jboss.elemento.Elements.div;

@dagger.Module
public abstract class FrameMock {
    @Binds abstract FrameContainer frameContainerProvider(FrameContainerImpl impl);
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
