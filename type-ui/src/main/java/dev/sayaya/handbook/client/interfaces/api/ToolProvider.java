package dev.sayaya.handbook.client.interfaces.api;
import dev.sayaya.handbook.domain.Tool;
import dev.sayaya.rx.Observable;
import java.util.List;

public interface ToolProvider {
    Observable<List<Tool>> observable();
    List<Tool> getValue();
}
