package com.racinggame.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.racinggame.core.GameConstants;
import com.racinggame.core.RacingGame;
import com.racinggame.core.systems.CameraController;
import com.racinggame.core.utils.TextDraw;
import com.racinggame.core.utils.UiButton;

/** 车库：选择赛车品牌（配色）与默认视角 */
public class GarageScreen extends ScreenAdapter {
    private final RacingGame game;
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer sr = new ShapeRenderer();

    private UiButton[] brandBtns = new UiButton[3];
    private UiButton btnChase, btnCockpit, btnBack;

    public GarageScreen(RacingGame game) {
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

        float bw = Math.min(w * 0.8f, 420f);
        float bh = 70f;
        float gap = 16f;
        float cx = w / 2f - bw / 2f;
        float y0 = h / 2f - 120f;

        for (int i = 0; i < 3; i++) {
            brandBtns[i] = new UiButton(cx, y0 + i * (bh + gap), bw, bh,
                    GameConstants.BRAND_NAMES[i], GameConstants.BRAND_COLORS[i].cpy().mul(0.9f));
        }
        float camW = bw / 2f - gap / 2f;
        btnChase = new UiButton(cx, y0 + 3 * (bh + gap), camW, bh, "追尾视角");
        btnCockpit = new UiButton(cx + camW + gap, y0 + 3 * (bh + gap), camW, bh, "车内视角");
        btnBack = new UiButton(24f, 24f, 120f, 48f, "返回",
                new Color(0.4f, 0.4f, 0.45f, 0.9f));

        handleInput();

        Gdx.gl.glClearColor(0.07f, 0.09f, 0.16f, 1f);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        batch.begin();
        game.bigFont.setColor(Color.WHITE);
        TextDraw.draw(batch, game.bigFont, "车库", cx - 20f, y0 - 60f);
        batch.end();

        for (int i = 0; i < 3; i++) brandBtns[i].draw(sr, batch, game.font, game.settings.brand == i);
        btnChase.draw(sr, batch, game.font, game.settings.cameraMode == CameraController.Mode.CHASE);
        btnCockpit.draw(sr, batch, game.font, game.settings.cameraMode == CameraController.Mode.COCKPIT);
        btnBack.draw(sr, batch, game.font, false);
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        float sh = Gdx.graphics.getHeight();
        game.audio.playClick();
        for (int i = 0; i < 3; i++) {
            if (brandBtns[i].rect.contains(x, y)) {
                game.settings.brand = i;
                game.settings.save();
                return;
            }
        }
        if (btnChase.rect.contains(x, y)) {
            game.settings.cameraMode = CameraController.Mode.CHASE;
            game.settings.save();
        } else if (btnCockpit.rect.contains(x, y)) {
            game.settings.cameraMode = CameraController.Mode.COCKPIT;
            game.settings.save();
        } else if (btnBack.rect.contains(x, y)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        sr.dispose();
    }
}
