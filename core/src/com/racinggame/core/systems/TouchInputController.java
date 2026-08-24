package com.racinggame.core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.racinggame.core.utils.Rect;
import com.racinggame.core.utils.UiButton;

/**
 * 触控操控：左侧左右转向，右侧油门/刹车（支持多点触控同时按下）。
 * 同时提供顶部的「视角切换」与「暂停」边沿触发按钮。
 * 坐标统一为左上角原点（与触摸 y 轴一致）。
 */
public class TouchInputController {
    // 渲染用按钮（GameScreen 读取其 rect 进行绘制）
    public UiButton btnLeft, btnRight, btnThrottle, btnBrake;
    public Rect rectCamera, rectPause;

    // 本帧操控结果
    public float steer = 0;     // -1 左 / +1 右 / 0
    public boolean throttle = false;
    public boolean brake = false;
    public boolean cameraEdge = false; // 视角按钮本帧是否刚按下
    public boolean pauseEdge = false;  // 暂停按钮本帧是否刚按下

    private final boolean[] prevDown = new boolean[10];

    public void update() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        layout(w, h);

        steer = 0;
        throttle = false;
        brake = false;
        boolean cam = false, pau = false;

        int ptrs = Math.min(10, Gdx.input.getMaxPointers());
        for (int i = 0; i < ptrs; i++) {
            boolean down = Gdx.input.isTouched(i);
            if (!down) {
                prevDown[i] = false;
                continue;
            }
            int x = Gdx.input.getX(i);
            int y = Gdx.input.getY(i);

            if (btnLeft.rect.contains(x, y)) steer = -1;
            if (btnRight.rect.contains(x, y)) steer = 1;
            if (btnThrottle.rect.contains(x, y)) throttle = true;
            if (btnBrake.rect.contains(x, y)) brake = true;
            if (rectCamera.contains(x, y) && !prevDown[i]) cam = true;
            if (rectPause.contains(x, y) && !prevDown[i]) pau = true;

            prevDown[i] = true;
        }
        cameraEdge = cam;
        pauseEdge = pau;
    }

    private void layout(int w, int h) {
        float size = Math.max(70, Math.min(140, Math.min(w, h) * 0.18f));
        float gap = size * 0.15f;
        float margin = size * 0.25f;
        float y = h - margin - size;

        btnLeft = new UiButton(margin, y, size, size, "◀ 左", new Color(0.20f, 0.55f, 0.30f, 0.9f));
        btnRight = new UiButton(margin + size + gap, y, size, size, "右 ▶", new Color(0.20f, 0.55f, 0.30f, 0.9f));
        btnThrottle = new UiButton(w - margin - size, y, size, size, "油门", new Color(0.85f, 0.45f, 0.10f, 0.95f));
        btnBrake = new UiButton(w - margin - size * 2 - gap, y, size, size, "刹车/倒车", new Color(0.80f, 0.15f, 0.15f, 0.95f));

        float s2 = size * 0.7f;
        rectCamera = new Rect(w - margin - s2, margin, s2, s2);
        rectPause = new Rect(margin, margin, s2, s2);
    }
}
