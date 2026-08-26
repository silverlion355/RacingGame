package com.racinggame.core.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Node;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.racinggame.core.GameConstants;
import com.racinggame.core.levels.LevelDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序化赛道：由路点（中心线闭合环）生成路面、护栏与外围地面网格。
 * 提供进度采样（用于排名/圈数）与边界判断。
 */
public class Track {
    private final List<Vector2> points = new ArrayList<>();
    private final float[] cumDist;
    private final float totalLength;
    private final float halfWidth = GameConstants.TRACK_WIDTH / 2f;
    private final float segLen;

    public final Model model;
    public final ModelInstance instance;

    /** 采样结果 */
    public static class Sample {
        public float distance;   // 沿赛道累计距离
        public float progress;   // 0..1
        public boolean offTrack; // 是否越界
        public int index;        // 最近路点索引
    }

    public Track(LevelDefinition level) {
        generate(level);
        this.cumDist = new float[points.size()];
        float acc = 0;
        for (int i = 0; i < points.size(); i++) {
            cumDist[i] = acc;
            int next = (i + 1) % points.size();
            acc += points.get(i).dst(points.get(next));
        }
        this.totalLength = acc;
        this.segLen = totalLength / points.size();
        this.model = buildModel();
        this.instance = new ModelInstance(model);
    }

    /** 极坐标扰动法生成闭环中心线：r(θ) 恒正，确保不自交 */
    private void generate(LevelDefinition level) {
        int n = Math.max(72, level.curveCount * 10);
        float r = level.baseRadius;
        for (int i = 0; i < n; i++) {
            float a = (float) (i * MathUtils.PI2 / n);
            float radius = r * (1f
                    + 0.18f * (float) Math.sin(level.curveCount * a)
                    + 0.10f * (float) Math.sin(2f * level.curveCount * a + 1.3f));
            points.add(new Vector2(radius * (float) Math.cos(a), radius * (float) Math.sin(a)));
        }
    }

    private Model buildModel() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        int attrs = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        // 外围地面（草地）
        Material groundMat = new Material(ColorAttribute.createDiffuse(new Color(0.16f, 0.40f, 0.15f, 1f)));
        mb.part("ground", GL20.GL_TRIANGLES, attrs, groundMat)
                .box(-1000f, -1f, -1000f, 2000f, 1f, 2000f);

        // 路面
        Material roadMat = new Material(ColorAttribute.createDiffuse(new Color(0.30f, 0.31f, 0.34f, 1f)));
        MeshPartBuilder road = mb.part("road", GL20.GL_TRIANGLES, attrs, roadMat);

        // 护栏（左红右白）
        Material railLMat = new Material(ColorAttribute.createDiffuse(new Color(0.80f, 0.12f, 0.12f, 1f)));
        Material railRMat = new Material(ColorAttribute.createDiffuse(new Color(0.92f, 0.92f, 0.92f, 1f)));
        MeshPartBuilder railL = mb.part("railL", GL20.GL_TRIANGLES, attrs, railLMat);
        MeshPartBuilder railR = mb.part("railR", GL20.GL_TRIANGLES, attrs, railRMat);

        // 标线材质（白）
        Material lineMat = new Material(ColorAttribute.createDiffuse(new Color(0.95f, 0.95f, 0.90f, 1f)));
        MeshPartBuilder centerLine = mb.part("centerLine", GL20.GL_TRIANGLES, attrs, lineMat);
        MeshPartBuilder edgeLine = mb.part("edgeLine", GL20.GL_TRIANGLES, attrs, lineMat);

        // 外景材质
        Material trunkMat = new Material(ColorAttribute.createDiffuse(new Color(0.36f, 0.24f, 0.13f, 1f)));
        Material leafMat = new Material(ColorAttribute.createDiffuse(new Color(0.10f, 0.45f, 0.12f, 1f)));
        Material poleMat = new Material(ColorAttribute.createDiffuse(new Color(0.30f, 0.30f, 0.32f, 1f)));
        Material bulbMat = new Material(ColorAttribute.createDiffuse(new Color(1f, 0.90f, 0.50f, 1f)),
                ColorAttribute.createEmissive(new Color(1f, 0.85f, 0.40f, 1f)));

        float y0 = GameConstants.ROAD_Y;
        float y1 = y0 + GameConstants.RAIL_HEIGHT;
        Vector2 dir = new Vector2();
        Vector2 nrm = new Vector2();

        int n = points.size();
        for (int i = 0; i < n; i++) {
            Vector2 a = points.get(i);
            Vector2 b = points.get((i + 1) % n);
            dir.set(b).sub(a).nor();
            nrm.set(-dir.y, dir.x); // 左法线

            Vector2 aL = a.cpy().add(nrm.x * halfWidth, nrm.y * halfWidth);
            Vector2 aR = a.cpy().sub(nrm.x * halfWidth, nrm.y * halfWidth);
            Vector2 bL = b.cpy().add(nrm.x * halfWidth, nrm.y * halfWidth);
            Vector2 bR = b.cpy().sub(nrm.x * halfWidth, nrm.y * halfWidth);

            // 路面两三角
            road.triangle(v(aL, y0), v(aR, y0), v(bR, y0));
            road.triangle(v(aL, y0), v(bR, y0), v(bL, y0));

            // 左护栏（竖直带）
            railL.triangle(v(aL, y0), v(aL, y1), v(bL, y1));
            railL.triangle(v(aL, y0), v(bL, y1), v(bL, y0));
            // 右护栏
            railR.triangle(v(aR, y0), v(bR, y1), v(aR, y1));
            railR.triangle(v(aR, y0), v(bR, y0), v(bR, y1));

            // 中线（虚线感：隔段绘制）
            if (i % 2 == 0) {
                addRibbon(centerLine, a, b, nrm, 0.16f, y0 + 0.02f);
            }
            // 两侧边线
            addRibbon(edgeLine, aL, bL, nrm, 0.12f, y0 + 0.02f);
            addRibbon(edgeLine, aR, bR, nrm, 0.12f, y0 + 0.02f);
        }

        // 起跑/终点线（黑白格）
        Vector2 a0 = points.get(0);
        Vector2 b0 = points.get(1);
        Vector2 dir0 = b0.cpy().sub(a0).nor();
        Vector2 nrm0 = new Vector2(-dir0.y, dir0.x);
        float ang = (float) Math.atan2(dir0.y, dir0.x);
        int K = 16;
        float sqW = (halfWidth * 2f) / K;
        Material whiteM = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        Material blackM = new Material(ColorAttribute.createDiffuse(Color.BLACK));
        for (int k = 0; k < K; k++) {
            float off = -halfWidth + (k + 0.5f) * sqW;
            Vector2 c = a0.cpy().add(nrm0.x * off, nrm0.y * off);
            Material m = (k % 2 == 0) ? whiteM : blackM;
            Node sn = mb.node();
            sn.translation.set(c.x, y0 + 0.03f, c.y);
            sn.rotation.set(Vector3.Y, ang);
            mb.part("start", GL20.GL_TRIANGLES, attrs, m)
                    .box(-sqW * 0.45f, -0.02f, -1.0f, sqW * 0.9f, 0.04f, 2.0f);
        }

        // 赛道外景：树与路灯（护栏外侧）
        int sceneryStep = 9;
        for (int i = 0; i < n; i += sceneryStep) {
            Vector2 a = points.get(i);
            Vector2 b = points.get((i + 1) % n);
            Vector2 d = b.cpy().sub(a).nor();
            Vector2 nm = new Vector2(-d.y, d.x);
            for (int s : new int[]{1, -1}) {
                Vector2 base = a.cpy().add(nm.x * (halfWidth + 5f) * s, nm.y * (halfWidth + 5f) * s);
                Node tn = mb.node();
                tn.translation.set(base.x, 1f, base.y);
                mb.part("trunk", GL20.GL_TRIANGLES, attrs, trunkMat).cylinder(0.3f, 2f, 8);
                Node ln = mb.node();
                ln.translation.set(base.x, 2.6f, base.y);
                mb.part("leaf", GL20.GL_TRIANGLES, attrs, leafMat).cone(1.6f, 3.2f, 10);
            }
            if (i % (sceneryStep * 2) == 0) {
                Vector2 base = a.cpy().add(nm.x * (halfWidth + 2.5f), nm.y * (halfWidth + 2.5f));
                Node pn = mb.node();
                pn.translation.set(base.x, 2f, base.y);
                mb.part("pole", GL20.GL_TRIANGLES, attrs, poleMat).cylinder(0.15f, 4f, 6);
                Node bn = mb.node();
                bn.translation.set(base.x, 4f, base.y);
                mb.part("bulb", GL20.GL_TRIANGLES, attrs, bulbMat).box(-0.3f, -0.3f, -0.3f, 0.6f, 0.6f, 0.6f);
            }
        }

        return mb.end();
    }

    /** 在 a→b 段上沿法线 nrm 生成一条半宽 halfW 的带状几何（用于标线） */
    private void addRibbon(MeshPartBuilder b, Vector2 a, Vector2 b, Vector2 nrm, float halfW, float y) {
        Vector2 aL = a.cpy().add(nrm.x * halfW, nrm.y * halfW);
        Vector2 aR = a.cpy().sub(nrm.x * halfW, nrm.y * halfW);
        Vector2 bL = b.cpy().add(nrm.x * halfW, nrm.y * halfW);
        Vector2 bR = b.cpy().sub(nrm.x * halfW, nrm.y * halfW);
        b.triangle(v(aL, y), v(aR, y), v(bR, y));
        b.triangle(v(aL, y), v(bR, y), v(bL, y));
    }

    private static Vector3 v(Vector2 p, float y) {
        return new Vector3(p.x, y, p.y);
    }

    /** 采样某点到赛道中心线的进度与越界状态 */
    public Sample sample(Vector2 p) {
        Sample s = new Sample();
        s.distance = Float.MAX_VALUE;
        s.offTrack = false;
        float best = Float.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            Vector2 a = points.get(i);
            Vector2 b = points.get((i + 1) % points.size());
            Vector2 ab = b.cpy().sub(a);
            float len2 = ab.len2();
            if (len2 < 1e-6f) continue;
            Vector2 ap = p.cpy().sub(a);
            float t = MathUtils.clamp(ap.dot(ab) / len2, 0f, 1f);
            Vector2 proj = a.cpy().add(ab.scl(t));
            float d = proj.dst(p);
            if (d < best) {
                best = d;
                s.index = i;
                s.distance = cumDist[i] + t * (float) Math.sqrt(len2);
                s.offTrack = d > halfWidth;
            }
        }
        s.progress = totalLength > 0 ? s.distance / totalLength : 0f;
        return s;
    }

    public Vector2 getWaypoint(int i) {
        int n = points.size();
        return points.get(((i % n) + n) % n);
    }

    public int getWaypointCount() {
        return points.size();
    }

    public float segmentLength() {
        return segLen;
    }

    public float getTotalLength() {
        return totalLength;
    }

    /** 起点朝向（指向第 1 个路点） */
    public float getStartHeading() {
        Vector2 d = points.get(1).cpy().sub(points.get(0));
        return (float) Math.atan2(d.y, d.x);
    }

    /** 第 slot 辆车在起跑格的位置（略过起点线、左右错开） */
    public Vector2 getStartPosition(int slot) {
        Vector2 dir = points.get(1).cpy().sub(points.get(0)).nor();
        Vector2 nrm = new Vector2(-dir.y, dir.x);
        float back = 3f + slot * 3.5f;
        float side = (slot % 2 == 0 ? 1 : -1) * (halfWidth * 0.4f);
        return points.get(0).cpy().add(dir.x * back, dir.y * back).add(nrm.x * side, nrm.y * side);
    }

    public void dispose() {
        if (model != null) model.dispose();
    }
}
