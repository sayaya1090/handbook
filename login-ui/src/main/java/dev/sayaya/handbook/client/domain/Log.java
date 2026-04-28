package dev.sayaya.handbook.domain;

import dev.sayaya.rx.subject.ReplaySubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.ReplaySubject.replayWithBuffer;

@Singleton
public class Log {
    @Delegate private final ReplaySubject<String> subject = replayWithBuffer(String.class, 50);
    @Inject Log() {}
}
