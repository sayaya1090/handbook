package dev.sayaya.handbook.client.usecase;

import dev.sayaya.rx.subject.BehaviorSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 애플리케이션의 현재 URI(URL Path) 상태를 관리하는 Store.
 * 
 * <p><b>역할:</b> 브라우저 주소창이나 에이전트 내비게이션에 의해 변경되는 
 * 시스템 전체의 URI 상태를 단일 출처(SSOT)로서 유지한다.</p>
 * 
 * <p><b>구현:</b> {@link BehaviorSubject}를 상속하여 Observable(읽기)과 
 * Observer(쓰기) 인터페이스를 모두 제공한다. 초기값은 null 이다.</p>
 */
@Singleton
public class UriStore extends BehaviorSubject<String> {
    @Inject
    public UriStore() {
        super(null);
    }
}
