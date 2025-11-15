package com.oysgemutshet.venom;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class PoliceCar {

    private static final float CAR_SPEED = 420f; // faster than policemen
    private final Rectangle bounds;
    private final int dir; // 1 = right, -1 = left

    private boolean alive = true;
    private float stateTime = 0f; // for blinking lights

    public PoliceCar(float x, float groundTopY, int dir) {
        this.dir = dir;
        float width = 110f;
        float height = 40f;

        // bottom of car sits on the ground
        this.bounds = new Rectangle(x, groundTopY, width, height);
    }

    public void update(float delta) {
        if (!alive) return;
        stateTime += delta;
        bounds.x += dir * CAR_SPEED * delta;
    }

    public void render(ShapeRenderer renderer) {
        if (!alive) return;

        float x = bounds.x;
        float y = bounds.y;
        float w = bounds.width;
        float h = bounds.height;

        // Wheels
        renderer.setColor(Color.BLACK);
        float wheelH = h * 0.25f;
        float wheelW = w * 0.18f;
        float wheelY = y;
        renderer.rect(x + w * 0.08f, wheelY, wheelW, wheelH);          // front wheel
        renderer.rect(x + w - w * 0.08f - wheelW, wheelY, wheelW, wheelH); // back wheel

        // Main body (white)
        float bodyY = y + wheelH;
        float bodyH = h * 0.45f;
        renderer.setColor(Color.WHITE);
        renderer.rect(x + w * 0.02f, bodyY, w * 0.96f, bodyH);

        // Blue stripe
        renderer.setColor(0.25f, 0.45f, 0.95f, 1f); // lighter blue
        renderer.rect(x + w * 0.05f, bodyY + bodyH * 0.25f, w * 0.90f, bodyH * 0.35f);

        // Cabin
        float cabinY = bodyY + bodyH;
        float cabinH = h - (cabinY - y);
        renderer.setColor(0.9f, 0.9f, 0.95f, 1f);
        renderer.rect(x + w * 0.25f, cabinY, w * 0.5f, cabinH);

        // Windows
        renderer.setColor(0.6f, 0.75f, 0.98f, 1f);
        renderer.rect(x + w * 0.28f, cabinY + cabinH * 0.15f, w * 0.2f, cabinH * 0.6f); // front window
        renderer.rect(x + w * 0.52f, cabinY + cabinH * 0.15f, w * 0.2f, cabinH * 0.6f); // back window

        // Light bar - blinking red/blue
        float barW = w * 0.3f;
        float barH = h * 0.18f;
        float barX = x + w * 0.35f;
        float barY = cabinY + cabinH;

        boolean blinkPhase = ((int)(stateTime * 6f) % 2) == 0; // toggle ~6 times per second

        // Left light
        renderer.setColor(blinkPhase ? Color.RED : Color.BLUE);
        renderer.rect(barX, barY, barW * 0.5f, barH);

        // Right light
        renderer.setColor(blinkPhase ? Color.BLUE : Color.RED);
        renderer.rect(barX + barW * 0.5f, barY, barW * 0.5f, barH);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return alive;
    }

    public void destroy() {
        alive = false;
    }

    public boolean isOffscreen(float worldWidth) {
        return (dir == 1 && bounds.x > worldWidth + bounds.width)
            || (dir == -1 && bounds.x + bounds.width < -bounds.width);
    }
}
