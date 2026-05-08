package dev.sayaya.handbook.client.interfaces.controller;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 설정용 Speed Dial (Mode Toggle, Snap).
 */
@Singleton
public class SettingsDialElement extends SpeedDialElement {
    @Inject
    SettingsDialElement() {
        super("fa-gear", "settings-dial");
        css("settings");
    }
}
