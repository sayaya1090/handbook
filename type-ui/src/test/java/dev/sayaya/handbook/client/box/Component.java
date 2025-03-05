package dev.sayaya.handbook.client.box;

import dev.sayaya.handbook.client.usecase.BoxElementList;
import dev.sayaya.handbook.client.usecase.BoxList;

import javax.inject.Singleton;

@Singleton
@dagger.Component
public interface Component {
    BoxList boxList();
    BoxElementList boxElementList();
}
