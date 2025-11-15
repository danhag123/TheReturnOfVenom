package com.oysgemutshet.venom;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class PoliceBullet {

    private static final float SPEED = 380f;
    private static final float RADIUS = 4f;

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private boolean alive = true;

    public PoliceBullet(float x, float y, int dir) {
        position.set(x, y);
        velocity.set(SPEED * dir, 0f);
    }

    public void update(float delta) {
        if (!alive) return;
        position.mulAdd(velocity, delta);
    }

    public void render(ShapeRenderer renderer) {
        if (!alive) return;
        renderer.setColor(Color.LIGHT_GRAY);
        renderer.circle(position.x, position.y, RADIUS);
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x - RADIUS, position.y - RADIUS, RADIUS * 2f, RADIUS * 2f);
    }

    public boolean isAlive() {
        return alive;
    }

    public void destroy() {
        alive = false;
    }
}
