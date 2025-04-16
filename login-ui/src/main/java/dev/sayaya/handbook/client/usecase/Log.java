package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.ReplaySubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.ReplaySubject.replay;

@Singleton
public class Log {
    @Delegate private final ReplaySubject<String> _this = replay(String.class);
    @Inject Log() {}
}
