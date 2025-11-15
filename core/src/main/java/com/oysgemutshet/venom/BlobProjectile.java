package com.oysgemutshet.venom;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;


public class BlobProjectile {

    private static final float SPEED = 420f;
    private static final float MAX_LIFE = 1.6f; // seconds
    private static final float BASE_RADIUS = 10f;

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private float age = 0f;
    private boolean alive = true;

    public BlobProjectile(float x, float y, int dir) {
        position.set(x, y);
        velocity.set(SPEED * dir, 0f); // horizontal shot
    }

    public void update(float delta) {
        if (!alive) return;

        age += delta;
        position.mulAdd(velocity, delta);

        if (age > MAX_LIFE) {
            alive = false; // dissipated
        }
    }

    public void render(ShapeRenderer renderer) {
        if (!alive) return;

        float t = age / MAX_LIFE; // 0 → 1

        // Main blob radius
        float r = BASE_RADIUS * (1.0f - 0.15f * t);

        if (t < 0.7f) {
            // ===== PHASE 1: flying / impact =====

            // Main dark blob
            renderer.setColor(0.02f, 0.02f, 0.06f, 1f);
            renderer.circle(position.x, position.y, r);

            // Goo droplets / smear
            renderer.setColor(0.02f, 0.02f, 0.10f, 1f);
            renderer.circle(position.x - 0.6f * r, position.y - 0.2f * r, r * 0.35f);
            renderer.circle(position.x + 0.5f * r, position.y + 0.1f * r, r * 0.25f);

            // Highlight (shiny top)
            renderer.setColor(0.45f, 0.45f, 0.8f, 1f);
            renderer.circle(position.x - 0.2f * r, position.y + 0.4f * r, r * 0.25f);

        } else {
            // ===== PHASE 2: dissipate into droplets =====
            float fade = (t - 0.05f) / 0.3f; // 0 → 1 over last 30% of life
            float alpha = 1.0f - fade;      // fade out
            float base = r * (1.0f - 0.5f * fade);

            // 3–4 small droplets breaking apart
            renderer.setColor(0.05f, 0.05f, 0.15f, alpha);
            renderer.circle(position.x + 0.2f * r, position.y - 0.1f * r, base * 0.7f);
            renderer.circle(position.x - 0.4f * r, position.y - 0.3f * r, base * 0.5f);

            renderer.setColor(0.10f, 0.10f, 0.25f, alpha * 0.9f);
            renderer.circle(position.x + 0.1f * r, position.y - 0.4f * r, base * 0.4f);
            renderer.circle(position.x - 0.2f * r, position.y - 0.5f * r, base * 0.3f);
        }
    }


    public boolean isAlive() {
        return alive;
    }

    public Rectangle getBounds() {
        // simple generous rectangle; fine for this game
        float r = BASE_RADIUS;
        return new Rectangle(position.x - r, position.y - r, r * 2f, r * 2f);
    }

    public void destroy() {
        alive = false;
    }

}
