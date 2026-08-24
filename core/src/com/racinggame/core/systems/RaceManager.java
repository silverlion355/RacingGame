package com.racinggame.core.systems;

import com.racinggame.core.entities.AICar;
import com.racinggame.core.entities.Car;
import com.racinggame.core.entities.PlayerCar;
import com.racinggame.core.entities.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 比赛管理器：推进所有车辆物理、边界/车辆碰撞、圈数与排名判定、完赛检测。
 */
public class RaceManager {
    public final Track track;
    public final PlayerCar player;
    public final List<AICar> aiCars;
    public final int lapTarget;

    public float raceTime = 0;
    public boolean raceOver = false;
    public boolean playerFinished = false;
    public boolean playerHitThisFrame = false; // 供 GameScreen 触发碰撞音效

    public RaceManager(Track track, PlayerCar player, List<AICar> aiCars, int lapTarget) {
        this.track = track;
        this.player = player;
        this.aiCars = aiCars;
        this.lapTarget = lapTarget;
        initProgress();
    }

    /** 初始化各车进度，避免起跑即误判圈数 */
    public void initProgress() {
        Track.Sample s = track.sample(player.position);
        player.lastProgress = s.progress;
        player.totalDistance = s.distance;
        for (AICar ai : aiCars) {
            Track.Sample as = track.sample(ai.position);
            ai.lastProgress = as.progress;
            ai.totalDistance = as.distance;
        }
    }

    public void update(float dt, float steer, float throttle, float brake) {
        if (raceOver) return;
        raceTime += dt;
        playerHitThisFrame = false;

        // 1) 推进物理
        player.applyControl(steer, throttle, brake, dt);
        for (AICar ai : aiCars) ai.updateAI(track, dt);

        // 2) 边界采样 + 越界减速（抓地力下降）
        Track.Sample ps = track.sample(player.position);
        player.offTrack = ps.offTrack;
        if (player.offTrack) player.speed *= 0.95f;
        for (AICar ai : aiCars) {
            Track.Sample as = track.sample(ai.position);
            ai.offTrack = as.offTrack;
            if (ai.offTrack) ai.speed *= 0.96f;
        }

        // 3) 车车碰撞
        handleCarCollisions();

        // 4) 圈数 / 进度
        updateProgress(player, ps);
        for (AICar ai : aiCars) {
            Track.Sample as = track.sample(ai.position);
            updateProgress(ai, as);
        }

        // 5) 完赛
        if (!playerFinished && player.lapsCompleted >= lapTarget) {
            player.finished = true;
            playerFinished = true;
            raceOver = true;
        }
    }

    private void updateProgress(Car car, Track.Sample s) {
        float p = s.progress;
        if (car.lastProgress > 0.8f && p < 0.2f) car.lapsCompleted++;
        else if (car.lastProgress < 0.2f && p > 0.8f) car.lapsCompleted = Math.max(0, car.lapsCompleted - 1);
        car.lastProgress = p;
        car.totalDistance = car.lapsCompleted * track.getTotalLength() + s.distance;
    }

    private void handleCarCollisions() {
        List<Car> all = new ArrayList<>();
        all.add(player);
        all.addAll(aiCars);
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                Car a = all.get(i), b = all.get(j);
                float d = a.position.dst(b.position);
                float min = a.radius + b.radius;
                if (d < min && d > 1e-4f) {
                    float overlap = (min - d);
                    float nx = (b.position.x - a.position.x) / d;
                    float ny = (b.position.y - a.position.y) / d;
                    a.position.x -= nx * overlap * 0.5f;
                    a.position.y -= ny * overlap * 0.5f;
                    b.position.x += nx * overlap * 0.5f;
                    b.position.y += ny * overlap * 0.5f;
                    a.speed *= 0.86f;
                    b.speed *= 0.86f;
                    if (a == player || b == player) playerHitThisFrame = true;
                }
            }
        }
    }

    /** 玩家当前排名（1 为第一） */
    public int getPlayerRank() {
        int rank = 1;
        for (AICar ai : aiCars) {
            if (ai.totalDistance > player.totalDistance) rank++;
        }
        return rank;
    }

    /** 全部车辆按进度排序（第一名在前） */
    public List<Car> getStandings() {
        List<Car> all = new ArrayList<>();
        all.add(player);
        all.addAll(aiCars);
        Collections.sort(all, new Comparator<Car>() {
            @Override
            public int compare(Car a, Car b) {
                return Float.compare(b.totalDistance, a.totalDistance);
            }
        });
        return all;
    }
}
