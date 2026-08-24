package com.racinggame.core.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.AmbientLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.racinggame.core.GameConstants;
import com.racinggame.core.RacingGame;
import com.racinggame.core.entities.AICar;
import com.racinggame.core.entities.CarFactory;
import com.racinggame.core.entities.PlayerCar;
import com.racinggame.core.entities.Track;
import com.racinggame.core.levels.LevelConfig;
import com.racinggame.core.levels.LevelDefinition;
import com.racinggame.core.systems.CameraController;
import com.racinggame.core.systems.RaceManager;
import com.racinggame.core.systems.TouchInputController;
import com.racinggame.core.utils.Rect;
import com.racinggame.core.utils.TextDraw;
import com.racinggame.core.utils.UiButton;

import java.util.ArrayList;
import java.util.List;

/** 核心 3D 比赛界面：赛道/车辆渲染、触控操控、HUD、圈数排名、暂停 */
public class GameScreen extends ScreenAdapter {
    private final RacingGame game;
    private final int levelId;

    private Track track;
    private PlayerCar player;
    private final List<AICar> aiCars = new ArrayList<>();
    private final List<Model> carModels = new ArrayList<>();
    private RaceManager raceManager;

    private PerspectiveCamera cam;
    private final ModelBatch modelBatch = new ModelBatch();
    private Environment environment;
    private CameraController cameraController;
    private final TouchInputController touch = new TouchInputController();

    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final ShapeRenderer sr = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();

    private boolean paused = false;
    private boolean transitioned = false;

    // 暂停遮罩按钮
    private UiButton pauseResume, pauseQuit;

    public GameScreen(RacingGame game, int levelId) {
        this.game = game;
        this.levelId = levelId;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        LevelDefinition lv = LevelConfig.get(levelId);

        track = new Track(lv);

        // 玩家车
        player = new PlayerCar(game.settings.brand, lv.playerMaxSpeed);
        player.position.set(track.getStartPosition(0));
        player.heading = track.getStartHeading();
        attachModel(player, GameConstants.BRAND_COLORS[game.settings.brand]);

        // 对手车
        for (int k = 0; k < lv.opponentCount; k++) {
            int brand = (k + 1) % 3;
            AICar ai = new AICar(brand, lv.opponentSpeed + k * 0.6f);
            ai.targetWaypoint = 3;
            ai.position.set(track.getStartPosition(k + 1));
            ai.heading = track.getStartHeading();
            attachModel(ai, GameConstants.BRAND_COLORS[brand]);
            aiCars.add(ai);
        }

        raceManager = new RaceManager(track, player, aiCars, lv.laps);

        // 摄像机
        cam = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.1f;
        cam.far = 2000f;
        Vector2 f = player.forward();
        cam.position.set(player.position.x - f.x * GameConstants.CHASE_DIST,
                GameConstants.CHASE_HEIGHT, player.position.y - f.y * GameConstants.CHASE_DIST);
        cam.lookAt(player.position.x, 1f, player.position.y);
        cam.up.set(0, 1, 0);
        cam.update();
        cameraController = new CameraController(cam, game.settings.cameraMode);

        // 光照环境
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.55f, 0.55f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.9f, 0.9f, 0.85f, -0.5f, -1f, -0.35f));

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        game.audio.startEngine();
    }

    private void attachModel(com.racinggame.core.entities.Car car, Color color) {
        Model m = CarFactory.buildCarModel(color);
        carModels.add(m);
        car.instance = new com.badlogic.gdx.graphics.g3d.ModelInstance(m);
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
        uiCam.setToOrtho(true, width, height);
    }

    @Override
    public void render(float delta) {
        // 1) 输入与物理
        touch.update();
        if (touch.cameraEdge) {
            cameraController.toggle();
            game.audio.playClick();
        }
        if (touch.pauseEdge) {
            paused = !paused;
            game.audio.playClick();
        }

        if (!paused && !transitioned) {
            raceManager.update(delta, touch.steer, touch.throttle ? 1 : 0, touch.brake ? 1 : 0);
            game.audio.setEngineIntensity(Math.min(1f, Math.abs(player.speed) / player.maxSpeed));
            if (raceManager.playerHitThisFrame) game.audio.playCrash();
            cameraController.update(player);
        }

        // 2) 完赛 → 结算
        if (raceManager.raceOver && !transitioned) {
            finishRace();
            return;
        }

        // 3) 渲染 3D
        Gdx.gl.glClearColor(0.45f, 0.62f, 0.85f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        modelBatch.begin(cam);
        modelBatch.render(track.instance, environment);
        modelBatch.render(player.instance, environment);
        for (AICar ai : aiCars) modelBatch.render(ai.instance, environment);
        modelBatch.end();

        // 4) 渲染 HUD / 触控按钮
        drawHud();

        // 5) 暂停遮罩
        if (paused) drawPauseOverlay();
    }

    private void drawHud() {
        sr.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        // 触控按钮
        touch.btnLeft.draw(sr, batch, game.font, touch.steer == -1);
        touch.btnRight.draw(sr, batch, game.font, touch.steer == 1);
        touch.btnThrottle.draw(sr, batch, game.font, touch.throttle);
        touch.btnBrake.draw(sr, batch, game.font, touch.brake);
        drawSmallButton(touch.rectCamera, "视角");
        drawSmallButton(touch.rectPause, "暂停");

        // 信息文本
        LevelDefinition lv = LevelConfig.get(levelId);
        int rank = raceManager.getPlayerRank();
        int total = aiCars.size() + 1;
        int lap = Math.min(player.lapsCompleted + 1, lv.laps);
        int t = (int) raceManager.raceTime;
        String time = String.format("%02d:%02d", t / 60, t % 60);

        int top = 96;
        batch.begin();
        game.font.setColor(Color.WHITE);
        TextDraw.draw(batch, game.font, lv.name, w / 2f - 40f, top);
        TextDraw.draw(batch, game.font,
                "排名 " + rank + "/" + total + "   圈 " + lap + "/" + lv.laps + "   时间 " + time,
                w / 2f - 130f, top + 34f);
        TextDraw.draw(batch, game.font,
                "速度 " + (int) (Math.abs(player.speed) * 3.6f) + " km/h",
                24, h / 2f);
        batch.end();
    }

    private void drawSmallButton(Rect r, String label) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0.2f, 0.4f, 0.7f, 0.9f));
        sr.rect(r.x, r.y, r.w, r.h);
        sr.end();
        batch.begin();
        game.font.setColor(Color.WHITE);
        TextDraw.draw(batch, game.font, label, r.x + r.w / 2f - 24f, r.y + r.h / 2f + 8f);
        batch.end();
    }

    private void drawPauseOverlay() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        sr.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.6f);
        sr.rect(0, 0, w, h);
        sr.end();

        float bw = Math.min(w * 0.6f, 320f);
        float bh = 64f;
        float cx = w / 2f - bw / 2f;
        float y = h / 2f - bh;
        pauseResume = new UiButton(cx, y, bw, bh, "继续");
        pauseQuit = new UiButton(cx, y + bh + 16f, bw, bh, "退出到菜单");

        pauseResume.draw(sr, batch, game.font, false);
        pauseQuit.draw(sr, batch, game.font, false);

        if (Gdx.input.justTouched()) {
            int x = Gdx.input.getX();
            int y2 = Gdx.input.getY();
            float sh = Gdx.graphics.getHeight();
            game.audio.playClick();
            if (pauseResume.rect.contains(x, y2)) {
                paused = false;
            } else if (pauseQuit.rect.contains(x, y2)) {
                game.audio.stopEngine();
                disposeRace();
                game.setScreen(new MainMenuScreen(game));
            }
        }
    }

    private void finishRace() {
        transitioned = true;
        int rank = raceManager.getPlayerRank();
        int points = GameConstants.scoreForRank(rank);
        game.score.addScore(points);
        game.audio.stopEngine();
        game.audio.playWin();
        disposeRace();
        game.setScreen(new ResultScreen(game, levelId, rank, points, raceManager.raceTime));
    }

    private void disposeRace() {
        if (track != null) track.dispose();
        for (Model m : carModels) m.dispose();
        carModels.clear();
    }

    @Override
    public void dispose() {
        disposeRace();
        modelBatch.dispose();
        sr.dispose();
        batch.dispose();
    }
}
