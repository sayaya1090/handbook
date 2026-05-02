package dev.sayaya.handbook.client.interfaces.api;
import dev.sayaya.rx.Observable;

public interface WorkspaceEventReceiver {
    Observable<String> observable();
}
