package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Page;
import dev.sayaya.handbook.client.domain.Search;
import dev.sayaya.handbook.client.domain.Type;
import dev.sayaya.rx.Observable;

public interface TypeRepository {
    Observable<Page<Type>> search(Search param);
}
