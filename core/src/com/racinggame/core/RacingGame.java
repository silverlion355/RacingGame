package com.racinggame.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.racinggame.core.screens.MainMenuScreen;
import com.racinggame.core.systems.AudioManager;
import com.racinggame.core.systems.ScoreManager;
import com.racinggame.core.utils.FontManager;

/**
 * 游戏入口（继承 Game），负责全局管理：音效、积分、设置、字体与界面切换。
 */
public class RacingGame extends Game {
    public AudioManager audio;
    public ScoreManager score;
    public GameSettings settings;
    public BitmapFont font;     // 常规字号
    public BitmapFont bigFont;  // 标题字号

    @Override
    public void create() {
        audio = new AudioManager();
        audio.load();

        score = new ScoreManager();
        settings = new GameSettings();
        audio.setMuted(score.isMuted());

        font = FontManager.createFont(26);
        bigFont = FontManager.createFont(42);

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        if (audio != null) audio.dispose();
        if (font != null) font.dispose();
        if (bigFont != null) bigFont.dispose();
        super.dispose();
    }
}
