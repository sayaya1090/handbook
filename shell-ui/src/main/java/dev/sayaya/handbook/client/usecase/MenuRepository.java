package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Menu;
import dev.sayaya.rx.Observable;

import java.util.List;

public interface MenuRepository {
    Observable<List<Menu>> findAll();
}
