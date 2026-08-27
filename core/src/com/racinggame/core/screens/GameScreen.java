package com.racinggame.core.screens;

import com.badlogic.gdx.Application;
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
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
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
import java.util.ArrayList;
import java.util.List;
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
    private Model wheelModel;
    private SceneManager sceneManager;                  // PBR + IBL + 软阴影 渲染管线
    private DirectionalShadowLight shadowLight;          // gdx-gltf 软阴影光（含 PBR 阴影）
    private Cubemap envCubemap;                         // IBL 反射/高光图
    private Cubemap irrCubemap;                         // IBL 漫反射图
    private SceneSkybox skybox;
    private final Vector3 shadowCenter = new Vector3();
    private Model gltfCarModel = null;                  // 真实 glTF 车模型（共享；null 则回退基础车模）
    private static final float GLTF_CAR_SCALE = 0.82f;  // CesiumMilkTruck.glb 车长约4.87，缩放到游戏单位(车长约4)
    private static final float GLTF_CAR_YAW = 0f;       // 若真机发现车头朝后/侧，改为 (float)Math.PI 或 Math.PI/2
    private Scene trackScene;                            // 赛道场景
    private final com.badlogic.gdx.utils.Array<Scene> carScenes = new com.badlogic.gdx.utils.Array<>();
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
        wheelModel = CarFactory.buildWheelModel();

        // 尝试加载真实 glTF 车模型；失败则回退基础车模（带圆柱车轮）
        try {
            gltfCarModel = CarFactory.loadGltfCarModel("car.glb");
        } catch (Throwable t) {
            gltfCarModel = null;
        }

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

        // PBR + IBL + 软阴影渲染管线（gdx-gltf SceneManager）
        sceneManager = new SceneManager();
        sceneManager.setCamera(cam);

        int shadowRes = (Gdx.app.getType() == Application.ApplicationType.Android) ? 1024 : 2048;
        shadowLight = new DirectionalShadowLight(shadowRes, shadowRes);
        shadowLight.setViewport(60f, 60f, 1f, 250f);
        shadowLight.set(1f, 0.97f, 0.9f, -0.5f, -1f, -0.35f);
        shadowLight.direction.nor();
        sceneManager.environment.add(shadowLight);
        sceneManager.setAmbientLight(0.35f);

        // IBL 环境光：烘焙反射/漫反射 cubemap，使 PBR 材质（车身）呈现真实金属反光
        IBLBuilder ibl = IBLBuilder.createOutdoor(shadowLight);
        envCubemap = ibl.buildEnvMap(256);
        irrCubemap = ibl.buildIrradianceMap(128);
        sceneManager.environment.set(new CubemapAttribute(CubemapAttribute.Diffuse, irrCubemap));
        sceneManager.environment.set(new CubemapAttribute(CubemapAttribute.Specular, envCubemap));
        ibl.dispose();

        // 天空盒（复用 IBL 环境图，保证反射与天空一致）
        skybox = new SceneSkybox(envCubemap);
        sceneManager.setSkyBox(skybox);

        // 赛道场景
        trackScene = new Scene(track.instance);
        sceneManager.addScene(trackScene);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        game.audio.startEngine();
    }

    private void attachModel(com.racinggame.core.entities.Car car, Color color) {
        if (gltfCarModel != null) {
            // glTF 真实车模：整体替换车体，自动检测模型内 wheel 节点用于滚动
            car.instance = new com.badlogic.gdx.graphics.g3d.ModelInstance(gltfCarModel);
            car.modelScale = GLTF_CAR_SCALE;
            car.modelYaw = GLTF_CAR_YAW;
            car.wheels = null; // 不再使用独立圆柱车轮（glTF 模型自带车轮）
            // 收集模型内独立轮子节点，捕获基础旋转用于滚动
            car.wheelNodes = collectWheelNodes(car.instance.nodes);
            if (car.wheelNodes != null && car.wheelNodes.size > 0) {
                car.wheelBase = new com.badlogic.gdx.math.Quaternion[car.wheelNodes.size];
                for (int i = 0; i < car.wheelNodes.size; i++) {
                    car.wheelBase[i] = new com.badlogic.gdx.math.Quaternion(car.wheelNodes.get(i).rotation);
                }
            }
            // 注册为 SceneManager 场景（PBR+IBL 渲染）
            Scene s = new Scene(car.instance);
            sceneManager.addScene(s);
            carScenes.add(s);
        } else {
            // 基础车模 fallback：程序化车体 + 独立圆柱车轮
            Model m = CarFactory.buildCarModel(color);
            carModels.add(m);
            car.instance = new com.badlogic.gdx.graphics.g3d.ModelInstance(m);
            Scene body = new Scene(car.instance);
            sceneManager.addScene(body);
            carScenes.add(body);
            car.wheels = new com.badlogic.gdx.graphics.g3d.ModelInstance[4];
            for (int i = 0; i < 4; i++) {
                car.wheels[i] = new com.badlogic.gdx.graphics.g3d.ModelInstance(wheelModel);
                Scene ws = new Scene(car.wheels[i]);
                sceneManager.addScene(ws);
                carScenes.add(ws);
            }
        }
    }

    /** 递归收集名字含 wheel 的节点（glTF 模型若含独立轮子节点则用于滚动；圆柱体车轮模型无则空） */
    private Array<Node> collectWheelNodes(java.lang.Iterable<Node> nodes) {
        Array<Node> out = new Array<>();
        for (Node n : nodes) {
            if (n.id != null && n.id.toLowerCase().contains("wheel")) out.add(n);
            java.lang.Iterable<Node> kids = n.getChildren();
            if (kids != null) out.addAll(collectWheelNodes(kids));
        }
        return out;
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
        if (sceneManager != null) sceneManager.updateViewport(width, height);
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
            cameraController.update(player, Math.min(1f, Math.abs(player.speed) / player.maxSpeed));
        }

        // 2) 完赛 → 结算
        if (raceManager.raceOver && !transitioned) {
            finishRace();
            return;
        }

        // 3) 渲染 3D（PBR + IBL + 软阴影，gdx-gltf SceneManager 自动处理）
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        // 阴影中心跟随玩家
        if (shadowLight != null) shadowLight.setCenter(player.position.x, 0f, player.position.y);
        sceneManager.update(delta);
        sceneManager.render();

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
        TextDraw.draw(batch, game.font, "FPS " + Gdx.graphics.getFramesPerSecond(), w - 130f, 40f);
        batch.end();

        drawMinimap();
    }

    private void drawMinimap() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float mmW = 150f, mmH = 150f;
        float mmX = w - mmW - 20f, mmY = h - mmH - 20f;
        int wp = track.getWaypointCount();
        if (wp < 2) return;
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < wp; i++) {
            Vector2 p = track.getWaypoint(i);
            minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
        }
        float pad = 14f;
        float sw = (mmW - 2 * pad) / Math.max(1f, (maxX - minX));
        float sh = (mmH - 2 * pad) / Math.max(1f, (maxY - minY));
        float s = Math.min(sw, sh);
        float ox = mmX + pad + ((mmW - 2 * pad) - (maxX - minX) * s) / 2f;
        float oy = mmY + pad + ((mmH - 2 * pad) - (maxY - minY) * s) / 2f;

        sr.setProjectionMatrix(uiCam.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.45f);
        sr.rect(mmX, mmY, mmW, mmH);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.92f, 0.92f, 0.92f, 0.9f);
        for (int i = 0; i < wp; i++) {
            Vector2 p = track.getWaypoint(i);
            Vector2 q = track.getWaypoint((i + 1) % wp);
            float x1 = ox + (p.x - minX) * s, y1 = oy + (p.y - minY) * s;
            float x2 = ox + (q.x - minX) * s, y2 = oy + (q.y - minY) * s;
            sr.rectLine(x1, y1, x2, y2, 3f);
        }
        sr.setColor(0.2f, 0.6f, 1f, 1f);
        for (AICar ai : aiCars) {
            sr.circle(ox + (ai.position.x - minX) * s, oy + (ai.position.y - minY) * s, 4f);
        }
        sr.setColor(1f, 0.2f, 0.2f, 1f);
        sr.circle(ox + (player.position.x - minX) * s, oy + (player.position.y - minY) * s, 5f);
        sr.end();
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
        if (sceneManager != null) {
            for (Scene s : carScenes) sceneManager.removeScene(s);
            carScenes.clear();
            if (trackScene != null) { sceneManager.removeScene(trackScene); trackScene = null; }
        }
        if (track != null) track.dispose();
        for (Model m : carModels) m.dispose();
        carModels.clear();
        if (gltfCarModel != null) { gltfCarModel.dispose(); gltfCarModel = null; }
    }

    @Override
    public void dispose() {
        disposeRace();
        if (wheelModel != null) wheelModel.dispose();
        if (shadowLight != null) shadowLight.dispose();
        if (skybox != null) skybox.dispose();
        if (envCubemap != null) envCubemap.dispose();
        if (irrCubemap != null) irrCubemap.dispose();
        if (sceneManager != null) sceneManager.dispose();
        sr.dispose();
        batch.dispose();
    }
}
