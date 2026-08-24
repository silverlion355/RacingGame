package com.racinggame.core;

import com.badlogic.gdx.graphics.Color;

/**
 * 全局常量集中定义，便于统一调参与扩展。
 */
public final class GameConstants {

    private GameConstants() {
    }

    // ===== 摄像机（相对 3D 视角）=====
    /** 追尾视角：摄像机在车后方的水平距离 */
    public static final float CHASE_DIST = 15f;
    /** 追尾视角：摄像机高度 */
    public static final float CHASE_HEIGHT = 7f;
    /** 追尾视角：注视点在前方的距离 */
    public static final float CHASE_LOOK_AHEAD = 14f;
    /** 车内第一视角：摄像机高度（近似驾驶人头部） */
    public static final float COCKPIT_HEIGHT = 1.7f;
    /** 车内第一视角：摄像机相对车头略微前移 */
    public static final float COCKPIT_FORWARD = 0.3f;

    // ===== 赛道与物理（街机式简化）=====
    public static final float TRACK_WIDTH = 16f;     // 赛道宽度
    public static final float RAIL_HEIGHT = 1.6f;    // 护栏高度
    public static final float ROAD_Y = 0.1f;         // 路面离地高度
    public static final float CAR_RADIUS = 2.2f;     // 碰撞圆半径

    // ===== 品牌配色（程序化近似名车）=====
    public static final Color COLOR_FERRARI = new Color(0.83f, 0.07f, 0.17f, 1f); // 红色=法拉利
    public static final Color COLOR_PORSCHE = new Color(0.96f, 0.76f, 0.05f, 1f); // 黄色=保时捷
    public static final Color COLOR_SILVER = new Color(0.74f, 0.76f, 0.82f, 1f);  // 银色=其它
    public static final Color[] BRAND_COLORS = {COLOR_FERRARI, COLOR_PORSCHE, COLOR_SILVER};
    public static final String[] BRAND_NAMES = {"法拉利", "保时捷", "银箭"};

    // ===== 积分规则 =====
    /**
     * 根据最终排名计算积分：第1名=100，第2=60，第3=30，完赛=10。
     */
    public static int scoreForRank(int rank) {
        if (rank == 1) return 100;
        if (rank == 2) return 60;
        if (rank == 3) return 30;
        return 10; // 完赛（第4名及以后）
    }
}
