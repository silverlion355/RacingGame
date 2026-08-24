package com.racinggame.core.utils;

/** 屏幕坐标矩形（左上角原点，y 向下，与 libGDX 触摸坐标一致） */
public class Rect {
    public float x, y, w, h;

    public Rect(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /** 是否包含某屏幕点（libGDX 触摸坐标已是左上角原点 y 向下） */
    public boolean contains(float px, float py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public float centerX() {
        return x + w / 2f;
    }

    public float centerY() {
        return y + h / 2f;
    }
}
