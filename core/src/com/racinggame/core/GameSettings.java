package com.racinggame.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.racinggame.core.systems.CameraController;

/** 玩家选项：选车品牌与默认视角，持久化保存 */
public class GameSettings {
    private static final String PREF = "racinggame_settings";
    private static final String KEY_BRAND = "brand";
    private static final String KEY_COCKPIT = "cockpit";

    public int brand = 0; // 0 法拉利 / 1 保时捷 / 2 银箭
    public CameraController.Mode cameraMode = CameraController.Mode.CHASE;

    public GameSettings() {
        Preferences p = Gdx.app.getPreferences(PREF);
        brand = p.getInteger(KEY_BRAND, 0);
        cameraMode = p.getBoolean(KEY_COCKPIT, false) ? CameraController.Mode.COCKPIT : CameraController.Mode.CHASE;
    }

    public void save() {
        Preferences p = Gdx.app.getPreferences(PREF);
        p.putInteger(KEY_BRAND, brand);
        p.putBoolean(KEY_COCKPIT, cameraMode == CameraController.Mode.COCKPIT);
        p.flush();
    }
}
