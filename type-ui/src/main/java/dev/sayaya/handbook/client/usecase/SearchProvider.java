package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

@Singleton
public class SearchProvider {
    @Delegate private final BehaviorSubject<Search> subject = behavior(Search.builder().build());
    @Inject SearchProvider() {}
}
