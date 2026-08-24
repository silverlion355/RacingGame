package com.racinggame.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.racinggame.core.RacingGame;
import com.racinggame.core.levels.LevelConfig;
import com.racinggame.core.utils.TextDraw;
import com.racinggame.core.utils.UiButton;

/** 结算界面：展示排名、获得积分、累计总分，并提供下一关/重玩/返回 */
public class ResultScreen extends ScreenAdapter {
    private final RacingGame game;
    private final int levelId;
    private final int rank;
    private final int points;
    private final float time;

    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer sr = new ShapeRenderer();

    private UiButton btnNext, btnRetry, btnMenu;

    public ResultScreen(RacingGame game, int levelId, int rank, int points, float time) {
        this.game = game;
        this.levelId = levelId;
        this.rank = rank;
        this.points = points;
        this.time = time;
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

        float bw = Math.min(w * 0.6f, 320f);
        float bh = 60f;
        float gap = 16f;
        float cx = w / 2f - bw / 2f;
        float y = h / 2f + 10f;

        boolean hasNext = LevelConfig.get(levelId).id < LevelConfig.count();
        btnNext = new UiButton(cx, y, bw, bh, hasNext ? "下一关" : "已是最后一关");
        btnRetry = new UiButton(cx, y + bh + gap, bw, bh, "重玩本关");
        btnMenu = new UiButton(cx, y + 2 * (bh + gap), bw, bh, "返回菜单");

        handleInput(hasNext);

        Gdx.gl.glClearColor(0.07f, 0.09f, 0.16f, 1f);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        batch.begin();
        game.bigFont.setColor(Color.WHITE);
        TextDraw.draw(batch, game.bigFont, "比赛结束", cx - 30f, y - 110f);
        game.font.setColor(new Color(1f, 0.85f, 0.3f, 1f));
        TextDraw.draw(batch, game.font, "最终排名: 第 " + rank + " 名", cx - 10f, y - 60f);
        game.font.setColor(Color.WHITE);
        TextDraw.draw(batch, game.font, "获得积分: +" + points, cx - 10f, y - 30f);
        TextDraw.draw(batch, game.font, "累计总分: " + game.score.getTotalScore(),
                cx - 10f, y - 4f);
        batch.end();

        btnNext.draw(sr, batch, game.font, false);
        btnRetry.draw(sr, batch, game.font, false);
        btnMenu.draw(sr, batch, game.font, false);
    }

    private void handleInput(boolean hasNext) {
        if (!Gdx.input.justTouched()) return;
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        float sh = Gdx.graphics.getHeight();
        game.audio.playClick();
        if (hasNext && btnNext.rect.contains(x, y)) {
            game.setScreen(new GameScreen(game, levelId + 1));
        } else if (btnRetry.rect.contains(x, y)) {
            game.setScreen(new GameScreen(game, levelId));
        } else if (btnMenu.rect.contains(x, y)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        sr.dispose();
    }
}
