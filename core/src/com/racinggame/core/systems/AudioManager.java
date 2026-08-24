package com.racinggame.core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * 音效管理：引擎循环声、碰撞、点击、过关音效。
 * 资源均为程序化生成的 WAV（位于 android/assets/audio/）。
 * 提供静音开关与引擎随速度变调。
 */
public class AudioManager implements Disposable {
    private Sound engine, crash, click, win;
    private long engineId = -1;
    private boolean muted = false;
    private boolean loaded = false;

    public void load() {
        engine = Gdx.audio.newSound(Gdx.files.internal("audio/engine.wav"));
        crash = Gdx.audio.newSound(Gdx.files.internal("audio/crash.wav"));
        click = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));
        win = Gdx.audio.newSound(Gdx.files.internal("audio/win.wav"));
        loaded = true;
    }

    public void startEngine() {
        if (!loaded || muted || engineId != -1) return;
        engineId = engine.loop(0.4f);
    }

    public void stopEngine() {
        if (engineId != -1) {
            engine.stop(engineId);
            engineId = -1;
        }
    }

    /** 根据速度比(0..1)调节引擎音高 */
    public void setEngineIntensity(float speedRatio) {
        if (engineId != -1) {
            engine.setPitch(engineId, 0.5f + speedRatio * 1.2f);
        }
    }

    public void playCrash() {
        if (!muted) crash.play(0.6f);
    }

    public void playClick() {
        if (!muted) click.play(0.5f);
    }

    public void playWin() {
        if (!muted) win.play(0.8f);
    }

    public void setMuted(boolean m) {
        muted = m;
        if (m) stopEngine();
    }

    public boolean isMuted() {
        return muted;
    }

    @Override
    public void dispose() {
        stopEngine();
        if (loaded) {
            engine.dispose();
            crash.dispose();
            click.dispose();
            win.dispose();
            loaded = false;
        }
    }
}
