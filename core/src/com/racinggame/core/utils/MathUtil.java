package com.racinggame.core.utils;

import com.badlogic.gdx.math.MathUtils;

/**
 * 数学/插值工具，集中常用算法。
 */
public final class MathUtil {

    private MathUtil() {
    }

    /** 将角度差归一化到 [-PI, PI]，用于转向与 AI 朝向计算 */
    public static float angleDiff(float target, float current) {
        float diff = (target - current) % MathUtils.PI2;
        if (diff > MathUtils.PI) diff -= MathUtils.PI2;
        if (diff < -MathUtils.PI) diff += MathUtils.PI2;
        return diff;
    }

    /** 线性插值 */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** 数值限制 */
    public static float clamp(float v, float min, float max) {
        return MathUtils.clamp(v, min, max);
    }

    /** 把速度比(0..1)映射到引擎音高范围，避免刺耳 */
    public static float enginePitch(float speedRatio) {
        return MathUtils.clamp(0.5f + speedRatio * 1.2f, 0.5f, 2.0f);
    }
}
