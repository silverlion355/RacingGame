package com.racinggame.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.racinggame.core.RacingGame;
import com.racinggame.core.utils.TextDraw;
import com.racinggame.core.utils.UiButton;

/** 主菜单：开始游戏 / 车库 / 静音切换，并展示累计总分 */
public class MainMenuScreen extends ScreenAdapter {
    private final RacingGame game;
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer sr = new ShapeRenderer();

    private UiButton btnStart, btnGarage, btnMute;

    public MainMenuScreen(RacingGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        uiCam.setToOrtho(true, width, height);
    }

    @Override
    public void render(float delta) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        float bw = Math.min(w * 0.6f, 360f);
        float bh = 64f;
        float gap = 18f;
        float cx = w / 2f - bw / 2f;
        float totalH = bh * 2 + gap;
        float y0 = h / 2f - totalH / 2f + 40f;

        btnStart = new UiButton(cx, y0, bw, bh, "开始游戏");
        btnGarage = new UiButton(cx, y0 + bh + gap, bw, bh, "车库 / 选车");
        btnMute = new UiButton(w - 170f, 24f, 150f, 48f,
                game.audio.isMuted() ? "音效:关" : "音效:开",
                new Color(0.4f, 0.4f, 0.45f, 0.9f));

        handleInput();

        Gdx.gl.glClearColor(0.07f, 0.09f, 0.16f, 1f);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        // 标题
        batch.begin();
        game.bigFont.setColor(Color.WHITE);
        TextDraw.draw(batch, game.bigFont, "极速赛车", cx - 40f, y0 - 90f);
        game.font.setColor(new Color(0.7f, 0.8f, 1f, 1f));
        TextDraw.draw(batch, game.font, "累计总分: " + game.score.getTotalScore(), cx - 10f, y0 - 50f);
        batch.end();

        btnStart.draw(sr, batch, game.font, false);
        btnGarage.draw(sr, batch, game.font, false);
        btnMute.draw(sr, batch, game.font, false);
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        float sh = Gdx.graphics.getHeight();
        game.audio.playClick();
        if (btnStart.rect.contains(x, y)) {
            game.setScreen(new LevelSelectScreen(game));
        } else if (btnGarage.rect.contains(x, y)) {
            game.setScreen(new GarageScreen(game));
        } else if (btnMute.rect.contains(x, y)) {
            boolean m = !game.audio.isMuted();
            game.audio.setMuted(m);
            game.score.setMuted(m);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        sr.dispose();
    }
}
