#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
程序化生成游戏音效 WAV（纯标准库 wave/struct/math，无需联网或外部素材）。
输出 4 个文件到 android/assets/audio/：
  - engine.wav : 低频循环引擎声（可无缝循环）
  - crash.wav  : 短促噪声爆破（碰撞）
  - click.wav  : 短促高频（按钮点击）
  - win.wav    : 上行琶音（过关）

采样率 44100，16bit，单声道。
用法: python3 tools/gen_audio.py [输出目录]
"""

import math
import os
import struct
import sys
import wave

SAMPLE_RATE = 44100
AMP = 32767  # 16-bit 峰值


def write_wav(path, samples):
    """samples: list[float] in [-1, 1]"""
    with wave.open(path, 'w') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for s in samples:
            s = max(-1.0, min(1.0, s))
            frames += struct.pack('<h', int(s * AMP))
        w.writeframes(bytes(frames))
    print("  生成:", path, "(%d 采样)" % len(samples))


def gen_engine(duration=1.0):
    """低频引擎轰鸣，频率取整数 Hz 以保证无缝循环。"""
    n = int(SAMPLE_RATE * duration)
    out = []
    for i in range(n):
        t = i / SAMPLE_RATE
        wob = 0.9 + 0.1 * math.sin(2 * math.pi * 8 * t)  # 8Hz 抖动，整数周期
        s = (0.30 * math.sin(2 * math.pi * 60 * t)
             + 0.18 * math.sin(2 * math.pi * 120 * t)
             + 0.09 * math.sin(2 * math.pi * 180 * t)) * wob
        out.append(s * 0.6)
    return out


def gen_crash(duration=0.35):
    """短促噪声爆破，指数衰减。"""
    import random
    random.seed(42)
    n = int(SAMPLE_RATE * duration)
    out = []
    for i in range(n):
        t = i / SAMPLE_RATE
        decay = math.exp(-t * 14.0)
        out.append((random.uniform(-1.0, 1.0) * decay) * 0.7)
    return out


def gen_click(duration=0.06):
    """短促高频点击。"""
    n = int(SAMPLE_RATE * duration)
    out = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 90.0)
        out.append(math.sin(2 * math.pi * 1400 * t) * env * 0.5)
    return out


def gen_win():
    """上行琶音（C5 E5 G5 C6），带简单包络。"""
    notes = [523.25, 659.25, 783.99, 1046.50]
    note_dur = 0.22
    out = []
    for f in notes:
        n = int(SAMPLE_RATE * note_dur)
        for i in range(n):
            t = i / SAMPLE_RATE
            # attack-release 包络
            if t < 0.02:
                env = t / 0.02
            else:
                env = math.exp(-(t - 0.02) * 6.0)
            out.append(math.sin(2 * math.pi * f * t) * env * 0.5)
    return out


def main():
    out_dir = sys.argv[1] if len(sys.argv) > 1 else 'android/assets/audio'
    os.makedirs(out_dir, exist_ok=True)
    print("输出目录:", out_dir)
    write_wav(os.path.join(out_dir, 'engine.wav'), gen_engine())
    write_wav(os.path.join(out_dir, 'crash.wav'), gen_crash())
    write_wav(os.path.join(out_dir, 'click.wav'), gen_click())
    write_wav(os.path.join(out_dir, 'win.wav'), gen_win())
    print("全部音效已生成。")


if __name__ == '__main__':
    main()
