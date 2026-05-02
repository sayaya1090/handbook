package dev.sayaya.handbook.client.interfaces.api;
import dev.sayaya.rx.Observable;

public interface MutationReceiver {
    Observable<String> observable();
}
