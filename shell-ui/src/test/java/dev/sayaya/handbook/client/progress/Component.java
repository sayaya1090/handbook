package dev.sayaya.handbook.client.progress;

import dev.sayaya.handbook.client.interfaces.ProgressElement;
import dev.sayaya.handbook.domain.Progress;
import dev.sayaya.rx.Observer;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { ProgressMock.class })
public interface Component {
    ProgressElement progressElement();
    Observer<Progress> progressObserver();
}
