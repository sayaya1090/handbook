package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.rx.Observable;

import java.util.List;

public interface MenuRepository {
    Observable<List<Menu>> findAll();
}
