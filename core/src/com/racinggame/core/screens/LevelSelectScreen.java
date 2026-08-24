package com.racinggame.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.racinggame.core.RacingGame;
import com.racinggame.core.levels.LevelConfig;
import com.racinggame.core.levels.LevelDefinition;
import com.racinggame.core.utils.TextDraw;
import com.racinggame.core.utils.UiButton;

/** 选关：展示 3 个递增难度关卡，点击进入比赛 */
public class LevelSelectScreen extends ScreenAdapter {
    private final RacingGame game;
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer sr = new ShapeRenderer();

    private UiButton[] levelBtns = new UiButton[LevelConfig.count()];
    private UiButton btnBack;

    public LevelSelectScreen(RacingGame game) {
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

        float cardW = Math.min(w * 0.8f, 520f);
        float cardH = 88f;
        float gap = 16f;
        float cx = w / 2f - cardW / 2f;
        float y0 = h / 2f - (cardH * levelBtns.length + gap * (levelBtns.length - 1)) / 2f + 20f;

        for (int i = 0; i < levelBtns.length; i++) {
            LevelDefinition lv = LevelConfig.LEVELS.get(i);
            levelBtns[i] = new UiButton(cx, y0 + i * (cardH + gap), cardW, cardH,
                    (i + 1) + ". " + lv.name + "  (" + lv.laps + "圈/" + lv.opponentCount + "对手)");
        }
        btnBack = new UiButton(24f, 24f, 120f, 48f, "返回",
                new Color(0.4f, 0.4f, 0.45f, 0.9f));

        handleInput();

        Gdx.gl.glClearColor(0.07f, 0.09f, 0.16f, 1f);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        batch.begin();
        game.bigFont.setColor(Color.WHITE);
        TextDraw.draw(batch, game.bigFont, "选择关卡", cx - 20f, y0 - 70f);
        game.font.setColor(new Color(0.7f, 0.8f, 1f, 1f));
        TextDraw.draw(batch, game.font, "累计总分: " + game.score.getTotalScore(), cx - 10f, y0 - 34f);
        // 关卡难度描述
        for (int i = 0; i < levelBtns.length; i++) {
            LevelDefinition lv = LevelConfig.LEVELS.get(i);
            game.font.setColor(new Color(0.85f, 0.85f, 0.9f, 1f));
            TextDraw.draw(batch, game.font, lv.desc, cx + 12f, y0 + i * (cardH + gap) + cardH - 16f);
        }
        batch.end();

        for (UiButton b : levelBtns) b.draw(sr, batch, game.font, false);
        btnBack.draw(sr, batch, game.font, false);
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        float sh = Gdx.graphics.getHeight();
        game.audio.playClick();
        for (int i = 0; i < levelBtns.length; i++) {
            if (levelBtns[i].rect.contains(x, y)) {
                game.setScreen(new GameScreen(game, LevelConfig.LEVELS.get(i).id));
                return;
            }
        }
        if (btnBack.rect.contains(x, y)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        sr.dispose();
    }
}
