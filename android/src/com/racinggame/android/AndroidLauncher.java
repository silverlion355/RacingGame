package com.racinggame.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.racinggame.core.RacingGame;

/** Android 启动器：初始化 libGDX 游戏并进入全屏横屏 */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true; // 隐藏系统栏，沉浸体验
        config.useWakelock = true;      // 比赛时保持屏幕常亮
        config.numSamples = 4;          // 开启 MSAA 4x 抗锯齿，消除几何边缘锯齿
        initialize(new RacingGame(), config);
    }
}
