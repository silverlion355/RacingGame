package com.racinggame.core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

/**
 * 在「左上角原点、y 向下」的 UI 正交相机下，正确绘制【正向】中文文本的工具。
 *
 * 背景：UI 相机使用 setToOrtho(true)（y 向下），与 libGDX 触摸坐标一致，
 * 按钮矩形与触摸命中都依赖该约定。但 BitmapFont 在该投影下会被垂直镜像。
 * 因此本工具在绘制文本时「临时」切换到 y 向上的正交投影，并把 y 坐标翻转为
 * (screenHeight - yDown)，使文本正向显示且屏幕位置不变；绘制后恢复原投影矩阵，
 * 不影响按钮/形状/触摸的 y-down 约定。
 */
public final class TextDraw {
    private TextDraw() {}

    private static final Matrix4 FLIP = new Matrix4();

    public static void draw(SpriteBatch batch, BitmapFont font, String text, float x, float yDown) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        Matrix4 prev = batch.getProjectionMatrix();
        FLIP.setToOrtho2D(0f, 0f, w, h);
        batch.setProjectionMatrix(FLIP);
        font.draw(batch, text, x, h - yDown);
        batch.setProjectionMatrix(prev);
    }

    public static void draw(SpriteBatch batch, BitmapFont font, GlyphLayout layout, float x, float yDown) {
        int h = Gdx.graphics.getHeight();
        Matrix4 prev = batch.getProjectionMatrix();
        FLIP.setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), h);
        batch.setProjectionMatrix(FLIP);
        font.draw(batch, layout, x, h - yDown);
        batch.setProjectionMatrix(prev);
    }
}
