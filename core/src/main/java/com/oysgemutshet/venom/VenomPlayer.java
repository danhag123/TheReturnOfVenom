package com.oysgemutshet.venom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class VenomPlayer {

    private static final float MOVE_SPEED = 220f;
    private static final float JUMP_FORCE = 480f;
    private static final float GRAVITY = -1000f;
    private static final float WALK_ANIM_SPEED = 10f;    // higher = faster leg swing
    private static final float WALK_ANIM_INTENSITY = 0.25f;
    private static final float TONGUE_MAX_LENGTH   = 90f;   // how far it reaches
    private static final float TONGUE_DURATION     = 0.45f; // seconds from start to fully retracted


    private final Rectangle bounds;
    private final Vector2 velocity = new Vector2();
    private boolean grounded = false;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private static final int MAX_JUMPS = 2; // normal jump + one double jump
    private int jumpsUsed = 0;
    private boolean tongueActive = false;
    private float  tongueTimer   = 0f;



    public VenomPlayer(float x, float y) {
        this.bounds = new Rectangle(x, y, 40, 80);
    }

    public void update(float delta, Array<Rectangle> platforms) {
        stateTime += delta;

        float moveX = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -1f;
            facingRight = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = 1f;
            facingRight = true;
        }

        velocity.x = moveX * MOVE_SPEED;

        // Jump + double jump
        boolean jumpPressed =
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP);

        if (jumpPressed && jumpsUsed < MAX_JUMPS) {
            velocity.y = JUMP_FORCE;
            grounded = false;
            jumpsUsed++;

            // Optional: slightly weaker second jump
            // if (jumpsUsed == 2) velocity.y *= 0.9f;
        }

        // Tongue: press G to shoot it out
        if (Gdx.input.isKeyJustPressed(Input.Keys.G) && !tongueActive) {
            tongueActive = true;
            tongueTimer = 0f;
        }



        // Gravity
        velocity.y += GRAVITY * delta;

        // Move horizontally and resolve collisions
        bounds.x += velocity.x * delta;
        resolveHorizontal(platforms);

        // Move vertically and resolve collisions
        bounds.y += velocity.y * delta;
        resolveVertical(platforms);

        // Update tongue timer
        if (tongueActive) {
            tongueTimer += delta;
            if (tongueTimer > TONGUE_DURATION) {
                tongueActive = false;
                tongueTimer = 0f;
            }
        }

    }

    private void resolveHorizontal(Array<Rectangle> platforms) {
        for (Rectangle p : platforms) {
            if (bounds.overlaps(p)) {
                if (velocity.x > 0) {
                    bounds.x = p.x - bounds.width;
                } else if (velocity.x < 0) {
                    bounds.x = p.x + p.width;
                }
                velocity.x = 0;
            }
        }
    }

    private void resolveVertical(Array<Rectangle> platforms) {
        grounded = false;

        for (Rectangle p : platforms) {
            if (bounds.overlaps(p)) {
                if (velocity.y > 0f) {
                    // Moving up: hit your head on the underside
                    bounds.y = p.y - bounds.height;
                } else {
                    // Moving down or standing: land on top
                    bounds.y = p.y + p.height;
                    grounded = true;
                }
                velocity.y = 0f;
                jumpsUsed = 0;
            }
        }
    }



    public void render(ShapeRenderer renderer) {
        float x = bounds.x;
        float y = bounds.y;
        float w = bounds.width;
        float h = bounds.height;

        float headHeight = h * 0.35f;
        float torsoHeight = h * 0.40f;
        float legsHeight = h * 0.25f;

        float headY = y + torsoHeight + legsHeight;
        float torsoY = y + legsHeight;
        float legsY = y;

        int dir = facingRight ? 1 : -1; // for swing direction
        float centerX = x + w * 0.5f;

        // Make body a bit slimmer (less square)
        float bodyWidth = w * 0.45f;
        float torsoXRight = centerX - bodyWidth * 0.5f;
        float torsoX = facingRight ? torsoXRight : mirrorX(torsoXRight, bodyWidth, centerX);

        // ===== TORSO (black) =====
        renderer.setColor(Color.BLACK);
        renderer.rect(torsoX, torsoY, bodyWidth, torsoHeight);

        // ===== HEAD (black, slightly forward) =====
        float headWidth = bodyWidth * 0.9f;
        float headXRight = centerX - headWidth * 0.5f + w * 0.05f; // nose area slightly forward
        float headX = facingRight ? headXRight : mirrorX(headXRight, headWidth, centerX);
        renderer.rect(headX, headY, headWidth, headHeight);

        // ===== LEGS (with simple walk animation) =====
        float speedFactor = Math.min(1f, Math.abs(velocity.x) / MOVE_SPEED);
        float walkPhase = stateTime * WALK_ANIM_SPEED;
        float swing = MathUtils.sin(walkPhase) * WALK_ANIM_INTENSITY * legsHeight * speedFactor;

        float legWidth = bodyWidth * 0.30f;

        // Back leg (slightly darker, behind)
        renderer.setColor(0.05f, 0.05f, 0.05f, 1f);
        float backLegXRight = centerX - legWidth * 0.5f - w * 0.03f;
        float backLegX = facingRight ? backLegXRight : mirrorX(backLegXRight, legWidth, centerX);
        float backLegY = legsY - swing * 0.5f;
        renderer.rect(backLegX, backLegY, legWidth, legsHeight * 0.95f);

        // Front leg (full black, in front)
        renderer.setColor(Color.BLACK);
        float frontLegXRight = centerX - legWidth * 0.5f + w * 0.03f;
        float frontLegX = facingRight ? frontLegXRight : mirrorX(frontLegXRight, legWidth, centerX);
        float frontLegY = legsY + swing;
        renderer.rect(frontLegX, frontLegY, legWidth, legsHeight);

        // ===== ARMS (front and back, side view, animated) =====
        float armWidth = bodyWidth * 0.22f;
        float armHeight = torsoHeight * 0.55f;
        float armBaseY = torsoY + torsoHeight * 0.25f;

        // Stronger swing for arms (re-use swing but boost it)
        float armSwing = swing * 1.8f;   // increase multiplier if you want more motion

        float backArmY = armBaseY - armSwing;  // back arm moves opposite
        float frontArmY = armBaseY + armSwing;  // front arm moves with front leg

        // BACK ARM (slightly darker, behind torso)
        renderer.setColor(0.05f, 0.05f, 0.05f, 1f);

        // Back arm sits more toward the middle of torso
        float backArmXRight = torsoXRight + bodyWidth * 0.15f;
        float backArmX = facingRight ? backArmXRight : mirrorX(backArmXRight, armWidth, centerX);
        renderer.rect(backArmX, backArmY, armWidth, armHeight);

        // FRONT ARM (full black, toward the "camera" side)
        renderer.setColor(Color.BLACK);

        // Front arm nearer viewer, slightly forward on the body
        float frontArmXRight = torsoXRight + bodyWidth - armWidth * 0.65f;
        float frontArmX = facingRight ? frontArmXRight : mirrorX(frontArmXRight, armWidth, centerX);
        renderer.rect(frontArmX, frontArmY, armWidth, armHeight);


        // ===== EYE (big white, side-facing) =====
        renderer.setColor(Color.WHITE);
        float eyeWidth = headWidth * 0.40f;
        float eyeHeight = headHeight * 0.55f;
        float eyeY = headY + headHeight * 0.35f;

        float eyeXRight = headXRight + headWidth * 0.35f;   // toward front side
        float eyeX = facingRight ? eyeXRight : mirrorX(eyeXRight, eyeWidth, centerX);
        renderer.rect(eyeX, eyeY, eyeWidth, eyeHeight);

        // ===== JAW / MOUTH (profile, properly mirrored) =====
        renderer.setColor(0.05f, 0.05f, 0.05f, 1f);
        float jawWidth  = headWidth * 0.6f;
        float jawHeight = headHeight * 0.38f;
        float jawY      = headY + headHeight * 0.08f;

        float jawXRight = headXRight + headWidth * 0.45f; // sticks out when facing right
        float jawX = facingRight ? jawXRight : mirrorX(jawXRight, jawWidth, centerX);
        renderer.rect(jawX, jawY, jawWidth, jawHeight);

// ===== TONGUE (only when active) =====
        if (tongueActive) {
            // 0→1 over the tongue animation
            float t = tongueTimer / TONGUE_DURATION;
            t = MathUtils.clamp(t, 0f, 1f);

            // Extend then retract (0→1→0)
            float phase;
            if (t <= 0.5f) {
                phase = t * 2f;          // 0→1
            } else {
                phase = (1f - t) * 2f;   // 1→0
            }

            float tongueLength   = TONGUE_MAX_LENGTH * phase;
            float tongueThickness = bounds.height * 0.12f;

            // Base of tongue: around middle of jaw
            float baseY = jawY + jawHeight * 0.6f;
            float baseX = facingRight ? (jawX + jawWidth) : jawX;

            renderer.setColor(0.8f, 0.15f, 0.15f, 1f); // red tongue

            // Draw as a rectangle sticking out from the mouth
            if (facingRight) {
                renderer.rect(baseX, baseY - tongueThickness / 2f,
                    tongueLength, tongueThickness);
            } else {
                renderer.rect(baseX - tongueLength, baseY - tongueThickness / 2f,
                    tongueLength, tongueThickness);
            }
        }

// Teeth inside jaw (draw on top of tongue)
        renderer.setColor(Color.WHITE);
        int teethCount = 5;
        float mouthInnerWidth = jawWidth * 0.85f;
        float toothSpace = mouthInnerWidth / teethCount;
        float toothW = toothSpace * 0.5f;
        float toothH = jawHeight * 0.85f;
        for (int i = 0; i < teethCount; i++) {
            float txRight = jawXRight + jawWidth * 0.05f + i * toothSpace;
            float tx = facingRight ? txRight : mirrorX(txRight, toothW, centerX);
            renderer.rect(tx, jawY, toothW, toothH);
        }


        // ===== WHITE SPIDER SYMBOL (center on torso, symmetrical) =====
        renderer.setColor(Color.WHITE);
        float spiderBodyW = bodyWidth * 0.20f;
        float spiderBodyH = torsoHeight * 0.45f;
        float spiderBodyX = centerX - spiderBodyW * 0.5f;
        float spiderBodyY = torsoY + torsoHeight * 0.35f;
        renderer.rect(spiderBodyX, spiderBodyY, spiderBodyW, spiderBodyH);

        float legThickness = spiderBodyH * 0.12f;
        float legLength = bodyWidth * 0.40f;

        // Forward legs (toward facing direction)
        float f1Y = spiderBodyY + spiderBodyH * 0.75f;
        float f2Y = spiderBodyY + spiderBodyH * 0.45f;
        // Backward legs (opposite direction)
        float b1Y = spiderBodyY + spiderBodyH * 0.25f;
        float b2Y = spiderBodyY;

        // forward side
        renderer.rect(spiderBodyX + spiderBodyW * 0.5f,
            f1Y, legLength * dir, legThickness);
        renderer.rect(spiderBodyX + spiderBodyW * 0.5f,
            f2Y, legLength * dir, legThickness);

        // backward side
        renderer.rect(spiderBodyX + spiderBodyW * 0.5f,
            b1Y, -legLength * dir, legThickness);
        renderer.rect(spiderBodyX + spiderBodyW * 0.5f,
            b2Y, -legLength * dir, legThickness);
    }


    // Mirrors an x position around a vertical center line.
    private float mirrorX(float x, float width, float center) {
        // Take the right edge (x + width), mirror it around center,
        // then subtract width to get the new left edge.
        return 2f * center - (x + width);
    }


    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public boolean isGrounded() {
        return grounded;
    }

    // Approximate mouth position where projectiles should spawn
    public float getMouthX() {
        // Center plus some offset toward facing direction
        float centerX = bounds.x + bounds.width * 0.5f;
        float offset = bounds.width * 0.28f;
        return centerX + (isFacingRight() ? offset : -offset);
    }

    public float getMouthY() {
        // Somewhere mid-head / upper torso
        return bounds.y + bounds.height * 0.65f;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    // Area in front of the mouth, used for hitting collectibles later
    public Rectangle getTongueHitbox() {
        if (!tongueActive) return null;

        float length    = TONGUE_MAX_LENGTH;
        float thickness = bounds.height * 0.15f;
        float baseX     = getMouthX();
        float baseY     = getMouthY();

        float x = facingRight ? baseX : baseX - length;
        float y = baseY - thickness / 2f;

        return new Rectangle(x, y, length, thickness);
    }


}
