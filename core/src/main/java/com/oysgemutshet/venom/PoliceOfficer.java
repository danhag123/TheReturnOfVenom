package com.oysgemutshet.venom;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class PoliceOfficer {

    private static final float WALK_SPEED = 70f;
    private static final int   MAX_HITS   = 3;
    private static final float SHOOT_INTERVAL = 0.18f; // time between bullets in a burst
    private static final float SHOOT_COOLDOWN = 2.5f;  // time between bursts

    public enum State { WALKING, SHOOTING }

    private final Rectangle bounds;
    private final int dir;        // 1 = right, -1 = left
    private final int burstSize;  // how many bullets per burst (1 in easy, 3 in hard)

    private State state = State.WALKING;
    private float stateTimer = 0f;
    private int bulletsShotInBurst = 0;
    private int hitsTaken = 0;
    private boolean alive = true;

    public PoliceOfficer(float x, float y, int dir, int burstSize) {
        this.dir = dir;
        this.burstSize = Math.max(1, burstSize); // safety: at least 1
        this.bounds = new Rectangle(x, y, 26, 60);
    }

    public void update(float delta, Array<PoliceBullet> bullets) {
        if (!alive) return;

        stateTimer += delta;

        switch (state) {
            case WALKING:
                bounds.x += dir * WALK_SPEED * delta;

                // After cooldown, start a shooting burst
                if (stateTimer > SHOOT_COOLDOWN) {
                    state = State.SHOOTING;
                    stateTimer = 0f;
                    bulletsShotInBurst = 0;
                }
                break;

            case SHOOTING:
                // Stand still and fire burstSize bullets
                if (bulletsShotInBurst < burstSize && stateTimer >= SHOOT_INTERVAL) {
                    stateTimer -= SHOOT_INTERVAL;
                    bulletsShotInBurst++;

                    float muzzleX = (dir == 1) ? bounds.x + bounds.width : bounds.x;
                    float muzzleY = bounds.y + bounds.height * 0.55f;

                    bullets.add(new PoliceBullet(muzzleX, muzzleY, dir));
                }

                // Done with this burst → go back to walking
                if (bulletsShotInBurst >= burstSize && stateTimer > SHOOT_INTERVAL * 1.5f) {
                    state = State.WALKING;
                    stateTimer = 0f;
                }
                break;
        }
    }

    public void render(ShapeRenderer renderer) {
        if (!alive) return;

        float x = bounds.x;
        float y = bounds.y;
        float w = bounds.width;
        float h = bounds.height;

        float headH  = h * 0.25f;
        float bodyH  = h * 0.45f;
        float legH   = h * 0.30f;

        float headY  = y + bodyH + legH;
        float bodyY  = y + legH;
        float legsY  = y;

        // light blue police uniform
        Color uniform = new Color(0.35f, 0.55f, 0.95f, 1f);

        // legs (dark pants)
        renderer.setColor(0.15f, 0.18f, 0.22f, 1f);
        renderer.rect(x + w * 0.1f, legsY, w * 0.32f, legH);
        renderer.rect(x + w * 0.58f, legsY, w * 0.32f, legH);

        // body (shirt)
        renderer.setColor(uniform);
        renderer.rect(x + w * 0.1f, bodyY, w * 0.8f, bodyH);

        // head (skin)
        renderer.setColor(1f, 0.9f, 0.7f, 1f);
        renderer.rect(x + w * 0.15f, headY, w * 0.7f, headH);

        // hat
        renderer.setColor(uniform);
        renderer.rect(x + w * 0.1f, headY + headH, w * 0.8f, headH * 0.7f);
        renderer.setColor(0.1f, 0.15f, 0.2f, 1f);
        renderer.rect(x + w * 0.05f, headY + headH * 1.55f, w * 0.9f, headH * 0.2f);

        // badge
        renderer.setColor(Color.GOLD);
        renderer.rect(x + w * 0.65f, bodyY + bodyH * 0.4f, w * 0.12f, bodyH * 0.22f);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void registerHit() {
        if (!alive) return;
        hitsTaken++;
        if (hitsTaken >= MAX_HITS) {
            alive = false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isOffscreen(float worldWidth) {
        return (dir == 1 && bounds.x > worldWidth + bounds.width)
            || (dir == -1 && bounds.x + bounds.width < -bounds.width);
    }
}
