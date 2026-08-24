package com.racinggame.core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * 积分与设置持久化（Android 即 SharedPreferences）。
 * 累计总分随关卡结算入账；同时复用同一 Preferences 保存静音开关。
 */
public class ScoreManager {
    private static final String PREF_NAME = "racinggame_prefs";
    private static final String KEY_TOTAL = "total_score";
    private static final String KEY_MUTE = "muted";

    private final Preferences prefs;

    public ScoreManager() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
    }

    public int getTotalScore() {
        return prefs.getInteger(KEY_TOTAL, 0);
    }

    /** 结算时累加积分并落盘 */
    public void addScore(int points) {
        prefs.putInteger(KEY_TOTAL, getTotalScore() + points);
        prefs.flush();
    }

    public boolean isMuted() {
        return prefs.getBoolean(KEY_MUTE, false);
    }

    public void setMuted(boolean muted) {
        prefs.putBoolean(KEY_MUTE, muted);
        prefs.flush();
    }
}
