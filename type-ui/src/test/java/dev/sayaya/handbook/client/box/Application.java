package dev.sayaya.handbook.client.box;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.handbook.client.domain.Box;
import elemental2.dom.HTMLElement;
import org.jboss.elemento.IsElement;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override public void onModuleLoad() {
        var canvas = div();
        components.boxElementList().subscribe(elements -> update(canvas.element(), elements));
        body().add(canvas);

        components.boxList().next(new Box[] {
                new Box("Box1", null, 1, 1, 100, 100 ),
                new Box("Box1", null, 1, 1, 100, 100 ),
                new Box("Box1", null, 1, 1, 100, 100 )
        });
    }
    private void update(HTMLElement canvas, IsElement<?>[] elements) {
        while (canvas.firstChild != null) canvas.removeChild(canvas.firstChild);
        for (var element : elements) canvas.appendChild(element.element());
    }
}
