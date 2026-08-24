package com.racinggame.core.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.racinggame.core.utils.MathUtil;
import com.racinggame.core.entities.Track;

/**
 * 对手 AI：沿赛道路点行驶，根据前方目标点计算转向，弯道适当减速。
 */
public class AICar extends Car {
    public int targetWaypoint;
    public float targetSpeed;

    public AICar(int brand, float targetSpeed) {
        super(38f, 18f, 30f, 2.0f, 6f, 2.2f);
        this.brand = brand;
        this.targetSpeed = targetSpeed;
        this.maxSpeed = targetSpeed * 1.25f;
    }

    /** 简单路点跟随 AI */
    public void updateAI(Track track, float dt) {
        Vector2 target = track.getWaypoint(targetWaypoint);
        Vector2 toTarget = new Vector2(target.x - position.x, target.y - position.y);
        float dist = toTarget.len();

        // 到达当前目标点则切换到下一个
        if (dist < track.segmentLength() * 0.6f) {
            targetWaypoint = (targetWaypoint + 1) % track.getWaypointCount();
            target = track.getWaypoint(targetWaypoint);
            toTarget.set(target.x - position.x, target.y - position.y);
        }

        float desiredHeading = (float) Math.atan2(toTarget.y, toTarget.x);
        float diff = MathUtil.angleDiff(desiredHeading, heading);
        float steer = MathUtils.clamp(diff * 2.5f, -1f, 1f);

        // 弯道急时收油，直道全速
        float throttle = Math.abs(diff) > 0.9f ? 0.35f : 1f;
        // 让 AI 速度趋向目标速度
        if (speed < targetSpeed) throttle = Math.max(throttle, 0.7f);
        else throttle = 0.1f;

        applyControl(steer, throttle, 0, dt);
    }
}
