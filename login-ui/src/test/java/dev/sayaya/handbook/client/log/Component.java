package dev.sayaya.handbook.client.log;

import dev.sayaya.handbook.client.interfaces.log.ConsoleElement;
import dev.sayaya.handbook.client.usecase.Log;

import javax.inject.Singleton;

@Singleton
@dagger.Component
public interface Component {
    Log log();
    ConsoleElement console();
}