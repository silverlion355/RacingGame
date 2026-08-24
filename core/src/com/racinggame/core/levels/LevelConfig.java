package com.racinggame.core.levels;

import java.util.Arrays;
import java.util.List;

/** 初期 3 个关卡，难度递增：赛道更长 / 弯道更多 / 对手更快 */
public final class LevelConfig {

    private LevelConfig() {
    }

    public static final List<LevelDefinition> LEVELS = Arrays.asList(
            new LevelDefinition(1, "新手环路",
                    "宽阔平缓，适合熟悉操控", 60f, 4, 3, 26f, 2, 42f),
            new LevelDefinition(2, "蜿蜒峡谷",
                    "弯道增多，对手更凶", 82f, 7, 4, 31f, 3, 44f),
            new LevelDefinition(3, "极速回旋",
                    "长赛道高频弯，对手全速", 104f, 10, 5, 35f, 3, 46f)
    );

    public static LevelDefinition get(int id) {
        for (LevelDefinition l : LEVELS) {
            if (l.id == id) return l;
        }
        return LEVELS.get(0);
    }

    public static int count() {
        return LEVELS.size();
    }
}
