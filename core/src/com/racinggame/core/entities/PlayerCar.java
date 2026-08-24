package com.racinggame.core.entities;

/** 玩家赛车：操控来自 TouchInputController，物理逻辑复用基类 */
public class PlayerCar extends Car {

    public PlayerCar(int brand, float maxSpeed) {
        super(maxSpeed, 22f, 38f, 2.2f, 7f, 2.2f);
        this.brand = brand;
    }
}
