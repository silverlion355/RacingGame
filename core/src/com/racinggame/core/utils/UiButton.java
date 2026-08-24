package com.racinggame.core.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 轻量 UI 按钮：无需外部 Skin 资源，使用 ShapeRenderer 画底、SpriteBatch 画文字。
 * 坐标系为「左上角原点，y 向下」，与 Rect/触摸坐标一致。
 */
public class UiButton {
    public final Rect rect;
    public final String label;
    public final Color color;
    private final GlyphLayout layout = new GlyphLayout();

    public UiButton(float x, float y, float w, float h, String label) {
        this(x, y, w, h, label, new Color(0.23f, 0.51f, 0.96f, 0.92f));
    }

    public UiButton(float x, float y, float w, float h, String label, Color color) {
        this.rect = new Rect(x, y, w, h);
        this.label = label;
        this.color = color;
    }

    public void draw(ShapeRenderer sr, SpriteBatch batch, BitmapFont font, boolean pressed) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(pressed ? color.cpy().mul(1.15f) : color);
        sr.rect(rect.x, rect.y, rect.w, rect.h);
        sr.end();

        layout.setText(font, label);
        batch.begin();
        font.setColor(Color.WHITE);
        float tx = rect.x + (rect.w - layout.width) / 2f;
        float ty = rect.y + (rect.h + layout.height) / 2f;
        TextDraw.draw(batch, font, layout, tx, ty);
        batch.end();
    }
}
