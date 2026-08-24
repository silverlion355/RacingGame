package com.racinggame.core.levels;

/** 单个关卡的数据定义（纯数据，便于扩展更多关卡） */
public class LevelDefinition {
    public final int id;
    public final String name;
    public final String desc;
    public final float baseRadius;   // 赛道基础半径（越大越长）
    public final int curveCount;      // 弯道数量（正弦扰动次数）
    public final int opponentCount;   // 对手数量
    public final float opponentSpeed; // 对手目标速度
    public final int laps;            // 目标圈数
    public final float playerMaxSpeed;// 玩家最高速度

    public LevelDefinition(int id, String name, String desc,
                           float baseRadius, int curveCount, int opponentCount,
                           float opponentSpeed, int laps, float playerMaxSpeed) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.baseRadius = baseRadius;
        this.curveCount = curveCount;
        this.opponentCount = opponentCount;
        this.opponentSpeed = opponentSpeed;
        this.laps = laps;
        this.playerMaxSpeed = playerMaxSpeed;
    }
}
