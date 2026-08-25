package com.racinggame.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.racinggame.core.RacingGame;

/** 桌面启动器（LWJGL3），用于本地冒烟测试，资源根指向 android/assets */
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("极速赛车 (Desktop)");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.setSamples(4); // 桌面端 MSAA 4x 抗锯齿
        new Lwjgl3Application(new RacingGame(), config);
    }
}
