package com.racinggame.core.systems;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.racinggame.core.GameConstants;
import com.racinggame.core.entities.Car;

/**
 * 相对 3D 摄像机：追尾视角（默认）与车内第一视角，可实时切换。
 */
public class CameraController {
    public enum Mode { CHASE, COCKPIT }

    public Mode mode;
    private final PerspectiveCamera cam;

    public CameraController(PerspectiveCamera cam, Mode initial) {
        this.cam = cam;
        this.mode = initial;
    }

    public void toggle() {
        mode = (mode == Mode.CHASE) ? Mode.COCKPIT : Mode.CHASE;
    }

    public void update(Car car) {
        update(car, 0f);
    }

    public void update(Car car, float speedRatio) {
        cam.fieldOfView = GameConstants.BASE_FOV + Math.min(1f, speedRatio) * GameConstants.MAX_FOV_BOOST;
        Vector3 fwd = new Vector3(car.forward().x, 0, car.forward().y);
        Vector3 carPos = new Vector3(car.position.x, 0, car.position.y);

        if (mode == Mode.CHASE) {
            Vector3 desired = carPos.cpy()
                    .sub(fwd.cpy().scl(GameConstants.CHASE_DIST))
                    .add(0, GameConstants.CHASE_HEIGHT, 0);
            cam.position.lerp(desired, 0.12f);
            cam.lookAt(carPos.cpy().add(fwd.cpy().scl(GameConstants.CHASE_LOOK_AHEAD)).add(0, 1.2f, 0));
        } else {
            // 车内第一视角：贴近车头、略高
            Vector3 desired = carPos.cpy()
                    .add(fwd.cpy().scl(GameConstants.COCKPIT_FORWARD))
                    .add(0, GameConstants.COCKPIT_HEIGHT, 0);
            cam.position.set(desired);
            cam.lookAt(carPos.cpy().add(fwd.cpy().scl(20f)).add(0, GameConstants.COCKPIT_HEIGHT - 0.3f, 0));
        }
        cam.up.set(0, 1, 0);
        cam.update();
    }
}
