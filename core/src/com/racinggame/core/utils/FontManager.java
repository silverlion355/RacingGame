package com.racinggame.core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * 字体工厂：优先用系统 CJK 字体（Android 自带 Noto CJK）通过 FreeType 生成，
 * 以保证中文 UI 正常显示；找不到则回退到 libGDX 默认字体（仅 ASCII）。
 */
public final class FontManager {

    private FontManager() {
    }

    private static final String CJK_CHARS =
            "中文赛车游戏开始菜单选关关卡排行积分总暂停视角切换油门刹车倒车胜利完成返回下一重玩车库银箭法拉利保时捷第一二三四五名圈速度时间路你我的与及最难度递增更长弯道对手快进设";

    public static BitmapFont createFont(int size) {
        try {
            String[] candidates = {
                    "/system/fonts/NotoSansCJK-Regular.ttc",
                    "/system/fonts/NotoSansCJKsc-Regular.ttc",
                    "/system/fonts/NotoSansSC-Regular.otf",
                    "/system/fonts/DroidSansFallback.ttf",
                    "/system/fonts/DroidSansFallbackFull.ttf"
            };
            for (String path : candidates) {
                FileHandle f = Gdx.files.absolute(path);
                if (f.exists()) {
                    FreeTypeFontGenerator gen = new FreeTypeFontGenerator(f);
                    FreeTypeFontGenerator.FreeTypeFontParameter p =
                            new FreeTypeFontGenerator.FreeTypeFontParameter();
                    p.size = size;
                    p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + CJK_CHARS;
                    p.flip = false;
                    BitmapFont font = gen.generateFont(p);
                    gen.dispose();
                    return font;
                }
            }
        } catch (Exception ignored) {
            // 回退默认字体
        }
        return new BitmapFont();
    }
}
