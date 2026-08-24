# 极速赛车 (RacingGame)

一款基于 **libGDX 1.12.x** 的 Android 3D 赛车小游戏（相对 3D：车内第一视角 / 车外追尾视角可切换）。
全部美术与音频均由代码或脚本程序化生成，**无需任何联网下载的第三方资源**。

## 特性

- 程序化低多边形 3D 赛道与赛车（按品牌配色近似名车：红=法拉利、黄=保时捷、银=其它）。
- 触控操控：屏幕左侧左右转向，右侧「油门」「刹车/倒车」按钮（支持多点触控同时操作）。
- 3 个递增难度关卡（更长赛道 / 更多弯道 / 更快对手），按排名获得积分（第1=100、第2=60、第3=30、完赛=10）。
- 积分通过 `Preferences`（Android = `SharedPreferences`）持久化累计。
- 音效：循环引擎声、碰撞声、点击声、过关音效，均由 `tools/gen_audio.py` 生成 WAV。
- 完整可玩闭环：主菜单 → 选关 → 比赛 → 结算 → 返回。

## 工程结构

```
RacingGame/
├── settings.gradle / build.gradle / gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/      # Gradle 8.5 包装
├── .github/workflows/build.yml                  # GitHub Actions 构建 APK
├── tools/gen_audio.py                           # 程序化生成 WAV 脚本
├── core/        # 共享游戏逻辑（实体/系统/界面/关卡）
├── android/     # Android 发布目标（含 assets/audio/*.wav）
└── desktop/     # LWJGL 桌面冒烟测试
```

## 环境要求

- **Android 构建**：JDK 17 + Android SDK（compileSdk/targetSdk 34，minSdk 21）。
- **桌面运行**：JDK 17 +（LWJGL 原生库由依赖自动提供）。
- **音效生成**：Python 3（标准库即可）。

## 本地运行 / 构建

### 1) 生成音效（首次或清理后）
```bash
python3 tools/gen_audio.py android/assets/audio
```

### 2) 桌面冒烟测试（需本机有 JDK17 与图形环境）
```bash
./gradlew desktop:run
```

### 3) 构建 Android APK
```bash
./gradlew android:assembleDebug
# 产物: android/build/outputs/apk/debug/android-debug.apk
```
> 也可直接 `./gradlew assembleDebug` 构建全部模块。

### 4) 发布到手机
将 `android-debug.apk` 拷贝到手机安装，或在 Android Studio 打开本工程后运行到设备。

## GitHub Actions 自动构建

推送到 `main`/`master` 或发起 PR 即触发：
1. `checkout` 代码
2. `setup-java@17`（Temurin）
3. `android-actions/setup-android` 并 `sdkmanager --licenses` 接受协议、安装 `platform-34` 与 `build-tools;34.0.0`
4. 运行 Python 脚本生成音效
5. `./gradlew assembleDebug`
6. 上传 `android/build/outputs/apk/**/*.apk` 为 Artifact（名为 `racing-game-apk`）

下载 Artifact 即可获得可安装的调试 APK。

## 代码分包

- `core/screens/`：`MainMenuScreen`、`LevelSelectScreen`、`GameScreen`、`ResultScreen`、`GarageScreen`
- `core/entities/`：`PlayerCar`、`AICar`、`Track`（路点/边界/网格）、`CarFactory`（品牌配色车体）
- `core/systems/`：`TouchInputController`、`RaceManager`、`ScoreManager`、`AudioManager`、`CameraController`
- `core/levels/`：`LevelDefinition` + `LevelConfig`（3 关参数）
- `core/utils/`：`MathUtil`、`UiButton`、`Rect`、`FontManager`
- `GameConstants`：集中常量（视角、物理、配色、积分规则）

## 已知限制与后续升级

- 赛车为程序化低多边形，可替换为真实 glTF/obj 模型（保留 `CarFactory` 接口即可）。
- 当前为街机式简化物理（非 Bullet/Box2D），可升级为真实物理。
- 阴影使用基础方向光，可加入 `ModelBatch` 阴影或 PCF 优化。
- 关卡目前 3 个，扩展只需在 `LevelConfig.LEVELS` 增加 `LevelDefinition`。
- 中文 UI 依赖系统 CJK 字体（Android 自带）；若目标设备无 CJK 字体，会回退为默认字体（ASCII）。
