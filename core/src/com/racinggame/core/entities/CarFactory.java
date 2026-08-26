package com.racinggame.core.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** 按品牌配色程序化生成低多边形车体（无需外部模型资源） */
public final class CarFactory {

    private CarFactory() {
    }

    public static Model buildCarModel(Color bodyColor) {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        int attrs = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        Material bodyMat = new Material(
                ColorAttribute.createDiffuse(bodyColor),
                ColorAttribute.createSpecular(Color.WHITE),
                FloatAttribute.createShininess(32f));
        Material glassMat = new Material(
                ColorAttribute.createDiffuse(new Color(0.13f, 0.15f, 0.22f, 1f)),
                ColorAttribute.createSpecular(new Color(0.6f, 0.6f, 0.7f, 1f)),
                FloatAttribute.createShininess(64f));
        Material lightMat = new Material(
                ColorAttribute.createDiffuse(new Color(1f, 0.95f, 0.7f, 1f)),
                ColorAttribute.createEmissive(new Color(1f, 0.9f, 0.5f, 1f)));
        Material spoilerMat = new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.10f, 0.12f, 1f)));

        // 车身底盘（更低更宽）
        mb.node().translation.set(0f, 0.45f, 0f);
        mb.part("body", GL20.GL_TRIANGLES, attrs, bodyMat)
                .box(-0.9f, -0.25f, -2f, 1.8f, 0.5f, 4f);

        // 前部楔形（略窄、靠前、更低）
        mb.node().translation.set(0f, 0.38f, 1.5f);
        mb.part("nose", GL20.GL_TRIANGLES, attrs, bodyMat)
                .box(-0.7f, -0.2f, -0.6f, 1.4f, 0.32f, 1.2f);

        // 座舱
        mb.node().translation.set(0f, 0.82f, -0.1f);
        mb.part("roof", GL20.GL_TRIANGLES, attrs, glassMat)
                .box(-0.62f, -0.22f, -0.9f, 1.24f, 0.44f, 1.8f);

        // 尾翼
        mb.node().translation.set(0f, 1.05f, -1.9f);
        mb.part("spoiler", GL20.GL_TRIANGLES, attrs, spoilerMat)
                .box(-0.9f, -0.05f, -0.12f, 1.8f, 0.1f, 0.24f);

        // 注：车轮改为独立圆柱网格，由 Car 持有并随车体滚动（见 buildWheelModel）。

        // 前大灯（自发光）
        mb.node().translation.set(0.55f, 0.45f, 2.0f);
        mb.part("hlR", GL20.GL_TRIANGLES, attrs, lightMat).box(-0.12f, -0.12f, -0.05f, 0.24f, 0.24f, 0.1f);
        mb.node().translation.set(-0.55f, 0.45f, 2.0f);
        mb.part("hlL", GL20.GL_TRIANGLES, attrs, lightMat).box(-0.12f, -0.12f, -0.05f, 0.24f, 0.24f, 0.1f);

        return mb.end();
    }

    /** 真实圆柱轮胎：预旋转使轴朝车宽方向(X)，放置时仅需平移+绕X滚动。直径 0.7，轮宽 0.4。 */
    public static Model buildWheelModel() {
        ModelBuilder mb = new ModelBuilder();
        int attrs = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        Material tireMat = new Material(ColorAttribute.createDiffuse(new Color(0.07f, 0.07f, 0.08f, 1f)));
        Model wheel = mb.createCylinder(0.4f, 0.7f, 0.7f, 18, tireMat, attrs);
        // createCylinder 的圆柱轴默认沿 Y，绕 Z 旋转 90° 使轴朝 X（车宽方向）
        for (Node n : wheel.nodes) {
            n.rotation.set(Vector3.Z, MathUtils.PI / 2f);
        }
        return wheel;
    }
}
