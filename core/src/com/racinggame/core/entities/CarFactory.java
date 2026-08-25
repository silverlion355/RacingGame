package com.racinggame.core.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/** 按品牌配色程序化生成低多边形车体（无需外部模型资源） */
public final class CarFactory {

    private CarFactory() {
    }

    public static Model buildCarModel(Color bodyColor) {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        Material bodyMat = new Material(
                ColorAttribute.createDiffuse(bodyColor),
                ColorAttribute.createSpecular(Color.WHITE),
                FloatAttribute.createShininess(32f));
        Material glassMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.13f, 0.15f, 0.22f, 1f)),
                ColorAttribute.createSpecular(new Color(0.6f, 0.6f, 0.7f, 1f)),
                FloatAttribute.createShininess(64f));
        Material tireMat = new Material(ColorAttribute.createDiffuse(new Color(0.07f, 0.07f, 0.08f, 1f)));

        int attrs = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        // 车身底盘
        mb.node().translation.set(0f, 0.55f, 0f);
        mb.part("body", GL20.GL_TRIANGLES, attrs, bodyMat)
                .box(-0.9f, -0.3f, -2f, 1.8f, 0.6f, 4f);

        // 车顶座舱
        mb.node().translation.set(0f, 0.95f, 0.2f);
        mb.part("roof", GL20.GL_TRIANGLES, attrs, glassMat)
                .box(-0.7f, -0.25f, -1f, 1.4f, 0.5f, 2f);

        // 四个车轮
        float[][] wheels = {
                {0.95f, 1.3f}, {-0.95f, 1.3f}, {0.95f, -1.3f}, {-0.95f, -1.3f}
        };
        for (float[] w : wheels) {
            mb.node().translation.set(w[0], 0.35f, w[1]);
            mb.part("wheel", GL20.GL_TRIANGLES, attrs, tireMat)
                    .box(-0.2f, -0.35f, -0.45f, 0.4f, 0.7f, 0.9f);
        }

        return mb.end();
    }
}
