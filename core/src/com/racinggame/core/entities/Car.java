package com.racinggame.core.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * 赛车基类（街机式物理，非真实物理引擎）。
 * 位置用 2D 平面 (x,z)，heading 为朝向弧度；3D 模型每帧跟随更新。
 */
public abstract class Car {
    public Vector2 position = new Vector2();
    public float heading;          // 朝向弧度（0 指向 +X）
    public float speed;            // 当前速度（沿朝向，可为负=倒车）
    public float maxSpeed;
    public float accel;
    public float brakePower;
    public float steerRate;        // 转向速率 rad/s
    public float friction;         // 自然阻力
    public float radius;           // 碰撞圆半径
    public int brand;             // 品牌/配色索引
    public ModelInstance instance;

    // 比赛进度状态
    public int lapsCompleted = 0;
    public float lastProgress = 0;     // 上一帧赛道进度 0..1
    public float totalDistance = 0;    // 累计沿赛道距离，用于排名
    public boolean offTrack = false;
    public boolean finished = false;

    public Car(float maxSpeed, float accel, float brakePower, float steerRate, float friction, float radius) {
        this.maxSpeed = maxSpeed;
        this.accel = accel;
        this.brakePower = brakePower;
        this.steerRate = steerRate;
        this.friction = friction;
        this.radius = radius;
    }

    /** 当前前进方向（XZ 平面） */
    public Vector2 forward() {
        return new Vector2((float) Math.cos(heading), (float) Math.sin(heading));
    }

    /**
     * 应用操控并推进一帧物理。
     * @param steer   转向输入 -1(左)..+1(右)
     * @param throttle 油门 0..1
     * @param brake    刹车/倒车 0..1
     */
    public void applyControl(float steer, float throttle, float brake, float dt) {
        // 转向：速度越高转向略钝，手感更稳
        float speedFactor = 0.55f + 0.45f * Math.min(1f, Math.abs(speed) / maxSpeed);
        heading += steer * steerRate * speedFactor * dt;

        // 油门加速
        if (throttle > 0) {
            speed += accel * throttle * dt;
        }
        // 刹车 / 倒车
        if (brake > 0) {
            if (speed > 0) {
                speed -= brakePower * brake * dt;
            } else {
                speed -= accel * 0.6f * brake * dt; // 倒车
            }
        }
        // 自然阻力（无输入时滑行减速）
        if (throttle == 0 && brake == 0) {
            float drop = friction * dt;
            if (Math.abs(speed) <= drop) speed = 0;
            else speed -= Math.signum(speed) * drop;
        }

        // 限速（倒车最高速度为前进的 40%）
        speed = MathUtils.clamp(speed, -maxSpeed * 0.4f, maxSpeed);

        // 位移
        Vector2 f = forward();
        position.x += f.x * speed * dt;
        position.y += f.y * speed * dt;

        // 同步 3D 模型
        if (instance != null) {
            instance.transform.setToTranslation(position.x, 0, position.y);
            instance.transform.rotateRad(Vector3.Y, (float) (Math.PI / 2 - heading));
        }
    }
}
