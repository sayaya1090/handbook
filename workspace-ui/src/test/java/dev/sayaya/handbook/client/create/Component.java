package dev.sayaya.handbook.client.create;

import dev.sayaya.handbook.client.interfaces.create.ContentElement;

import javax.inject.Singleton;

@Singleton
@dagger.Component
public interface Component {
    ContentElement contentElement();
}