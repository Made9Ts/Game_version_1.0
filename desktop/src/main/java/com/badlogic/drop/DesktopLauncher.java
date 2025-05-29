package com.badlogic.drop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Запускает игру на компьютере
 */
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Space Courier");
        config.setWindowedMode(720, 1080);
        new Lwjgl3Application(new SpaceCourierGame(), config);
    }
} 