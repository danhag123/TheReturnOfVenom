package com.oysgemutshet.venom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SplashScreen extends ScreenAdapter {

    private final VengeanceOfVenomGame game;
    private final OrthographicCamera camera;
    private final Texture logoTexture;

    private float time = 0f;

    // timings in seconds
    private static final float BLACK_BEFORE      = 0.5f; // black screen before logo appears
    private static final float FADE_IN_DURATION  = 1.2f;
    private static final float HOLD_DURATION     = 1.8f;

    public SplashScreen(VengeanceOfVenomGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 720);

        // TODO: adjust path to your actual image
        this.logoTexture = new Texture(Gdx.files.internal("splash/112-gaming.png"));
    }

    @Override
    public void render(float delta) {
        time += delta;

        // Clear to black
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        SpriteBatch batch = game.batch;
        batch.setProjectionMatrix(camera.combined);

        float vw = camera.viewportWidth;
        float vh = camera.viewportHeight;

        // Compute alpha for logo fade-in
        float alpha;
        if (time < BLACK_BEFORE) {
            alpha = 0f; // still black, no logo
        } else if (time < BLACK_BEFORE + FADE_IN_DURATION) {
            float t = time - BLACK_BEFORE;
            alpha = t / FADE_IN_DURATION; // 0 → 1
        } else {
            alpha = 1f; // fully visible during hold
        }

        // Draw logo centered with alpha
        if (alpha > 0f) {
            batch.begin();
            batch.setColor(1f, 1f, 1f, alpha);

            float imgW = logoTexture.getWidth();
            float imgH = logoTexture.getHeight();
            float scale = 1f;

            // Scale to fit nicely (optional)
            float targetW = vw * 0.6f;
            if (imgW > 0f) {
                scale = targetW / imgW;
            }

            float drawW = imgW * scale;
            float drawH = imgH * scale;
            float x = (vw - drawW) / 2f;
            float y = (vh - drawH) / 2f;

            batch.draw(logoTexture, x, y, drawW, drawH);

            batch.setColor(1f, 1f, 1f, 1f);
            batch.end();
        }

        // After black + fade-in + hold, go to IntroScreen with global fade
        float totalDuration = BLACK_BEFORE + FADE_IN_DURATION + HOLD_DURATION;
        if (time > totalDuration) {
            game.setScreenWithFade(new IntroScreen(game));
        }
    }

    @Override
    public void dispose() {
        logoTexture.dispose();
    }
}
