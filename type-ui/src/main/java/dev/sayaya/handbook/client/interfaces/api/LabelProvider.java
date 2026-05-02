package dev.sayaya.handbook.client.interfaces.api;
import dev.sayaya.handbook.domain.Labels;
import dev.sayaya.rx.Observable;

public interface LabelProvider {
    Observable<Labels> observable();
    Labels getValue();
    void subscribe(java.util.function.Consumer<Labels> consumer);
}
