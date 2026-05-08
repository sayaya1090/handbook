package dev.sayaya.handbook.client.interfaces.controller;

import dev.sayaya.handbook.client.components.ActionManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 액션용 Speed Dial (Undo, Redo, Save, Reload).
 */
@Singleton
public class ActionDialElement extends SpeedDialElement {
    @Inject
    ActionDialElement() {
        super("fa-bolt", "action-dial");
    }
}
